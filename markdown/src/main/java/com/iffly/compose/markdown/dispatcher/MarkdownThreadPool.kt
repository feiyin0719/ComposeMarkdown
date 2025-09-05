package com.iffly.compose.markdown.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Thread pool manager specifically for Markdown parsing
 * Provides independent thread pool to avoid resource competition with other background tasks
 */
object MarkdownThreadPool {

    private val threadCounter = AtomicInteger(0)

    /**
     * Custom thread factory to provide meaningful names for markdown parsing threads
     */
    private val markdownThreadFactory = ThreadFactory { runnable ->
        Thread(runnable, "MarkdownParser-${threadCounter.incrementAndGet()}").apply {
            isDaemon = true
            priority = Thread.NORM_PRIORITY
        }
    }

    /**
     * Dedicated thread pool executor
     * Uses fixed-size thread pool, size equals to available processor cores
     */
    private val executor = Executors.newFixedThreadPool(
        1,
        markdownThreadFactory
    )

    /**
     * Dispatcher for coroutine usage
     */
    val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

    /**
     * Shutdown thread pool and release resources
     * Usually called when application exits
     */
    fun shutdown() {
        executor.shutdown()
    }

    /**
     * Immediately shutdown thread pool
     */
    fun shutdownNow() {
        executor.shutdownNow()
    }
}
