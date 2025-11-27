package com.iffly.compose.markdown.chunkloader

import com.iffly.compose.markdown.util.LRUCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

/**
 * Sealed class for read results, for better error handling
 */
sealed class ReadResult<out T> {
    data class Success<T>(
        val data: T,
    ) : ReadResult<T>()

    data class Error(
        val exception: Throwable,
        val message: String,
    ) : ReadResult<Nothing>()

    object EndOfFile : ReadResult<Nothing>()
}

/**
 * File reader interface for easy testing and extension
 */
interface IMarkdownFileReader {
    suspend fun readLines(
        startLine: Int,
        endLine: Int,
    ): ReadResult<List<String>>

    suspend fun readLine(lineNumber: Int): ReadResult<String>

    fun clearCache()

    fun getCacheInfo(): FileCacheInfo

    fun close()
}

/**
 * Cache statistics information
 */
data class FileCacheInfo(
    val cacheSize: Int,
    val maxCacheSize: Int,
    val currentReaderLine: Int,
    val isReaderOpen: Boolean,
    val loadFactor: Float,
    val hitRate: Float = 0f,
    val missRate: Float = 0f,
)

/**
 * LRU cache-based file reader
 * Supports line-level caching and BufferedReader state management
 *
 * @param file The file to read
 * @param maxCacheSize Maximum number of cached lines
 * @param dispatcher Coroutine dispatcher
 */
class CachedMarkdownFileReader(
    private val file: File,
    private val maxCacheSize: Int = 1000,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IMarkdownFileReader {
    // Reader state management
    private var bufferedReader: BufferedReader? = null
    private var currentLineNumber = 0

    // Cache and statistics
    private val lineCache = LRUCache<Int, String>(maxCacheSize)
    private var cacheHits = 0L
    private var cacheMisses = 0L

    // Thread safety
    private val mutex = Mutex()

    init {
        require(file.exists()) { "File does not exist: ${file.absolutePath}" }
        require(file.isFile) { "Path is not a file: ${file.absolutePath}" }
        require(file.canRead()) { "File is not readable: ${file.absolutePath}" }
        require(maxCacheSize > 0) { "Cache size must be positive: $maxCacheSize" }
    }

    /**
     * Read lines within specified range
     */
    override suspend fun readLines(
        startLine: Int,
        endLine: Int,
    ): ReadResult<List<String>> =
        withContext(dispatcher) {
            require(startLine >= 1) { "Start line must be >= 1, got: $startLine" }
            require(endLine >= startLine) { "End line ($endLine) must be >= start line ($startLine)" }

            mutex.withLock {
                try {
                    val lines = mutableListOf<String>()

                    for (lineNum in startLine..endLine) {
                        when (val result = readLineInternal(lineNum)) {
                            is ReadResult.Success -> lines.add(result.data)
                            is ReadResult.EndOfFile -> break
                            is ReadResult.Error -> return@withContext result
                        }
                    }

                    ReadResult.Success(lines)
                } catch (e: Exception) {
                    ReadResult.Error(e, "Failed to read lines $startLine-$endLine: ${e.message}")
                }
            }
        }

    /**
     * Read single line
     */
    override suspend fun readLine(lineNumber: Int): ReadResult<String> =
        withContext(dispatcher) {
            require(lineNumber >= 1) { "Line number must be >= 1, got: $lineNumber" }

            mutex.withLock {
                readLineInternal(lineNumber)
            }
        }

    /**
     * Internal single line read implementation, supports caching and smart BufferedReader management
     */
    private fun readLineInternal(lineNumber: Int): ReadResult<String> {
        return try {
            // 1. Check cache
            lineCache[lineNumber]?.let { cachedLine ->
                cacheHits++
                return ReadResult.Success(cachedLine)
            }

            cacheMisses++

            // 2. Check if reader reset is needed
            if (shouldResetReader(lineNumber)) {
                resetReader()
            }

            // 3. Ensure reader is initialized
            if (bufferedReader == null) {
                resetReader()
            }

            // 4. Read until target line
            readUntilLine(lineNumber)
        } catch (e: Exception) {
            closeReader()
            ReadResult.Error(e, "Failed to read line $lineNumber: ${e.message}")
        }
    }

    /**
     * Determine if reader reset is needed
     */
    private fun shouldResetReader(targetLine: Int): Boolean = targetLine < currentLineNumber && bufferedReader != null

    /**
     * Read until specified line number
     */
    private fun readUntilLine(targetLine: Int): ReadResult<String> {
        val reader =
            bufferedReader ?: return ReadResult.Error(
                IllegalStateException("BufferedReader not initialized"),
                "Reader not available",
            )

        try {
            while (currentLineNumber < targetLine) {
                val line =
                    reader.readLine()
                        ?: return ReadResult.EndOfFile

                currentLineNumber++
                cacheLine(currentLineNumber, line)

                if (currentLineNumber == targetLine) {
                    return ReadResult.Success(line)
                }
            }

            // If current line number equals target line number, return cached line
            return lineCache[targetLine]?.let {
                ReadResult.Success(it)
            } ?: ReadResult.EndOfFile
        } catch (e: IOException) {
            closeReader()
            return ReadResult.Error(e, "IO error reading line $targetLine")
        }
    }

    /**
     * Cache line content
     */
    private fun cacheLine(
        lineNumber: Int,
        content: String,
    ) {
        lineCache[lineNumber] = content
    }

    /**
     * Reset BufferedReader to beginning of file
     */
    private fun resetReader() {
        closeReader()
        try {
            bufferedReader = BufferedReader(FileReader(file))
            currentLineNumber = 0
        } catch (e: IOException) {
            throw IllegalStateException("Failed to reset reader: ${e.message}", e)
        }
    }

    /**
     * Close BufferedReader
     */
    private fun closeReader() {
        bufferedReader?.close()
        bufferedReader = null
        currentLineNumber = 0
    }

    /**
     * Clear cache
     */
    override fun clearCache() {
        lineCache.clear()
        cacheHits = 0L
        cacheMisses = 0L
    }

    /**
     * Get cache information
     */
    override fun getCacheInfo(): FileCacheInfo {
        val totalRequests = cacheHits + cacheMisses
        return FileCacheInfo(
            cacheSize = lineCache.size,
            maxCacheSize = maxCacheSize,
            currentReaderLine = currentLineNumber,
            isReaderOpen = bufferedReader != null,
            loadFactor = lineCache.size.toFloat() / maxCacheSize,
            hitRate = if (totalRequests > 0) cacheHits.toFloat() / totalRequests else 0f,
            missRate = if (totalRequests > 0) cacheMisses.toFloat() / totalRequests else 0f,
        )
    }

    /**
     * Close reader and release resources
     */
    override fun close() {
        closeReader()
        clearCache()
    }
}
