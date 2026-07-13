package com.iffly.compose.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.sequence.BasedSequence
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Parses the replaceable tail of an append-only Markdown stream.
 *
 * [source] contains the complete current Markdown input. [startOffset] is the start of the source
 * line containing the previous document's last top-level block. Implementations must return a
 * document parsed from that offset through the end of [source], with node offsets still expressed
 * in the coordinate space of [source].
 */
fun interface StreamingMarkdownParser {
    fun parse(
        parser: Parser,
        source: BasedSequence,
        startOffset: Int,
    ): Document
}

/** Default offset-preserving Flexmark streaming parser. */
object DefaultStreamingMarkdownParser : StreamingMarkdownParser {
    override fun parse(
        parser: Parser,
        source: BasedSequence,
        startOffset: Int,
    ): Document = parser.parse(source.subSequence(startOffset, source.length))
}

internal sealed interface MarkdownState {
    data object Loading : MarkdownState

    data class Success(
        val node: Document,
    ) : MarkdownState

    data class Error(
        val throwable: Throwable,
    ) : MarkdownState
}

private data class MarkdownSnapshot(
    val content: String,
    val document: Document,
)

private sealed interface MarkdownParseRequest {
    val content: String

    data class Reuse(
        override val content: String,
        val document: Document,
    ) : MarkdownParseRequest

    data class Full(
        override val content: String,
    ) : MarkdownParseRequest

    data class Tail(
        override val content: String,
        val source: BasedSequence,
        val startOffset: Int,
        val previousDocument: Document,
    ) : MarkdownParseRequest
}

private class StreamingMarkdownParseSession {
    private var snapshot: MarkdownSnapshot? = null

    fun createRequest(
        content: String,
        isStreaming: Boolean,
    ): MarkdownParseRequest {
        if (!isStreaming) return MarkdownParseRequest.Full(content)

        val previous = snapshot ?: return MarkdownParseRequest.Full(content)
        if (content == previous.content) {
            return MarkdownParseRequest.Reuse(content, previous.document)
        }
        if (!content.startsWith(previous.content)) {
            return MarkdownParseRequest.Full(content)
        }

        val lastBlock = previous.document.lastChild ?: return MarkdownParseRequest.Full(content)
        val startOffset = lastBlock.startOfLine
        if (startOffset !in 0..content.length) return MarkdownParseRequest.Full(content)

        return MarkdownParseRequest.Tail(
            content = content,
            source = BasedSequence.of(content),
            startOffset = startOffset,
            previousDocument = previous.document,
        )
    }

    fun complete(
        request: MarkdownParseRequest,
        parsedDocument: Document?,
        parser: Parser,
    ): Document {
        val document =
            when (request) {
                is MarkdownParseRequest.Reuse -> {
                    request.document
                }

                is MarkdownParseRequest.Full -> {
                    requireNotNull(parsedDocument)
                }

                is MarkdownParseRequest.Tail -> {
                    mergeTail(
                        request = request,
                        tailDocument = requireNotNull(parsedDocument),
                        parser = parser,
                    )
                }
            }
        snapshot = MarkdownSnapshot(request.content, document)
        return document
    }
}

private fun MarkdownParseRequest.parse(
    parser: Parser,
    streamingParser: StreamingMarkdownParser,
): Document? =
    when (this) {
        is MarkdownParseRequest.Reuse -> null
        is MarkdownParseRequest.Full -> parser.parse(content)
        is MarkdownParseRequest.Tail -> streamingParser.parse(parser, source, startOffset)
    }

private fun mergeTail(
    request: MarkdownParseRequest.Tail,
    tailDocument: Document,
    parser: Parser,
): Document {
    val previousDocument = request.previousDocument
    val previousLastBlock = previousDocument.lastChild
    val document = Document(previousDocument, request.source)

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

@Composable
internal fun rememberMarkdownState(
    content: String,
    parser: Parser,
    isStreaming: Boolean,
    streamingParser: StreamingMarkdownParser,
): MarkdownState {
    val session = remember(parser, streamingParser) { StreamingMarkdownParseSession() }
    return remember(content, isStreaming, session) {
        try {
            val request = session.createRequest(content, isStreaming)
            MarkdownState.Success(
                session.complete(
                    request = request,
                    parsedDocument = request.parse(parser, streamingParser),
                    parser = parser,
                ),
            )
        } catch (throwable: Throwable) {
            MarkdownState.Error(throwable)
        }
    }
}

@Composable
internal fun rememberAsyncMarkdownState(
    content: String,
    parser: Parser,
    isStreaming: Boolean,
    streamingParser: StreamingMarkdownParser,
    dispatcher: CoroutineDispatcher,
): State<MarkdownState> {
    val session = remember(parser, streamingParser) { StreamingMarkdownParseSession() }
    return produceState<MarkdownState>(
        initialValue = MarkdownState.Loading,
        content,
        isStreaming,
        session,
        dispatcher,
    ) {
        value = MarkdownState.Loading
        value =
            try {
                val request = session.createRequest(content, isStreaming)
                val parsedDocument =
                    withContext(dispatcher) {
                        request.parse(parser, streamingParser)
                    }
                MarkdownState.Success(session.complete(request, parsedDocument, parser))
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                MarkdownState.Error(throwable)
            }
    }
}
