package com.iffly.compose.markdown

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.config.currentActionHandler
import com.iffly.compose.markdown.config.currentRenderRegistry
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.config.isShowNotSupported
import com.iffly.compose.markdown.dispatcher.MarkdownThreadPool
import com.iffly.compose.markdown.render.MarkdownContent
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.NodeStringBuilderContext
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.render.TextSizeConstraints
import com.iffly.compose.markdown.render.markdownText
import com.iffly.compose.markdown.render.rememberNodeStringBuilderContext
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.widget.richtext.RichText
import com.iffly.compose.markdown.widget.richtext.RichTextInlineContent
import com.iffly.compose.markdown.widget.richtext.appendStandaloneInlineTextContent
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.util.ast.Node
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Text-based Markdown rendering component.
 *
 * Unlike [MarkdownView] which renders each block as a separate composable in a [Column],
 * this component renders the entire Markdown document through a single [RichText] composable.
 * Text-containing blocks (Paragraph, Heading) are merged into a single [AnnotatedString],
 * enabling cross-paragraph text selection. Non-text blocks (code blocks, block quotes, lists, etc.)
 * are rendered as [StandaloneInlineContent][RichTextInlineContent.StandaloneInlineContent]
 * using existing [IBlockRenderer][com.iffly.compose.markdown.render.IBlockRenderer] implementations.
 *
 * This is a **complementary** approach to [MarkdownView], best suited for documents where
 * continuous text selection across paragraphs is desired.
 *
 * This version parses the Markdown content synchronously within a remember block.
 *
 * @param content The Markdown content as a String.
 * @param markdownRenderConfig Configuration for rendering the Markdown.
 * @param modifier Modifier to be applied to the Markdown text.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
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
    onError: (@Composable (Throwable) -> Unit)? = null,
) {
    val parser = markdownRenderConfig.parser
    val markdownState =
        remember(content, parser) {
            try {
                MarkdownState.Success(parser.parse(content))
            } catch (e: Exception) {
                MarkdownState.Error(e)
            }
        }

    when (markdownState) {
        is MarkdownState.Loading -> {}

        is MarkdownState.Success -> {
            MarkdownText(
                node = markdownState.node,
                markdownRenderConfig = markdownRenderConfig,
                modifier = modifier,
                showNotSupportedText = showNotSupportedText,
                actionHandler = actionHandler,
            )
        }

        is MarkdownState.Error -> {
            onError?.invoke(markdownState.exception)
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
 * @param markdownRenderConfig Configuration for rendering the Markdown.
 * @param modifier Modifier to be applied to the Markdown text.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
 * @param parseDispatcher Optional dispatcher for parsing. Defaults to a background thread pool.
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
    parseDispatcher: CoroutineDispatcher? = null,
    onLoading: (@Composable () -> Unit)? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {
    val parser by rememberUpdatedState(markdownRenderConfig.parser)
    var markdownState by remember { mutableStateOf<MarkdownState>(MarkdownState.Loading) }

    LaunchedEffect(content, parser) {
        markdownState = MarkdownState.Loading
        try {
            val parsedNode =
                withContext(parseDispatcher ?: MarkdownThreadPool.dispatcher) {
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
            MarkdownText(
                node = state.node,
                markdownRenderConfig = markdownRenderConfig,
                modifier = modifier,
                showNotSupportedText = showNotSupportedText,
                actionHandler = actionHandler,
            )
        }

        is MarkdownState.Error -> {
            onError?.invoke(state.exception)
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
 * @param markdownRenderConfig Configuration for rendering the Markdown.
 * @param modifier Modifier to be applied to the Markdown text.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
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
) {
    MarkdownLocalProviders(markdownRenderConfig, showNotSupportedText, actionHandler) {
        MarkdownTextContent(node, modifier)
    }
}

/**
 * Internal composable that builds a document-level [AnnotatedString] and renders it
 * via [RichText]. Text blocks (Paragraph, Heading) are merged directly into the
 * AnnotatedString; other blocks are wrapped as [StandaloneInlineContent]
 * [RichTextInlineContent.StandaloneInlineContent] delegating to their
 * [IBlockRenderer][com.iffly.compose.markdown.render.IBlockRenderer].
 */
@Composable
private fun MarkdownTextContent(
    node: Node,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val theme = currentTheme()
        val renderRegistry = currentRenderRegistry()
        val actionHandler = currentActionHandler()
        val isShowNotSupported = isShowNotSupported()
        val nodeStringBuilderContext =
            rememberNodeStringBuilderContext(
                textSizeConstraints =
                    TextSizeConstraints(
                        maxWidth = maxWidth,
                        maxHeight = maxHeight,
                        minWidth = minWidth,
                        minHeight = minHeight,
                    ),
                textAlign = TextAlign.Start,
            )

        val (text, inlineContentMap) =
            remember(
                node,
                theme,
                renderRegistry,
                isShowNotSupported,
                actionHandler,
                nodeStringBuilderContext,
            ) {
                buildDocumentAnnotatedString(
                    document = node,
                    theme = theme,
                    renderRegistry = renderRegistry,
                    actionHandler = actionHandler,
                    isShowNotSupported = isShowNotSupported,
                    nodeStringBuilderContext = nodeStringBuilderContext,
                )
            }

        val richTextInlineContent =
            remember(inlineContentMap) {
                inlineContentMap
                    .mapNotNull { (key, value) ->
                        if (value is MarkdownInlineView.MarkdownRichTextInlineContent) {
                            key to value.inlineContent
                        } else {
                            null
                        }
                    }.toMap()
            }

        RichText(
            text = text,
            inlineContent = richTextInlineContent.toImmutableMap(),
            modifier =
                Modifier
                    .wrapContentHeight()
                    .widthIn(minWidth, maxWidth),
            style = theme.textStyle,
        )
    }
}

/**
 * Walks the document AST and builds a single [AnnotatedString] with inline content.
 *
 * - [Paragraph] and [Heading] nodes: their inline content is built via [markdownText] and
 *   appended directly into the AnnotatedString (enabling cross-paragraph text selection).
 * - Other block nodes: inserted as [StandaloneInlineContent]
 *   [RichTextInlineContent.StandaloneInlineContent] placeholders.
 *   Their rendering delegates to [MarkdownBlock] which uses the registered
 *   [IBlockRenderer][com.iffly.compose.markdown.render.IBlockRenderer].
 */
private fun buildDocumentAnnotatedString(
    document: Node,
    theme: MarkdownTheme,
    renderRegistry: RenderRegistry,
    actionHandler: ActionHandler?,
    isShowNotSupported: Boolean,
    nodeStringBuilderContext: NodeStringBuilderContext,
): Pair<AnnotatedString, Map<String, MarkdownInlineView>> {
    val inlineContentMap = mutableMapOf<String, MarkdownInlineView>()

    val annotatedString =
        buildAnnotatedString {
            var child = document.firstChild
            var isFirst = true

            while (child != null) {
                if (!isFirst) {
                    append('\n')
                }

                when (child) {
                    is Paragraph, is Heading -> {
                        val (childText, childInlines) =
                            markdownText(
                                node = child,
                                markdownTheme = theme,
                                renderRegistry = renderRegistry,
                                actionHandler = actionHandler,
                                indentLevel = 1,
                                isShowNotSupported = isShowNotSupported,
                                nodeStringBuilderContext = nodeStringBuilderContext,
                            )
                        append(childText)
                        inlineContentMap.putAll(childInlines)
                    }

                    else -> {
                        val blockId = "block_${System.identityHashCode(child)}"
                        val blockNode = child
                        appendStandaloneInlineTextContent(blockId)
                        inlineContentMap[blockId] =
                            MarkdownInlineView.MarkdownRichTextInlineContent(
                                RichTextInlineContent.StandaloneInlineContent(
                                    modifier = Modifier.fillMaxWidth(),
                                ) { mod ->
                                    MarkdownContent(
                                        node = blockNode,
                                        modifier = mod,
                                    )
                                },
                            )
                    }
                }

                isFirst = false
                child = child.next
            }
        }

    return Pair(annotatedString, inlineContentMap)
}
