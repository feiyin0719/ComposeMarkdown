package com.iffly.compose.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Owns the parsing lifecycle for Markdown content that may be streaming.
 *
 * Implementations receive only the latest complete [content] and [isStreaming] state, and have full
 * control over caching, incremental parsing, fallback behavior, and final parsing.
 */
interface StreamingMarkdownParser {
    val markdownRenderConfig: MarkdownRenderConfig

    fun parse(
        content: String,
        isStreaming: Boolean,
    ): Node
}

/**
 * Default append-only Flexmark streaming parser.
 *
 * While streaming, it reparses from the source-line start of the previous final block and reuses
 * the stable prefix. A non-append update or [isStreaming] set to `false` performs a full parse.
 */
class DefaultStreamingMarkdownParser(
    override val markdownRenderConfig: MarkdownRenderConfig,
) : StreamingMarkdownParser {
    private val parser = markdownRenderConfig.parser
    private var snapshot: MarkdownSnapshot? = null

    @Synchronized
    override fun parse(
        content: String,
        isStreaming: Boolean,
    ): Document {
        if (!isStreaming) return parseFull(content)

        val previous = snapshot ?: return parseFull(content)
        if (content == previous.content) return previous.document
        if (!content.startsWith(previous.content)) return parseFull(content)

        val lastBlock = previous.document.lastChild ?: return parseFull(content)
        val startOffset = lastBlock.startOfLine
        if (startOffset !in 0..content.length) return parseFull(content)

        val source = BasedSequence.of(content)
        val tailDocument = parser.parse(source.subSequence(startOffset, source.length))
        val document = mergeTail(previous.document, tailDocument, source)
        snapshot = MarkdownSnapshot(content, document)
        return document
    }

    private fun parseFull(content: String): Document {
        val document = parser.parse(content)
        snapshot = MarkdownSnapshot(content, document)
        return document
    }

    private fun mergeTail(
        previousDocument: Document,
        tailDocument: Document,
        source: BasedSequence,
    ): Document {
        val previousLastBlock = previousDocument.lastChild
        val document = Document(previousDocument, source)

        parser.transferReferences(document, tailDocument, null)

        var child = previousDocument.firstChild
        while (child != null && child !== previousLastBlock) {
            val next = child.next
            document.appendChild(child)
            child = next
        }
        document.takeChildren(tailDocument)
        return document
    }
}

internal sealed interface MarkdownState {
    data object Loading : MarkdownState

    data class Success(
        val node: Node,
    ) : MarkdownState

    data class Error(
        val throwable: Throwable,
    ) : MarkdownState
}

private data class MarkdownSnapshot(
    val content: String,
    val document: Document,
)

@Composable
internal fun rememberMarkdownState(
    content: String,
    isStreaming: Boolean,
    parser: StreamingMarkdownParser,
): MarkdownState =
    remember(content, isStreaming, parser) {
        try {
            MarkdownState.Success(parser.parse(content, isStreaming))
        } catch (throwable: Throwable) {
            MarkdownState.Error(throwable)
        }
    }

@Composable
internal fun rememberAsyncMarkdownState(
    content: String,
    isStreaming: Boolean,
    parser: StreamingMarkdownParser,
    dispatcher: CoroutineDispatcher,
): State<MarkdownState> =
    produceState<MarkdownState>(
        initialValue = MarkdownState.Loading,
        content,
        isStreaming,
        parser,
        dispatcher,
    ) {
        if (!isStreaming || value !is MarkdownState.Success) {
            value = MarkdownState.Loading
        }
        value =
            try {
                val parsedNode =
                    withContext(dispatcher) {
                        parser.parse(content, isStreaming)
                    }
                MarkdownState.Success(parsedNode)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                MarkdownState.Error(throwable)
            }
    }
