package com.iffly.compose.markdown.util

/**
 * LRU cache implementation based on LinkedHashMap
 * Directly inherits from LinkedHashMap and overrides the removeEldestEntry method
 */
class LRUCache<K, V>(
    private val maxSize: Int
) : LinkedHashMap<K, V>(maxSize / 4, 0.75f, true) {

    /**
     * Override removeEldestEntry method to automatically remove the oldest entry when cache exceeds maximum size
     */
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > maxSize
    }

    /**
     * Get maximum cache size
     */
    fun maxSize(): Int {
        return maxSize
    }

    /**
     * Get cache statistics
     */
    fun getStats(): CacheStats {
        return CacheStats(
            currentSize = size,
            maxSize = maxSize,
            loadFactor = size.toFloat() / maxSize
        )
    }

    /**
     * Cache statistics information
     */
    data class CacheStats(
        val currentSize: Int,
        val maxSize: Int,
        val loadFactor: Float
    )
}