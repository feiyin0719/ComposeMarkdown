package com.iffly.compose.markdown.chunkloader

import com.iffly.compose.markdown.ScrollDirection
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Chunk loading configuration
 */
data class ChunkLoaderConfig(
    val initialLines: Int = 1000,
    val incrementalLines: Int = 500,
    val maxCachedChunks: Int = 1000,
    val maxCachedFileLines: Int = 2000,
    val chunkSize: Int = 5,
    val parserDispatcher: CoroutineDispatcher = Dispatchers.Default,
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
)

/**
 * Chunk loading result
 */
sealed class LoadResult {
    object Success : LoadResult()
    data class Error(val message: String, val cause: Throwable? = null) : LoadResult()
}

/**
 * Markdown chunk loader
 * Responsible for loading and parsing Markdown file content on demand
 */
class MarkdownChunkLoader(
    fileSource: File,
    renderConfig: MarkdownRenderConfig,
    private val config: ChunkLoaderConfig = ChunkLoaderConfig()
) {
    private var visibleLineRange: IntRange = IntRange.EMPTY
    private val chunkCache = ChunkCache(config.maxCachedChunks)
    private val nodeToChunkMap = NodeToChunkMapper()
    private val fileReader = CachedMarkdownFileReader(
        fileSource,
        maxCacheSize = config.maxCachedFileLines,
        dispatcher = config.ioDispatcher
    )
    private val markdownChunkParser = MarkdownChunkParser(renderConfig, config.parserDispatcher)

    // nodes flow
    private val _nodesFlow = MutableStateFlow<List<Block>>(emptyList())
    val nodes: StateFlow<List<Block>> = _nodesFlow.asStateFlow()

    private val _loadingFlow = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _loadingFlow.asStateFlow()

    /**
     * Load initial chunks
     */
    suspend fun loadInitialChunks(): LoadResult {
        return try {
            _loadingFlow.value = true
            updateVisibleLineRange(0..config.initialLines, ScrollDirection.NONE)
            LoadResult.Success
        } catch (e: Exception) {
            LoadResult.Error("Failed to load initial chunks", e)
        } finally {
            _loadingFlow.value = false
        }
    }

    /**
     * Load more chunks based on visible nodes
     */
    suspend fun loadMoreChunksIfNeeded(
        firstVisibleNode: Node,
        lastVisibleNode: Node,
        scrollDirection: ScrollDirection = ScrollDirection.NONE
    ): LoadResult {
        return try {
            _loadingFlow.value = true

            val newRange = calculateNewRange(firstVisibleNode, lastVisibleNode, scrollDirection)
            if (newRange.isEmpty() || newRange == visibleLineRange) {
                return LoadResult.Success
            }

            updateVisibleLineRange(newRange, scrollDirection)
            LoadResult.Success
        } catch (e: Exception) {
            LoadResult.Error("Failed to load more chunks", e)
        } finally {
            _loadingFlow.value = false
        }
    }

    /**
     * Calculate new visible range
     */
    private fun calculateNewRange(
        firstVisibleNode: Node,
        lastVisibleNode: Node,
        scrollDirection: ScrollDirection
    ): IntRange {
        val startLineNumber = nodeToChunkMap.getChunk(firstVisibleNode)?.startLineNumber ?: 0
        val endLineNumber = nodeToChunkMap.getChunk(lastVisibleNode)?.endLineNumber ?: 0

        return when (scrollDirection) {
            ScrollDirection.UP -> {
                val newStart = (startLineNumber - config.incrementalLines * 2).coerceAtLeast(0)
                newStart..endLineNumber
            }

            ScrollDirection.DOWN -> {
                val newEnd = endLineNumber + config.incrementalLines * 2
                startLineNumber..newEnd
            }

            ScrollDirection.NONE -> {
                val newStart = (startLineNumber - config.incrementalLines).coerceAtLeast(0)
                val newEnd = endLineNumber + config.incrementalLines
                newStart..newEnd
            }
        }
    }

    /**
     * Update visible line range
     */
    private suspend fun updateVisibleLineRange(
        newRange: IntRange,
        scrollDirection: ScrollDirection
    ) {
        // Load content before current range
        if (newRange.first < visibleLineRange.first) {
            loadChunksBeforeCurrent(newRange.first, visibleLineRange.first - 1)
        }

        // Load content after current range
        if (newRange.last > visibleLineRange.last) {
            loadChunksAfterCurrent(visibleLineRange.last + 1, newRange.last)
        }

        // Clean up cache
        chunkCache.recycleCacheIfNeeded(scrollDirection)

        // Update state
        updateState()
    }

    /**
     * Load chunks before current range
     */
    private suspend fun loadChunksBeforeCurrent(startLine: Int, endLine: Int) {
        var currentStart = startLine.coerceAtLeast(0)
        val currentEnd = endLine.coerceAtLeast(0)

        while (currentStart <= currentEnd) {
            val chunks = markdownChunkParser.parseRange(fileReader, currentStart, currentEnd)
            if (chunks == null || chunks.isNotEmpty()) { // Allow null to indicate read failure
                chunks?.let {
                    chunkCache.addChunksAtBeginning(chunks)
                    chunks.forEach { nodeToChunkMap.addChunk(it) }
                }
                break
            }
            // If no content, expand search range
            currentStart = (currentStart - config.incrementalLines).coerceAtLeast(0)
            if (currentStart == 0) break
        }
    }

    /**
     * Load chunks after current range
     */
    private suspend fun loadChunksAfterCurrent(startLine: Int, endLine: Int) {
        val currentStart = startLine
        var currentEnd = endLine

        while (currentStart <= currentEnd) {
            val chunks = markdownChunkParser.parseRange(
                fileReader,
                currentStart,
                currentEnd,
                dropFirst = false
            )
            if (chunks == null || chunks.isNotEmpty()) { // Allow null to indicate read failure
                chunks?.let {
                    chunkCache.addChunksAtEnd(chunks)
                    chunks.forEach { nodeToChunkMap.addChunk(it) }
                }
                break
            }
            // If no content, expand search range
            currentEnd += config.incrementalLines
        }
    }

    /**
     * Update state
     */
    private fun updateState() {
        val allChunks = chunkCache.getAllChunks()
        if (allChunks.isNotEmpty()) {
            val newStart = allChunks.first().startLineNumber
            val newEnd = allChunks.last().endLineNumber
            visibleLineRange = newStart..newEnd
        }
        _nodesFlow.value = getNodes()
    }

    /**
     * Get all current nodes
     */
    fun getNodes(): List<Block> {
        return chunkCache.getAllChunks().flatMap { it.nodes }
    }

    /**
     * Get cache information
     */
    fun getCacheInfo(): MarkdownChunkCacheInfo {
        return MarkdownChunkCacheInfo(
            chunkCacheSize = chunkCache.size(),
            visibleLineRange = visibleLineRange,
            fileReaderCache = fileReader.getCacheInfo()
        )
    }

    /**
     * Clean up resources
     */
    fun close() {
        fileReader.close()
        chunkCache.clear()
        nodeToChunkMap.clear()
    }

    /**
     * Cache information data class
     */
    data class MarkdownChunkCacheInfo(
        val chunkCacheSize: Int,
        val visibleLineRange: IntRange,
        val fileReaderCache: FileCacheInfo
    )
}

/**
 * Markdown chunk data class
 */
internal data class MarkdownChunk(
    val nodes: List<Block>,
    val startLineNumber: Int,
    val endLineNumber: Int,
) {
    val isValid: Boolean
        get() = nodes.isNotEmpty() && startLineNumber <= endLineNumber
}

/**
 * Chunk cache manager
 */
private class ChunkCache(private val maxSize: Int) {
    private val chunks = mutableListOf<MarkdownChunk>()

    fun addChunksAtBeginning(newChunks: List<MarkdownChunk>) {
        chunks.addAll(0, newChunks)
    }

    fun addChunksAtEnd(newChunks: List<MarkdownChunk>) {
        chunks.addAll(newChunks)
    }

    fun getAllChunks(): List<MarkdownChunk> = chunks.toList()

    fun size(): Int = chunks.size

    fun recycleCacheIfNeeded(scrollDirection: ScrollDirection) {
        if (chunks.size <= maxSize) return

        val toRemove = chunks.size - maxSize
        repeat(toRemove) {
            when (scrollDirection) {
                ScrollDirection.UP -> chunks.removeLastOrNull()
                ScrollDirection.DOWN -> chunks.removeFirstOrNull()
                ScrollDirection.NONE -> {
                    if (it % 2 == 0) chunks.removeFirstOrNull()
                    else chunks.removeLastOrNull()
                }
            }
        }
    }

    fun clear() {
        chunks.clear()
    }
}

/**
 * Node to chunk mapping manager
 */
private class NodeToChunkMapper {
    private val nodeToChunkMap = mutableMapOf<Node, MarkdownChunk>()

    fun addChunk(chunk: MarkdownChunk) {
        chunk.nodes.forEach { node ->
            nodeToChunkMap[node] = chunk
        }
    }

    fun getChunk(node: Node): MarkdownChunk? = nodeToChunkMap[node]

    fun removeChunk(chunk: MarkdownChunk) {
        chunk.nodes.forEach { node ->
            nodeToChunkMap.remove(node)
        }
    }

    fun clear() {
        nodeToChunkMap.clear()
    }
}

/**
 * Markdown chunk parser
 */
private class MarkdownChunkParser(
    private val renderConfig: MarkdownRenderConfig,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun parseRange(
        fileReader: CachedMarkdownFileReader,
        startLine: Int,
        endLine: Int,
        chunkSize: Int = 5,
        dropFirst: Boolean = true
    ): List<MarkdownChunk>? = withContext(dispatcher) {
        try {
            val result = fileReader.readLines(startLine, endLine)
            val lines = (result as? ReadResult.Success)?.data ?: return@withContext null
            if (lines.isEmpty()) return@withContext null

            val document = renderConfig.parser.parse(lines.joinToString("\n"))
            val chunks = mutableListOf<MarkdownChunk>()
            val blocks = mutableListOf<Block>()

            var node = if (dropFirst) document.firstChild?.next else document.firstChild

            while (node != null) {
                if (!dropFirst && node == document.lastChild) break

                if (node is Block) {
                    blocks.add(node)
                }

                if (blocks.size >= chunkSize) {
                    chunks.add(createChunk(blocks.toList(), startLine))
                    blocks.clear()
                }

                node = node.next
            }

            if (blocks.isNotEmpty()) {
                chunks.add(createChunk(blocks, startLine))
            }

            chunks.filter { it.isValid }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun createChunk(blocks: List<Block>, baseLineNumber: Int): MarkdownChunk {
        return MarkdownChunk(
            nodes = blocks,
            startLineNumber = baseLineNumber + blocks.first().startLineNumber,
            endLineNumber = baseLineNumber + blocks.last().endLineNumber
        )
    }
}