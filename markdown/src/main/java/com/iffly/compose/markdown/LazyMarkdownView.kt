package com.iffly.compose.markdown

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkInteractionListener
import com.iffly.compose.markdown.chunkloader.ChunkLoaderConfig
import com.iffly.compose.markdown.chunkloader.MarkdownChunkLoader
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.dispatcher.MarkdownThreadPool
import com.iffly.compose.markdown.render.MarkdownBlock
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.File

/**
 * A Composable that displays a markdown file in a lazy-loading manner.
 * It loads and renders markdown content in chunks as the user scrolls.
 * This is useful for displaying large markdown files efficiently.
 * @param file The markdown file to be displayed.
 * @param markdownRenderConfig Configuration for rendering the markdown.
 * @param modifier Modifier to be applied to the LazyColumn.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param linkInteractionListener Listener for link interactions.
 * @param chunkLoaderConfig Configuration for the chunk loader, including parser dispatcher.
 * @param nestedPrefetchItemCount Number of items to prefetch before and after the visible items for smoother scrolling.
 * Note: The parserDispatcher in chunkLoaderConfig should be set to a background thread dispatcher
 * to avoid blocking the main thread during parsing.
 * Default is MarkdownThreadPool.dispatcher.
 * LazyColumn has built-in prefetching enabled by default for better scrolling performance.
 *
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyMarkdownView(
    file: File,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    linkInteractionListener: LinkInteractionListener? = null,
    chunkLoaderConfig: ChunkLoaderConfig = ChunkLoaderConfig(parserDispatcher = MarkdownThreadPool.dispatcher),
    nestedPrefetchItemCount: Int = 3,
) {
    val markdownChunkLoader = remember(file, markdownRenderConfig, chunkLoaderConfig) {
        MarkdownChunkLoader(file, markdownRenderConfig, chunkLoaderConfig)
    }

    // Use collectAsState to collect Flow state
    val nodes by markdownChunkLoader.nodes.collectAsState()

    val prefetchStrategy = remember {
        LazyListPrefetchStrategy(nestedPrefetchItemCount)
    }
    // Create LazyListState to track scroll state
    val listState = rememberLazyListState(
        prefetchStrategy = prefetchStrategy
    )

    LaunchedEffect(file) {
        markdownChunkLoader.loadInitialChunks()
    }

    // Listen to scroll state changes, get first and last visible nodes and scroll direction
    LaunchedEffect(listState, nodes) {
        var previousScrollOffset = 0

        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val firstVisibleIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val currentScrollOffset = listState.firstVisibleItemScrollOffset

            Triple(firstVisibleIndex, lastVisibleIndex, currentScrollOffset)
        }
            .distinctUntilChanged()
            .collect { (firstIndex, lastIndex, currentScrollOffset) ->
                // Calculate scroll direction in collect to avoid modifying state in snapshotFlow
                val scrollDirection = when {
                    currentScrollOffset > previousScrollOffset -> ScrollDirection.DOWN
                    currentScrollOffset < previousScrollOffset -> ScrollDirection.UP
                    else -> ScrollDirection.NONE
                }

                previousScrollOffset = currentScrollOffset

                if (firstIndex != null && lastIndex != null &&
                    firstIndex < nodes.size && lastIndex < nodes.size
                ) {
                    val firstVisibleNode = nodes[firstIndex]
                    val lastVisibleNode = nodes[lastIndex]
                    markdownChunkLoader.loadMoreChunksIfNeeded(
                        firstVisibleNode,
                        lastVisibleNode,
                        scrollDirection
                    )
                }
            }
    }

    MarkdownLocalProviders(markdownRenderConfig, showNotSupportedText, linkInteractionListener) {
        LazyColumn(
            modifier = modifier,
            state = listState,
        ) {
            items(nodes, key = { System.identityHashCode(it) }) { node ->
                MarkdownBlock(
                    node = node,
                    modifier = Modifier
                )
            }
        }
    }
}

/**
 * Scroll direction enumeration
 */
enum class ScrollDirection {
    UP, DOWN, NONE
}