package com.iffly.compose.markdown.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.iffly.compose.markdown.config.currentLinkClickListener
import com.iffly.compose.markdown.config.currentRenderRegistry
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.iffly.compose.markdown.config.isShowNotSupported
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.util.contentText
import com.iffly.compose.markdown.widget.BasicText
import com.vladsch.flexmark.util.ast.Node

@Composable
fun MarkdownText(
    parent: Node,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    textStyle: TextStyle? = null,
) {
    val typographyStyle = currentTypographyStyle()
    val renderRegistry = currentRenderRegistry()
    val linkInteractionListener = currentLinkClickListener()
    val isShowNotSupported = isShowNotSupported()
    val (text, inlineContent) = remember(
        parent,
        typographyStyle,
        renderRegistry,
        isShowNotSupported,
        linkInteractionListener,
    ) {
        markdownText(
            parent,
            typographyStyle,
            renderRegistry,
            linkInteractionListener,
            1,
            isShowNotSupported,
        )
    }
    val inlineContentMap = remember(inlineContent) {
        inlineContent.mapValues {
            it.value.toInlineTextContent()
        }
    }

    BasicText(
        text = text,
        inlineContent = inlineContentMap,
        modifier = modifier,
        textAlign = textAlign,
        style = textStyle ?: typographyStyle.textStyle
    )
}

fun markdownText(
    node: Node,
    typographyStyle: TypographyStyle,
    renderRegistry: RenderRegistry,
    linkInteractionListener: LinkInteractionListener? = null,
    indentLevel: Int = 0,
    isShowNotSupported: Boolean,
): Pair<AnnotatedString, Map<String, MarkdownInlineTextContent>> {
    val inlineContentMap = mutableMapOf<String, MarkdownInlineTextContent>()

    val annotatedString = buildAnnotatedString {
        val buildNodeAnnotatedString = renderRegistry.getInlineNodeStringBuilder(node::class.java)
        if (buildNodeAnnotatedString != null) {
            buildNodeAnnotatedString.buildMarkdownInlineNodeString(
                node,
                inlineContentMap,
                typographyStyle,
                indentLevel,
                linkInteractionListener,
                renderRegistry,
                isShowNotSupported,
                this,
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