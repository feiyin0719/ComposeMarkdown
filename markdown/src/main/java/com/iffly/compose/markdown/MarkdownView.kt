package com.iffly.compose.markdown

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkInteractionListener
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.dispatcher.MarkdownThreadPool
import com.iffly.compose.markdown.render.MarkdownContent
import com.iffly.compose.markdown.util.MarkdownPreview
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal sealed class MarkdownState {
    object Loading : MarkdownState()
    data class Success(val node: Document) : MarkdownState()
    data class Error(val exception: Throwable) : MarkdownState()
}

/**
 * A Composable function that renders Markdown content.
 * This version parses the Markdown content synchronously within a remember block.
 * Use this for small to medium-sized content where parsing time is negligible.
 * For larger content, consider using the other overload that supports asynchronous parsing.
 * @param content The Markdown content as a String.
 * @param markdownRenderConfig Configuration for rendering the Markdown.
 * @param modifier Modifier to be applied to the Markdown view.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param linkInteractionListener Listener for link interactions.
 * @param onError Composable to display in case of an error during parsing.
 */
@Composable
fun MarkdownView(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    linkInteractionListener: LinkInteractionListener? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {

    val parser = markdownRenderConfig.parser
    val markdownState = remember(content, parser) {
        try {
            MarkdownState.Success(parser.parse(content))
        } catch (e: Exception) {
            MarkdownState.Error(e)
        }
    }


    when (markdownState) {
        is MarkdownState.Loading -> {
            // Loading state is instantaneous here since parsing is done in remember
            // You can implement a more complex loading state if needed
        }

        is MarkdownState.Success -> {
            MarkdownView(
                node = markdownState.node,
                markdownRenderConfig = markdownRenderConfig,
                modifier = modifier,
                showNotSupportedText = showNotSupportedText,
                linkInteractionListener = linkInteractionListener,
            )
        }

        is MarkdownState.Error -> {
            onError?.invoke(markdownState.exception)
        }
    }

}

/**
 * A Composable function that renders Markdown content.
 * This version parses the Markdown content asynchronously using a coroutine.
 * Use this for larger content where parsing time may be significant.
 * @param content The Markdown content as a String.
 * @param markdownRenderConfig Configuration for rendering the Markdown.
 * @param modifier Modifier to be applied to the Markdown view.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param linkInteractionListener Listener for link interactions.
 * @param parseDispatcher Optional dispatcher for parsing. Defaults to a background thread pool.
 * @param onLoading Composable to display while loading.
 * @param onError Composable to display in case of an error during parsing.
 */
@Composable
fun MarkdownView(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    linkInteractionListener: LinkInteractionListener? = null,
    parseDispatcher: CoroutineDispatcher? = null,
    onLoading: (@Composable () -> Unit)? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {

    val parser by rememberUpdatedState(markdownRenderConfig.parser)
    var markdownState by remember { mutableStateOf<MarkdownState>(MarkdownState.Loading) }

    LaunchedEffect(content, parser) {
        markdownState = MarkdownState.Loading
        try {
            val parsedNode = withContext(parseDispatcher ?: MarkdownThreadPool.dispatcher) {
                parser.parse(content)
            }
            markdownState = MarkdownState.Success(parsedNode)
        } catch (e: Exception) {
            markdownState = MarkdownState.Error(e)
        }
    }


    when (val state = markdownState) {
        is MarkdownState.Loading -> {
            onLoading?.invoke()
        }

        is MarkdownState.Success -> {
            MarkdownView(
                node = state.node,
                markdownRenderConfig = markdownRenderConfig,
                modifier = modifier,
                showNotSupportedText = showNotSupportedText,
                linkInteractionListener = linkInteractionListener,
            )
        }

        is MarkdownState.Error -> {
            onError?.invoke(state.exception)
        }
    }

}

/**
 * A Composable function that renders a parsed Markdown AST node.
 * @param node The node of the parsed Markdown content.
 * @param markdownRenderConfig Configuration for rendering the Markdown.
 * @param modifier Modifier to be applied to the Markdown view.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param linkInteractionListener Listener for link interactions.
 *
 */
@Composable
fun MarkdownView(
    node: Node,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    linkInteractionListener: LinkInteractionListener? = null,
) {
    MarkdownLocalProviders(markdownRenderConfig, showNotSupportedText, linkInteractionListener) {
        MarkdownContent(node, modifier)
    }
}

@MarkdownPreview
@Composable
private fun MarkdownViewPreview() {
    MarkdownView(
        content = "# Hello, Markdown!\n\nThis is a **preview** of the Markdown view.",
        markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
        onLoading = { Text("test") },
        onError = { error -> /* Show error message */ }
    )
}