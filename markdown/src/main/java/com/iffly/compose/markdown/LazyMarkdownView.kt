@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.iffly.compose.markdown

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.chunkloader.ChunkLoaderConfig
import com.iffly.compose.markdown.chunkloader.FileMarkdownLineSource
import com.iffly.compose.markdown.chunkloader.MarkdownChunkLoader
import com.iffly.compose.markdown.chunkloader.MarkdownChunkNode
import com.iffly.compose.markdown.chunkloader.MarkdownLineSource
import com.iffly.compose.markdown.chunkloader.MarkdownNodeWindow
import com.iffly.compose.markdown.chunkloader.MarkdownNodeWindowSnapshot
import com.iffly.compose.markdown.chunkloader.StringMarkdownLineSource
import com.iffly.compose.markdown.config.LocalNodeDataMap
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.config.currentRenderRegistry
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.dispatcher.MarkdownThreadPool
import com.iffly.compose.markdown.render.MarkdownContent
import com.vladsch.flexmark.util.ast.Node
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.yield
import java.io.File

/** Observable source/parsing activity. AST recycling remains [Idle]. */
enum class LazyMarkdownViewState {
    Idle,
    InitialLoading,
    LoadingBefore,
    LoadingAfter,
}

/** Existing Android file API, now backed by the bounded node-window architecture. */
@Suppress("ktlint:compose:parameter-naming")
@Composable
fun LazyMarkdownView(
    file: File,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    chunkLoaderConfig: ChunkLoaderConfig =
        ChunkLoaderConfig(parserDispatcher = MarkdownThreadPool.dispatcher),
    nestedPrefetchItemCount: Int = 3,
    onLoadingChanged: (Boolean) -> Unit = {},
    onStateChanged: (LazyMarkdownViewState) -> Unit = {},
    onError: (Throwable) -> Unit = {},
) {
    require(nestedPrefetchItemCount >= 0) { "nestedPrefetchItemCount must be non-negative" }
    val source =
        remember(file, chunkLoaderConfig.maxCachedFileLines, chunkLoaderConfig.sourceDispatcher) {
            FileMarkdownLineSource(
                file = file,
                maxCachedFileLines = chunkLoaderConfig.maxCachedFileLines,
                sourceDispatcher = chunkLoaderConfig.sourceDispatcher,
            )
        }
    DisposableEffect(source) {
        onDispose(source::close)
    }
    LazyMarkdownView(
        source = source,
        modifier = modifier,
        markdownRenderConfig = markdownRenderConfig,
        actionHandler = actionHandler,
        renderDependencies = renderDependencies,
        showNotSupportedText = showNotSupportedText,
        chunkLoaderConfig = chunkLoaderConfig,
        nestedPrefetchItemCount = nestedPrefetchItemCount,
        onLoadingChanged = onLoadingChanged,
        onStateChanged = onStateChanged,
        onError = onError,
    )
}

/** Lazily renders an already-loaded Markdown string using a bounded AST window. */
@Suppress("ktlint:compose:parameter-naming")
@Composable
fun LazyMarkdownView(
    text: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    showNotSupportedText: Boolean = false,
    chunkLoaderConfig: ChunkLoaderConfig = ChunkLoaderConfig(),
    nestedPrefetchItemCount: Int = 3,
    lazyListState: LazyListState =
        rememberLazyListState(
            prefetchStrategy = LazyListPrefetchStrategy(nestedPrefetchItemCount),
        ),
    onLoadingChanged: (Boolean) -> Unit = {},
    onStateChanged: (LazyMarkdownViewState) -> Unit = {},
    onError: (Throwable) -> Unit = {},
) {
    val source = remember(text) { StringMarkdownLineSource(text) }
    LazyMarkdownView(
        source = source,
        modifier = modifier,
        markdownRenderConfig = markdownRenderConfig,
        actionHandler = actionHandler,
        renderDependencies = renderDependencies,
        showNotSupportedText = showNotSupportedText,
        chunkLoaderConfig = chunkLoaderConfig,
        nestedPrefetchItemCount = nestedPrefetchItemCount,
        lazyListState = lazyListState,
        onLoadingChanged = onLoadingChanged,
        onStateChanged = onStateChanged,
        onError = onError,
    )
}

/** Lazily reads, parses, recycles, and reloads Markdown from a stable line source. */
@Suppress("ktlint:compose:parameter-naming")
@Composable
fun LazyMarkdownView(
    source: MarkdownLineSource,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    showNotSupportedText: Boolean = false,
    chunkLoaderConfig: ChunkLoaderConfig = ChunkLoaderConfig(),
    nestedPrefetchItemCount: Int = 3,
    lazyListState: LazyListState =
        rememberLazyListState(
            prefetchStrategy = LazyListPrefetchStrategy(nestedPrefetchItemCount),
        ),
    onLoadingChanged: (Boolean) -> Unit = {},
    onStateChanged: (LazyMarkdownViewState) -> Unit = {},
    onError: (Throwable) -> Unit = {},
) {
    require(nestedPrefetchItemCount >= 0) { "nestedPrefetchItemCount must be non-negative" }
    val loader =
        remember(
            source,
            markdownRenderConfig.parser,
            chunkLoaderConfig,
        ) {
            MarkdownChunkLoader(
                source = source,
                parser = markdownRenderConfig.parser,
                maxCachedSourceLines = chunkLoaderConfig.maxCachedSourceLines,
                sourceDispatcher = chunkLoaderConfig.sourceDispatcher,
                parserDispatcher = chunkLoaderConfig.parserDispatcher,
            )
        }
    val window = remember(loader, chunkLoaderConfig) { MarkdownNodeWindow(loader, chunkLoaderConfig) }
    var snapshot by remember(window) { mutableStateOf(MarkdownNodeWindowSnapshot.Empty) }
    var operationInProgress by remember(window) { mutableStateOf(false) }
    val nodeDataMap = remember(window) { mutableStateMapOf<Node, Any>() }
    val currentOnLoadingChanged by rememberUpdatedState(onLoadingChanged)
    val currentOnStateChanged by rememberUpdatedState(onStateChanged)
    val currentOnError by rememberUpdatedState(onError)

    suspend fun performOperation(
        state: LazyMarkdownViewState?,
        operation: suspend () -> Unit,
    ) {
        if (operationInProgress) return
        operationInProgress = true
        if (state != null) currentOnStateChanged(state)
        if (state == LazyMarkdownViewState.InitialLoading) currentOnLoadingChanged(true)
        try {
            operation()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            currentOnError(throwable)
        } finally {
            operationInProgress = false
            if (state == LazyMarkdownViewState.InitialLoading) currentOnLoadingChanged(false)
            if (state != null) currentOnStateChanged(LazyMarkdownViewState.Idle)
        }
    }

    LaunchedEffect(window) {
        performOperation(LazyMarkdownViewState.InitialLoading) {
            snapshot = window.loadInitial()
        }
    }
    LaunchedEffect(snapshot.nodes) {
        val retainedNodes = snapshot.nodes.flatMapTo(mutableSetOf()) { it.node.descendantsAndSelf() }
        nodeDataMap.keys.toList().forEach { node ->
            if (node !in retainedNodes) nodeDataMap.remove(node)
        }
    }
    LaunchedEffect(window, lazyListState, chunkLoaderConfig) {
        snapshotFlow {
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            val first = visibleItems.firstOrNull()
            val last = visibleItems.lastOrNull()
            VisibleNodeWindow(
                firstIndex = first?.index ?: -1,
                lastIndex = last?.index ?: -1,
                firstKey = first?.key as? String,
                lastKey = last?.key as? String,
                nodeCount = snapshot.nodes.size,
                canLoadBefore = snapshot.canLoadBefore,
                canLoadAfter = snapshot.canLoadAfter,
                needsRecycle = snapshot.needsRecycle,
                cachedSourceLineCount = snapshot.cachedSourceLineCount,
                loadDirection =
                    when {
                        lazyListState.lastScrolledBackward -> LoadDirection.Before
                        lazyListState.lastScrolledForward -> LoadDirection.After
                        else -> LoadDirection.After
                    },
            )
        }.distinctUntilChanged()
            .collect { visible ->
                val shouldLoadBefore = visible.shouldLoadBefore(chunkLoaderConfig.minNodesBehind)
                val shouldLoadAfter = visible.shouldLoadAfter(chunkLoaderConfig.minNodesAhead)
                if (visible.shouldRecycle(chunkLoaderConfig)) {
                    performOperation(null) {
                        snapshot = window.recycle(visible.firstKey, visible.lastKey)
                    }
                } else if (shouldLoadBefore &&
                    (!shouldLoadAfter || visible.loadDirection == LoadDirection.Before)
                ) {
                    performOperation(LazyMarkdownViewState.LoadingBefore) {
                        var updated = snapshot
                        do {
                            updated = window.loadBefore(visible.lastKey)
                            yield()
                        } while (
                            updated.canLoadBefore &&
                            !updated.needsRecycle &&
                            updated.nodes.nodesBefore(visible.firstKey) < chunkLoaderConfig.minNodesBehind
                        )
                        snapshot = updated
                    }
                } else if (shouldLoadAfter) {
                    performOperation(LazyMarkdownViewState.LoadingAfter) {
                        var updated = snapshot
                        do {
                            updated = window.loadAfter(visible.firstKey)
                            yield()
                        } while (
                            updated.canLoadAfter &&
                            !updated.needsRecycle &&
                            updated.nodes.nodesAfter(visible.lastKey) < chunkLoaderConfig.minNodesAhead
                        )
                        snapshot = updated
                    }
                }
            }
    }
    DisposableEffect(window) {
        onDispose {
            currentOnLoadingChanged(false)
            currentOnStateChanged(LazyMarkdownViewState.Idle)
        }
    }

    MarkdownLocalProviders(
        markdownRenderConfig = markdownRenderConfig,
        showNotSupportedText = showNotSupportedText,
        actionHandler = actionHandler,
        renderDependencies = renderDependencies,
    ) {
        CompositionLocalProvider(LocalNodeDataMap provides nodeDataMap) {
            val theme = currentTheme()
            val renderRegistry = currentRenderRegistry()
            LazyColumn(
                modifier = modifier,
                state = lazyListState,
            ) {
                itemsIndexed(
                    items = snapshot.nodes,
                    key = { _, chunk -> chunk.key },
                    contentType = { _, chunk -> chunk.node.javaClass },
                ) { index, chunk ->
                    val node = chunk.node
                    if (!renderRegistry.shouldSkipRender(node)) {
                        MarkdownContent(node = node, modifier = Modifier)
                        if (index != snapshot.nodes.lastIndex &&
                            theme.spacerTheme.showSpacer &&
                            renderRegistry.getBlockRenderer(node.javaClass) != null
                        ) {
                            Spacer(Modifier.height(theme.spacerTheme.spacerHeight))
                        }
                    }
                }
            }
        }
    }
}

private fun Node.descendantsAndSelf(): List<Node> =
    buildList {
        val pending = ArrayDeque<Node>()
        pending.addLast(this@descendantsAndSelf)
        while (pending.isNotEmpty()) {
            val node = pending.removeLast()
            add(node)
            var child = node.lastChild
            while (child != null) {
                pending.addLast(child)
                child = child.previous
            }
        }
    }

private enum class LoadDirection { Before, After }

private data class VisibleNodeWindow(
    val firstIndex: Int,
    val lastIndex: Int,
    val firstKey: String?,
    val lastKey: String?,
    val nodeCount: Int,
    val canLoadBefore: Boolean,
    val canLoadAfter: Boolean,
    val needsRecycle: Boolean,
    val cachedSourceLineCount: Int,
    val loadDirection: LoadDirection,
) {
    fun shouldLoadBefore(minNodesBehind: Int): Boolean = canLoadBefore && firstIndex in 0 until minNodesBehind

    fun shouldLoadAfter(minNodesAhead: Int): Boolean = canLoadAfter && lastIndex >= 0 && nodeCount - lastIndex - 1 < minNodesAhead

    fun shouldRecycle(config: ChunkLoaderConfig): Boolean {
        if (!needsRecycle || firstIndex < 0 || lastIndex < 0) return false
        val removableBefore = firstIndex - config.minNodesBehind
        val removableAfter = nodeCount - lastIndex - 1 - config.minNodesAhead
        val removableCount = maxOf(removableBefore, removableAfter)
        return removableCount >= config.minRecycleNodeCount ||
            (removableCount > 0 && cachedSourceLineCount > config.maxCachedSourceLines)
    }
}

private fun List<MarkdownChunkNode>.nodesBefore(key: String?): Int = indexOfFirst { it.key == key }.coerceAtLeast(0)

private fun List<MarkdownChunkNode>.nodesAfter(key: String?): Int {
    val index = indexOfLast { it.key == key }
    return if (index < 0) 0 else size - index - 1
}
