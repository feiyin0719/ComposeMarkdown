package com.iffly.compose.markdown

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.iffly.compose.markdown.dispatcher.MarkdownThreadPool
import com.iffly.compose.markdown.parser.ParserFactory
import com.iffly.compose.markdown.render.MarkdownContent
import com.iffly.compose.markdown.util.MarkdownPreview
import kotlinx.coroutines.launch
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
    modifier: Modifier = Modifier,
    onLoading: (@Composable () -> Unit)? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {

    val parser = remember {
        ParserFactory().build()
    }

    var markdownState by remember { mutableStateOf<MarkdownState>(MarkdownState.Loading) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(content) {
        markdownState = MarkdownState.Loading

        coroutineScope.launch {
            try {
                val parsedNode = withContext(MarkdownThreadPool.dispatcher) {
                    parser.parse(content)
                }
                markdownState = MarkdownState.Success(parsedNode)
            } catch (e: Exception) {
                markdownState = MarkdownState.Error(e)
            }
        }
    }

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

@MarkdownPreview
@Composable
private fun MarkdownViewPreview() {
    MarkdownView(
        content = "# Hello, Markdown!\n\nThis is a **preview** of the Markdown view.",
        onLoading = { Text("test") },
        onError = { error -> /* Show error message */ }
    )
}