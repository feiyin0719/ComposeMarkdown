package com.iffly.compose.markdown

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.dispatcher.MarkdownThreadPool
import com.iffly.compose.markdown.render.TextSizeConstraints
import com.iffly.compose.markdown.render.rememberMarkdownAnnotatedStringResult
import com.iffly.compose.markdown.widget.richtext.RichText
import com.vladsch.flexmark.util.ast.Node
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Text-based Markdown rendering component.
 *
 * Unlike [MarkdownView] which renders each block as a separate composable in a [Column],
 * this component renders the entire Markdown document through a single [RichText] composable.
 * Text-containing blocks (Paragraph, Heading) are merged into a single [AnnotatedString],
 * enabling cross-paragraph text selection. Non-text blocks (code blocks, block quotes, lists, etc.)
 * are rendered as [EmbeddedRichTextInlineContent][RichTextInlineContent.EmbeddedRichTextInlineContent]
 * using existing [IBlockRenderer][com.iffly.compose.markdown.render.IBlockRenderer] implementations.
 *
 * This is a **complementary** approach to [MarkdownView], best suited for documents where
 * continuous text selection across paragraphs is desired.
 *
 * This version parses the Markdown content synchronously within a remember block.
 *
 * @param content The Markdown content as a String.
 * @param markdownRenderConfig Configuration for rendering the Markdown. Create custom instances with [remember].
 * @param modifier Modifier to be applied to the Markdown text.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
 * @param renderDependencies Dependencies available to custom renderers and node string builders.
 * @param overflow How visual overflow should be handled.
 * @param softWrap Whether the text should wrap softly.
 * @param textAlign The alignment of the text.
 * @param maxLines The maximum number of lines to display.
 * @param minLines The minimum number of lines to display.
 * @param letterSpacing The spacing between letters.
 * @param textDecoration The text decoration to apply.
 * @param onTextLayout Callback invoked when the text layout is computed.
 * @param isStreaming Whether [content] is an append-only partial stream. Setting it to `false`
 * forces a final full parse.
 * @param streamingMarkdownParser Parser used for incremental tail updates while streaming.
 * @param onError Composable to display in case of an error during parsing.
 *
 * @see MarkdownView
 */
@Composable
fun MarkdownText(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    isStreaming: Boolean = false,
    streamingMarkdownParser: StreamingMarkdownParser = DefaultStreamingMarkdownParser,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {
    val parser = markdownRenderConfig.parser
    val markdownState =
        rememberMarkdownState(
            content = content,
            parser = parser,
            isStreaming = isStreaming,
            streamingParser = streamingMarkdownParser,
        )

    when (markdownState) {
        is MarkdownState.Loading -> {}

        is MarkdownState.Success -> {
            MarkdownText(
                node = markdownState.node,
                markdownRenderConfig = markdownRenderConfig,
                modifier = modifier,
                showNotSupportedText = showNotSupportedText,
                actionHandler = actionHandler,
                renderDependencies = renderDependencies,
                overflow = overflow,
                softWrap = softWrap,
                textAlign = textAlign,
                maxLines = maxLines,
                minLines = minLines,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                onTextLayout = onTextLayout,
            )
        }

        is MarkdownState.Error -> {
            onError?.invoke(markdownState.throwable)
        }
    }
}

/**
 * Text-based Markdown rendering component with asynchronous parsing.
 *
 * Unlike [MarkdownView] which renders each block as a separate composable in a [Column],
 * this component renders the entire Markdown document through a single [RichText] composable.
 *
 * This version parses the Markdown content asynchronously using a coroutine.
 *
 * @param content The Markdown content as a String.
 * @param markdownRenderConfig Configuration for rendering the Markdown. Create custom instances with [remember].
 * @param modifier Modifier to be applied to the Markdown text.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
 * @param renderDependencies Dependencies available to custom renderers and node string builders.
 * @param overflow How visual overflow should be handled.
 * @param softWrap Whether the text should wrap softly.
 * @param textAlign The alignment of the text.
 * @param maxLines The maximum number of lines to display.
 * @param minLines The minimum number of lines to display.
 * @param letterSpacing The spacing between letters.
 * @param textDecoration The text decoration to apply.
 * @param onTextLayout Callback invoked when the text layout is computed.
 * @param parseDispatcher Optional dispatcher for parsing. Defaults to a background thread pool.
 * @param isStreaming Whether [content] is an append-only partial stream. Setting it to `false`
 * forces a final full parse.
 * @param streamingMarkdownParser Parser used for incremental tail updates while streaming.
 * @param onLoading Composable to display while loading.
 * @param onError Composable to display in case of an error during parsing.
 *
 * @see MarkdownView
 */
@Composable
fun MarkdownText(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    parseDispatcher: CoroutineDispatcher? = null,
    isStreaming: Boolean = false,
    streamingMarkdownParser: StreamingMarkdownParser = DefaultStreamingMarkdownParser,
    onLoading: (@Composable () -> Unit)? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {
    val parser = markdownRenderConfig.parser
    val markdownState by
        rememberAsyncMarkdownState(
            content = content,
            parser = parser,
            isStreaming = isStreaming,
            streamingParser = streamingMarkdownParser,
            dispatcher = parseDispatcher ?: MarkdownThreadPool.dispatcher,
        )

    when (val state = markdownState) {
        is MarkdownState.Loading -> {
            onLoading?.invoke()
        }

        is MarkdownState.Success -> {
            MarkdownText(
                node = state.node,
                markdownRenderConfig = markdownRenderConfig,
                modifier = modifier,
                showNotSupportedText = showNotSupportedText,
                actionHandler = actionHandler,
                renderDependencies = renderDependencies,
                overflow = overflow,
                softWrap = softWrap,
                textAlign = textAlign,
                maxLines = maxLines,
                minLines = minLines,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                onTextLayout = onTextLayout,
            )
        }

        is MarkdownState.Error -> {
            onError?.invoke(state.throwable)
        }
    }
}

/**
 * Text-based Markdown rendering component for a pre-parsed AST node.
 *
 * Unlike [MarkdownView] which renders each block as a separate composable in a [Column],
 * this component renders the entire Markdown document through a single [RichText] composable.
 *
 * @param node The root node of the parsed Markdown content.
 * @param markdownRenderConfig Configuration for rendering the Markdown. Create custom instances with [remember].
 * @param modifier Modifier to be applied to the Markdown text.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
 * @param renderDependencies Dependencies available to custom renderers and node string builders.
 * @param overflow How visual overflow should be handled.
 * @param softWrap Whether the text should wrap softly.
 * @param textAlign The alignment of the text.
 * @param maxLines The maximum number of lines to display.
 * @param minLines The minimum number of lines to display.
 * @param letterSpacing The spacing between letters.
 * @param textDecoration The text decoration to apply.
 * @param onTextLayout Callback invoked when the text layout is computed.
 *
 * @see MarkdownView
 */
@Composable
fun MarkdownText(
    node: Node,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val textModeRegistry =
        remember(markdownRenderConfig.renderRegistry) {
            markdownRenderConfig.renderRegistry.textModeRegistry()
        }
    MarkdownLocalProviders(
        markdownRenderConfig = markdownRenderConfig,
        showNotSupportedText = showNotSupportedText,
        actionHandler = actionHandler,
        renderDependencies = renderDependencies,
        renderRegistry = textModeRegistry,
    ) {
        MarkdownTextContent(
            node = node,
            modifier = modifier,
            overflow = overflow,
            softWrap = softWrap,
            textAlign = textAlign,
            maxLines = maxLines,
            minLines = minLines,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            onTextLayout = onTextLayout,
        )
    }
}

/**
 * Internal composable that builds a document-level [AnnotatedString][androidx.compose.ui.text.AnnotatedString]
 * and renders it via [RichText].
 *
 * All node types are handled uniformly through [markdownText]: text blocks
 * (Paragraph, Heading) are merged directly into the AnnotatedString via their
 * registered [IInlineNodeStringBuilder][com.iffly.compose.markdown.render.IInlineNodeStringBuilder],
 * while other blocks are wrapped as embedded inline content by
 * [BlockRendererInlineStringBuilder][com.iffly.compose.markdown.render.BlockRendererInlineStringBuilder]
 * (lazily registered via [RenderRegistry.textModeRegistry][com.iffly.compose.markdown.render.RenderRegistry.textModeRegistry]).
 */
@Composable
private fun MarkdownTextContent(
    node: Node,
    modifier: Modifier = Modifier,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    BoxWithConstraints(modifier = modifier) {
        val theme = currentTheme()
        val textSizeConstraints =
            remember(maxWidth, maxHeight, minWidth, minHeight) {
                TextSizeConstraints(
                    maxWidth = maxWidth,
                    maxHeight = maxHeight,
                    minWidth = minWidth,
                    minHeight = minHeight,
                )
            }
        val result =
            rememberMarkdownAnnotatedStringResult(
                node = node,
                textSizeConstraints = textSizeConstraints,
                textAlign = textAlign ?: TextAlign.Start,
            )
        val richTextOnTextLayout =
            remember(onTextLayout) {
                { _: Int, result: TextLayoutResult -> onTextLayout(result) }
            }

        RichText(
            text = result.text,
            inlineContent = result.inlineContent,
            modifier =
                Modifier
                    .wrapContentHeight()
                    .widthIn(minWidth, maxWidth),
            style = theme.textStyle,
            overflow = overflow,
            softWrap = softWrap,
            textAlign = textAlign,
            maxLines = maxLines,
            minLines = minLines,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            onTextLayout = richTextOnTextLayout,
        )
    }
}
