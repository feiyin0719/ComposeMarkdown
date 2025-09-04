package com.iffly.compose.markdown

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.iffly.compose.markdown.render.LocalBlockRenderersProvider
import com.iffly.compose.markdown.render.LocalInlineNodeStringBuildersProvider
import com.iffly.compose.markdown.render.LocalLinkClickListenerProvider
import com.iffly.compose.markdown.render.MarkdownContent
import com.iffly.compose.markdown.style.LocalTypographyStyleProvider
import com.iffly.compose.markdown.util.MarkdownPreview
import kotlinx.coroutines.withContext
import org.commonmark.node.Node

sealed class MarkdownState {
    object Loading : MarkdownState()
    data class Success(val node: Node) : MarkdownState()
    data class Error(val exception: Throwable) : MarkdownState()
}

@Composable
fun MarkdownView(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    linkInteractionListener: LinkInteractionListener? = null,
    onLoading: (@Composable () -> Unit)? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {

    val parser by rememberUpdatedState(markdownRenderConfig.parser)
    val linkInteractionListener by rememberUpdatedState(linkInteractionListener)
    val inlineNodeStringBuilders by
    rememberUpdatedState(markdownRenderConfig.inlineNodeStringBuilders)
    val blockRenderers by rememberUpdatedState(markdownRenderConfig.blockRenderers)
    val typographyStyle by rememberUpdatedState(markdownRenderConfig.typographyStyle)


    var markdownState by remember { mutableStateOf<MarkdownState>(MarkdownState.Loading) }

    LaunchedEffect(content) {
        markdownState = MarkdownState.Loading
        try {
            val parsedNode = withContext(MarkdownThreadPool.dispatcher) {
                parser.parse(content)
            }
            markdownState = MarkdownState.Success(parsedNode)
        } catch (e: Exception) {
            markdownState = MarkdownState.Error(e)
        }
    }

    CompositionLocalProvider(
        LocalLinkClickListenerProvider provides linkInteractionListener,
        LocalInlineNodeStringBuildersProvider provides inlineNodeStringBuilders,
        LocalBlockRenderersProvider provides blockRenderers,
        LocalTypographyStyleProvider provides typographyStyle,
    ) {
        when (val state = markdownState) {
            is MarkdownState.Loading -> {
                onLoading?.invoke()
            }

            is MarkdownState.Success -> {
                MarkdownContent(state.node, modifier)
            }

            is MarkdownState.Error -> {
                onError?.invoke(state.exception)
            }
        }
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