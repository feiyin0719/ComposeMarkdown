package com.iffly.compose.markdown.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 专门用于Markdown解析的线程池管理器
 * 提供独立的线程池，避免与其他后台任务竞争资源
 */
object MarkdownThreadPool {

    private val threadCounter = AtomicInteger(0)

    /**
     * 自定义线程工厂，为markdown解析线程提供有意义的名称
     */
    private val markdownThreadFactory = ThreadFactory { runnable ->
        Thread(runnable, "MarkdownParser-${threadCounter.incrementAndGet()}").apply {
            isDaemon = true
            priority = Thread.NORM_PRIORITY
        }
    }

    /**
     * 专用的线程池执行器
     * 使用固定大小的线程池，大小为可用处理器核心数
     */
    private val executor = Executors.newFixedThreadPool(
        1,
        markdownThreadFactory
    )

    /**
     * 提供给协程使用的调度器
     */
    val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

    /**
     * 关闭线程池，释放资源
     * 通常在应用退出时调用
     */
    fun shutdown() {
        executor.shutdown()
    }

    /**
     * 立即关闭线程池
     */
    fun shutdownNow() {
        executor.shutdownNow()
    }
}
