package com.iffly.compose.markdown.render

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.config.currentActionHandler
import com.iffly.compose.markdown.config.currentRenderRegistry
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.config.isShowNotSupported
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.contentText
import com.iffly.compose.markdown.widget.richtext.RichText
import com.vladsch.flexmark.util.ast.Node
import kotlinx.collections.immutable.toImmutableMap

/**
 * A renderer for markdown text nodes.
 */
fun interface MarkdownTextRenderer {
    /**
     * @param parent The root node of the markdown content to be rendered.
     * @param modifier The modifier to be applied to the rich text.
     * @param textAlign The alignment of the text.
     * @param textStyle The style to be applied to the text.
     */
    @Composable
    operator fun invoke(
        parent: Node,
        modifier: Modifier,
        textAlign: TextAlign,
        textStyle: TextStyle?,
    )
}

/**
 * A Composable that renders markdown content as rich text.
 * @param parent The root node of the markdown content to be rendered.
 * @param modifier The modifier to be applied to the rich text.
 * @param textAlign The alignment of the text.
 * @param textStyle The style to be applied to the text.
 */
@Composable
fun MarkdownText(
    parent: Node,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    textStyle: TextStyle? = null,
) {
    val renderRegistry = currentRenderRegistry()
    renderRegistry.markdownTextRenderer?.invoke(
        parent = parent,
        modifier = modifier,
        textAlign = textAlign,
        textStyle = textStyle,
    ) ?: DefaultMarkdownText(
        parent = parent,
        modifier = modifier,
        textAlign = textAlign,
        textStyle = textStyle,
    )
}

@Composable
private fun DefaultMarkdownText(
    parent: Node,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    textStyle: TextStyle? = null,
) {
    BoxWithConstraints(modifier = modifier) {
        val theme = currentTheme()
        val renderRegistry = currentRenderRegistry()
        val actionHandler = currentActionHandler()
        val isShowNotSupported = isShowNotSupported()
        val measureContext =
            rememberTextMeasureContext(
                maxTextWidth = this.maxWidth,
            )
        val (text, inlineContent) =
            remember(
                parent,
                theme,
                renderRegistry,
                isShowNotSupported,
                actionHandler,
                measureContext,
            ) {
                markdownText(
                    parent,
                    theme,
                    renderRegistry,
                    actionHandler,
                    1,
                    isShowNotSupported,
                    measureContext,
                )
            }
        val inlineContentMap =
            remember(inlineContent) {
                inlineContent
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
            inlineContent = inlineContentMap.toImmutableMap(),
            modifier =
                Modifier
                    .wrapContentHeight()
                    .widthIn(minWidth, maxWidth),
            textAlign = textAlign,
            style = textStyle ?: theme.textStyle,
        )
    }
}

@Composable
private fun rememberTextMeasureContext(maxTextWidth: Dp): TextMeasureContext {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    return remember(density, textMeasurer, maxTextWidth) {
        TextMeasureContext(
            density = density,
            textMeasurer = textMeasurer,
            maxTextWidth = maxTextWidth,
        )
    }
}

fun markdownText(
    node: Node,
    markdownTheme: MarkdownTheme,
    renderRegistry: RenderRegistry,
    actionHandler: ActionHandler? = null,
    indentLevel: Int = 0,
    isShowNotSupported: Boolean,
    measureContext: TextMeasureContext,
): Pair<AnnotatedString, Map<String, MarkdownInlineView>> {
    val inlineContentMap = mutableMapOf<String, MarkdownInlineView>()

    val annotatedString =
        buildAnnotatedString {
            val buildNodeAnnotatedString =
                renderRegistry.getInlineNodeStringBuilder(node::class.java)
            if (buildNodeAnnotatedString != null) {
                buildNodeAnnotatedString.buildMarkdownInlineNodeString(
                    node,
                    inlineContentMap,
                    markdownTheme,
                    indentLevel,
                    actionHandler,
                    renderRegistry,
                    isShowNotSupported,
                    this,
                    measureContext,
                )
            } else {
                if (isShowNotSupported) {
                    append("[Unsupported: ${node::class.java.simpleName}]")
                } else {
                    append(node.contentText())
                }
            }
        }

    return Pair(annotatedString, inlineContentMap)
}
