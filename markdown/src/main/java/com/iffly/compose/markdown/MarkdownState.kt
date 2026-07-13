package com.iffly.compose.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.iffly.compose.markdown.streaming.StreamingMarkdownParser
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal sealed interface MarkdownState {
    data object Loading : MarkdownState

    data class Success(
        val node: Node,
    ) : MarkdownState

    data class Error(
        val throwable: Throwable,
    ) : MarkdownState
}

@Composable
internal fun rememberMarkdownState(
    content: String,
    isStreaming: Boolean,
    streamingParser: StreamingMarkdownParser?,
    fallbackParser: Parser,
): MarkdownState =
    remember(content, isStreaming, streamingParser, fallbackParser) {
        try {
            val node =
                if (isStreaming && streamingParser != null) {
                    streamingParser.parse(content, true)
                } else {
                    fallbackParser.parse(content)
                }
            MarkdownState.Success(node)
        } catch (throwable: Throwable) {
            MarkdownState.Error(throwable)
        }
    }

@Composable
internal fun rememberAsyncMarkdownState(
    content: String,
    isStreaming: Boolean,
    streamingParser: StreamingMarkdownParser?,
    fallbackParser: Parser,
    dispatcher: CoroutineDispatcher,
): State<MarkdownState> =
    produceState<MarkdownState>(
        initialValue = MarkdownState.Loading,
        content,
        isStreaming,
        streamingParser,
        fallbackParser,
        dispatcher,
    ) {
        if (!isStreaming || streamingParser == null || value !is MarkdownState.Success) {
            value = MarkdownState.Loading
        }
        value =
            try {
                val parsedNode =
                    withContext(dispatcher) {
                        if (isStreaming && streamingParser != null) {
                            streamingParser.parse(content, true)
                        } else {
                            fallbackParser.parse(content)
                        }
                    }
                MarkdownState.Success(parsedNode)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                MarkdownState.Error(throwable)
            }
    }
