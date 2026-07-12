package com.iffly.compose.markdown.chunkloader

import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Stable, zero-based, rereadable source of Markdown lines. */
fun interface MarkdownLineSource {
    suspend fun readLines(
        startLine: Int,
        lineCount: Int,
    ): List<String>
}

/** In-memory line source for Markdown already held as a string. */
class StringMarkdownLineSource(
    text: String,
) : MarkdownLineSource {
    private val lines = text.lines()

    override suspend fun readLines(
        startLine: Int,
        lineCount: Int,
    ): List<String> {
        require(startLine >= 0) { "startLine must be non-negative" }
        require(lineCount > 0) { "lineCount must be positive" }
        if (startLine >= lines.size) return emptyList()
        return lines.subList(startLine, minOf(startLine + lineCount, lines.size))
    }
}

/** File-backed line source with bounded line caching. */
class FileMarkdownLineSource(
    file: File,
    maxCachedFileLines: Int = 2000,
    sourceDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MarkdownLineSource,
    AutoCloseable {
    private val reader =
        CachedMarkdownFileReader(
            file = file,
            maxCacheSize = maxCachedFileLines,
            dispatcher = sourceDispatcher,
        )

    override suspend fun readLines(
        startLine: Int,
        lineCount: Int,
    ): List<String> {
        require(startLine >= 0) { "startLine must be non-negative" }
        require(lineCount > 0) { "lineCount must be positive" }
        return when (val result = reader.readLines(startLine + 1, startLine + lineCount)) {
            is ReadResult.Success -> result.data
            is ReadResult.EndOfFile -> emptyList()
            is ReadResult.Error -> throw result.exception
        }
    }

    override fun close() {
        reader.close()
    }
}

/** Configuration shared with the Multiplatform lazy node-window design. */
data class ChunkLoaderConfig(
    val initialLineCount: Int = 1000,
    val incrementalLineCount: Int = 500,
    val minNodesAhead: Int = 100,
    val minNodesBehind: Int = 30,
    val maxCachedNodes: Int = 500,
    val maxCachedSourceLines: Int = 10_000,
    val minRecycleNodeCount: Int = 50,
    val maxCachedFileLines: Int = 2000,
    val sourceDispatcher: CoroutineDispatcher = Dispatchers.IO,
    val parserDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    init {
        require(initialLineCount > 0) { "initialLineCount must be positive" }
        require(incrementalLineCount > 0) { "incrementalLineCount must be positive" }
        require(minNodesAhead > 0) { "minNodesAhead must be positive" }
        require(minNodesBehind >= 0) { "minNodesBehind must be non-negative" }
        require(maxCachedNodes > 0) { "maxCachedNodes must be positive" }
        require(maxCachedSourceLines > 0) { "maxCachedSourceLines must be positive" }
        require(minRecycleNodeCount > 0) { "minRecycleNodeCount must be positive" }
        require(minRecycleNodeCount <= maxCachedNodes) {
            "minRecycleNodeCount must not exceed maxCachedNodes"
        }
        require(minNodesAhead + minNodesBehind < maxCachedNodes) {
            "minNodesAhead + minNodesBehind must be smaller than maxCachedNodes"
        }
        require(maxCachedFileLines > 0) { "maxCachedFileLines must be positive" }
    }
}

internal data class MarkdownChunkNode(
    val node: Block,
    val startLine: Int,
    val endLine: Int,
    val key: String,
)

internal data class MarkdownChunkLoadResult(
    val nodes: List<MarkdownChunkNode>,
    val endOfSource: Boolean,
)

internal data class MarkdownNodeWindowSnapshot(
    val nodes: List<MarkdownChunkNode>,
    val canLoadBefore: Boolean,
    val canLoadAfter: Boolean,
    val needsRecycle: Boolean,
    val cachedSourceLineCount: Int,
) {
    companion object {
        val Empty = MarkdownNodeWindowSnapshot(emptyList(), false, true, false, 0)
    }
}

private data class EvictedNodeRange(
    val lines: IntRange,
    val nodeCount: Int,
    val keyHash: Int,
)

/** Incremental parser that keeps only the unconfirmed trailing source block between reads. */
internal class MarkdownChunkLoader(
    private val source: MarkdownLineSource,
    private val parser: Parser,
    private val maxCachedSourceLines: Int = 10_000,
    private val sourceDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val parserDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val mutex = Mutex()
    private var pendingStartLine = 0
    private val pendingLines = mutableListOf<String>()
    private var nextLine = 0
    private var endOfSource = false

    suspend fun load(lineCount: Int): MarkdownChunkLoadResult =
        mutex.withLock {
            if (endOfSource) return@withLock MarkdownChunkLoadResult(emptyList(), true)

            val loadedLines = readLines(nextLine, lineCount)
            require(loadedLines.size <= lineCount) {
                "MarkdownLineSource returned ${loadedLines.size} lines for a $lineCount-line request"
            }
            val reachedEnd = loadedLines.size < lineCount
            nextLine += loadedLines.size
            pendingLines.addAll(loadedLines)

            if (pendingLines.isEmpty()) {
                endOfSource = true
                return@withLock MarkdownChunkLoadResult(emptyList(), true)
            }

            val parsedNodes = parse(pendingLines)
            val trailingNode =
                parsedNodes.lastOrNull()?.takeIf { node ->
                    !reachedEnd && node.endLineNumber >= pendingLines.lastIndex
                }
            val emittedNodes = if (trailingNode == null) parsedNodes else parsedNodes.dropLast(1)
            val chunks = toChunkNodes(emittedNodes, pendingStartLine)

            if (reachedEnd) {
                pendingLines.clear()
                endOfSource = true
            } else {
                if (chunks.isNotEmpty()) {
                    val consumedLineCount = chunks.last().endLine - pendingStartLine + 1
                    pendingLines.subList(0, consumedLineCount).clear()
                    pendingStartLine += consumedLineCount
                }
                discardLeadingBlankLines()
                require(pendingLines.size <= maxCachedSourceLines) {
                    "Unconfirmed Markdown source exceeded maxCachedSourceLines=$maxCachedSourceLines"
                }
            }

            MarkdownChunkLoadResult(chunks, endOfSource)
        }

    suspend fun reload(range: IntRange): List<MarkdownChunkNode> =
        mutex.withLock {
            require(!range.isEmpty()) { "range must not be empty" }
            val expectedLineCount = range.last - range.first + 1
            val lines = readLines(range.first, expectedLineCount)
            require(lines.size == expectedLineCount) {
                "MarkdownLineSource changed while reloading lines ${range.first}..${range.last}"
            }
            toChunkNodes(parse(lines), range.first)
        }

    private suspend fun readLines(
        startLine: Int,
        lineCount: Int,
    ): List<String> =
        withContext(sourceDispatcher) {
            source.readLines(startLine, lineCount)
        }

    private suspend fun parse(lines: List<String>): List<Block> =
        withContext(parserDispatcher) {
            val document = parser.parse(lines.joinToString("\n"))
            buildList {
                var node: Node? = document.firstChild
                while (node != null) {
                    val next = node.next
                    if (node is Block) {
                        node.unlink()
                        add(node)
                    }
                    node = next
                }
            }
        }

    private fun discardLeadingBlankLines() {
        val firstContentIndex = pendingLines.indexOfFirst(String::isNotBlank)
        val discardCount = if (firstContentIndex < 0) pendingLines.size else firstContentIndex
        if (discardCount > 0) {
            pendingLines.subList(0, discardCount).clear()
            pendingStartLine += discardCount
        }
    }

    private fun toChunkNodes(
        nodes: List<Block>,
        baseLine: Int,
    ): List<MarkdownChunkNode> {
        val keys = mutableSetOf<String>()
        return nodes.map { node ->
            val startLine = baseLine + node.startLineNumber
            val endLine = baseLine + node.endLineNumber
            val sourceLineCount = endLine - startLine + 1
            require(sourceLineCount <= maxCachedSourceLines) {
                "${node.javaClass.simpleName} exceeded maxCachedSourceLines=$maxCachedSourceLines"
            }
            val key =
                "${node.javaClass.name}:$startLine:$endLine:" +
                    "${node.startOffset - node.startOfLine}:${node.chars.length}"
            require(keys.add(key)) {
                "LazyMarkdownView requires unique source positions for top-level nodes"
            }
            MarkdownChunkNode(node, startLine, endLine, key)
        }
    }
}

/** Bounded, bidirectional AST window shared conceptually with the MPP implementation. */
internal class MarkdownNodeWindow(
    private val loader: MarkdownChunkLoader,
    private val config: ChunkLoaderConfig,
) {
    private val mutex = Mutex()
    private var nodes = emptyList<MarkdownChunkNode>()
    private val evictedBefore = ArrayDeque<EvictedNodeRange>()
    private val evictedAfter = ArrayDeque<EvictedNodeRange>()
    private var endOfSource = false

    suspend fun loadInitial(): MarkdownNodeWindowSnapshot =
        mutex.withLock {
            if (nodes.isEmpty() && !endOfSource) {
                appendFromSource(config.initialLineCount)
                while (
                    nodes.size < config.minNodesAhead &&
                    nodes.size < config.maxCachedNodes &&
                    cachedSourceLineCount() < config.maxCachedSourceLines &&
                    !endOfSource
                ) {
                    appendFromSource(config.incrementalLineCount)
                }
            }
            snapshot()
        }

    suspend fun loadBefore(lastVisibleKey: String?): MarkdownNodeWindowSnapshot =
        mutex.withLock {
            val range = evictedBefore.lastOrNull() ?: return@withLock snapshot()
            val loaded = reload(range)
            evictedBefore.removeLast()
            nodes = loaded + nodes
            recycleAfter(lastVisibleKey)
            snapshot()
        }

    suspend fun loadAfter(firstVisibleKey: String?): MarkdownNodeWindowSnapshot =
        mutex.withLock {
            if (evictedAfter.isNotEmpty()) {
                val range = evictedAfter.first()
                val loaded = reload(range)
                evictedAfter.removeFirst()
                nodes = nodes + loaded
            } else if (!endOfSource) {
                appendFromSource(config.incrementalLineCount)
            }
            recycleBefore(firstVisibleKey)
            snapshot()
        }

    suspend fun recycle(
        firstVisibleKey: String?,
        lastVisibleKey: String?,
    ): MarkdownNodeWindowSnapshot =
        mutex.withLock {
            val firstVisibleIndex = nodes.indexOfFirst { it.key == firstVisibleKey }
            val lastVisibleIndex = nodes.indexOfLast { it.key == lastVisibleKey }
            if (firstVisibleIndex < 0 || lastVisibleIndex < 0) return@withLock snapshot()

            val removableBefore = (firstVisibleIndex - config.minNodesBehind).coerceAtLeast(0)
            val removableAfter = (nodes.lastIndex - lastVisibleIndex - config.minNodesAhead).coerceAtLeast(0)
            if (removableBefore > removableAfter) {
                recycleBefore(firstVisibleKey)
            } else {
                recycleAfter(lastVisibleKey)
            }
            snapshot()
        }

    private suspend fun appendFromSource(lineCount: Int) {
        do {
            val result = loader.load(lineCount)
            nodes = nodes + result.nodes
            endOfSource = result.endOfSource
        } while (nodes.isEmpty() && !endOfSource)
    }

    private fun recycleBefore(firstVisibleKey: String?) {
        val firstVisibleIndex = nodes.indexOfFirst { it.key == firstVisibleKey }
        if (firstVisibleIndex < 0) return
        val removableCount = (firstVisibleIndex - config.minNodesBehind).coerceAtLeast(0)
        val recycleCount = requiredRecycleFromStart().coerceAtMost(removableCount)
        if (!shouldRecycle(recycleCount)) return

        val removed = nodes.take(recycleCount)
        nodes = nodes.drop(recycleCount)
        evictedBefore.addLast(removed.toEvictedRange())
    }

    private fun recycleAfter(lastVisibleKey: String?) {
        val lastVisibleIndex = nodes.indexOfLast { it.key == lastVisibleKey }
        if (lastVisibleIndex < 0) return
        val removableCount = (nodes.lastIndex - lastVisibleIndex - config.minNodesAhead).coerceAtLeast(0)
        val recycleCount = requiredRecycleFromEnd().coerceAtMost(removableCount)
        if (!shouldRecycle(recycleCount)) return

        val removed = nodes.takeLast(recycleCount)
        nodes = nodes.dropLast(recycleCount)
        evictedAfter.addFirst(removed.toEvictedRange())
    }

    private suspend fun reload(range: EvictedNodeRange): List<MarkdownChunkNode> {
        val loaded = loader.reload(range.lines)
        require(loaded.size == range.nodeCount && loaded.map { it.key }.hashCode() == range.keyHash) {
            "MarkdownLineSource produced a different node structure while reloading ${range.lines}"
        }
        return loaded
    }

    private fun List<MarkdownChunkNode>.toEvictedRange(): EvictedNodeRange =
        EvictedNodeRange(
            lines = first().startLine..last().endLine,
            nodeCount = size,
            keyHash = map { it.key }.hashCode(),
        )

    private fun requiredRecycleFromStart(): Int {
        var count = (nodes.size - config.maxCachedNodes).coerceAtLeast(0)
        while (count < nodes.size && sourceLineCount(nodes.drop(count)) > config.maxCachedSourceLines) count++
        return count
    }

    private fun requiredRecycleFromEnd(): Int {
        var count = (nodes.size - config.maxCachedNodes).coerceAtLeast(0)
        while (count < nodes.size && sourceLineCount(nodes.dropLast(count)) > config.maxCachedSourceLines) count++
        return count
    }

    private fun shouldRecycle(recycleCount: Int): Boolean =
        recycleCount >= config.minRecycleNodeCount ||
            (recycleCount > 0 && cachedSourceLineCount() > config.maxCachedSourceLines)

    private fun cachedSourceLineCount(): Int = sourceLineCount(nodes)

    private fun sourceLineCount(nodes: List<MarkdownChunkNode>): Int =
        if (nodes.isEmpty()) 0 else nodes.last().endLine - nodes.first().startLine + 1

    private fun snapshot(): MarkdownNodeWindowSnapshot =
        MarkdownNodeWindowSnapshot(
            nodes = nodes,
            canLoadBefore = evictedBefore.isNotEmpty(),
            canLoadAfter = evictedAfter.isNotEmpty() || !endOfSource,
            needsRecycle =
                nodes.size - config.maxCachedNodes >= config.minRecycleNodeCount ||
                    cachedSourceLineCount() > config.maxCachedSourceLines,
            cachedSourceLineCount = cachedSourceLineCount(),
        )
}
