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

@Composable
fun MarkdownText(
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
                        (value as? MarkdownInlineView.MarkdownInlineTextContent)?.let {
                            key to value.toInlineTextContent()
                        }
                    }.toMap()
            }

        val standaloneInlineTextContent =
            remember(inlineContent) {
                inlineContent
                    .mapNotNull { (key, value) ->
                        (value as? MarkdownInlineView.MarkdownStandaloneInlineView)?.let {
                            key to it.toStandaloneInlineTextContent()
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
            standaloneInlineTextContent = standaloneInlineTextContent.toImmutableMap(),
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
            val buildNodeAnnotatedString = renderRegistry.getInlineNodeStringBuilder(node::class.java)
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
