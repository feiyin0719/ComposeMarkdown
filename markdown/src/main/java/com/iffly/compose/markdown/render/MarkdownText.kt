package com.iffly.compose.markdown.render

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.config.currentInlineNodeStringBuilders
import com.iffly.compose.markdown.config.currentLinkClickListener
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.iffly.compose.markdown.config.isShowNotSupported
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.util.contentText
import com.iffly.compose.markdown.util.getNodeParagraphStyle
import com.iffly.compose.markdown.util.getNodeSpanStyle
import com.iffly.compose.markdown.widget.BasicText
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.util.ast.Node


object ParagraphRenderer : IBlockRenderer<Paragraph> {
    @Composable
    override fun Invoke(
        node: Paragraph,
        modifier: Modifier
    ) {
        MarkdownText(parent = node, modifier = modifier)
    }
}

object HeadingRenderer : IBlockRenderer<Heading> {
    @Composable
    override fun Invoke(
        node: Heading,
        modifier: Modifier
    ) {
        MarkdownText(parent = node, modifier = modifier)
    }
}

object OrderedListRenderer : IBlockRenderer<OrderedList> {
    @Composable
    override fun Invoke(
        node: OrderedList,
        modifier: Modifier
    ) {
        MarkdownText(parent = node, modifier = modifier)
    }
}

object BulletListRenderer : IBlockRenderer<BulletList> {
    @Composable
    override fun Invoke(
        node: BulletList,
        modifier: Modifier
    ) {
        MarkdownText(parent = node, modifier = modifier)
    }
}

@Composable
fun MarkdownText(
    parent: Node,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    val typographyStyle = currentTypographyStyle()
    val inlineNodeStringBuilders = currentInlineNodeStringBuilders()
    val linkInteractionListener = currentLinkClickListener()
    val isShowNotSupported = isShowNotSupported()
    val (text, inlineContent) = remember(
        parent,
        typographyStyle,
        inlineNodeStringBuilders,
        isShowNotSupported,
    ) {
        markdownText(
            parent,
            typographyStyle,
            inlineNodeStringBuilders,
            linkInteractionListener,
            1,
            isShowNotSupported,
        )
    }
    val inlineContentMap = remember(inlineContent) {
        inlineContent.map {
            it.key to InlineTextContent(
                placeholder = it.value.placeholder,
                children = it.value.children
            )
        }.toMap()
    }

    BasicText(
        text = text,
        inlineContent = inlineContentMap,
        modifier = modifier,
        textAlign = textAlign,
        style = typographyStyle.textStyle ?: LocalTextStyle.current
    )
}

fun markdownText(
    node: Node,
    typographyStyle: TypographyStyle,
    inlineNodeStringBuilders: InlineNodeStringBuilders,
    linkInteractionListener: LinkInteractionListener? = null,
    indentLevel: Int = 0,
    isShowNotSupported: Boolean,
): Pair<AnnotatedString, Map<String, MarkdownInlineTextContent>> {
    val inlineContentMap = mutableMapOf<String, MarkdownInlineTextContent>()

    val annotatedString = buildAnnotatedString {
        val style: SpanStyle = typographyStyle.getNodeSpanStyle(node)
        val paragraphStyle = typographyStyle.getNodeParagraphStyle(node)
        withStyle(paragraphStyle) {
            withStyle(style) {
                buildMarkdownAnnotatedString(
                    node,
                    indentLevel,
                    inlineContentMap,
                    typographyStyle,
                    inlineNodeStringBuilders,
                    linkInteractionListener,
                    isShowNotSupported,
                )
            }
        }
    }

    return Pair(annotatedString, inlineContentMap)
}

fun AnnotatedString.Builder.buildMarkdownAnnotatedString(
    parent: Node,
    indentLevel: Int = 1,
    inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
    typographyStyle: TypographyStyle,
    inlineNodeStringBuilders: InlineNodeStringBuilders,
    linkInteractionListener: LinkInteractionListener? = null,
    isShowNotSupported: Boolean,
) {
    var node = parent.firstChild
    while (node != null) {
        val customBuilder =
            inlineNodeStringBuilders[node::class.java]
        customBuilder?.buildMarkdownInlineNodeString(
            node,
            inlineContentMap,
            typographyStyle,
            indentLevel,
            linkInteractionListener,
            inlineNodeStringBuilders,
            isShowNotSupported,
            this,

            ) ?: run {
            if (isShowNotSupported) {
                append("[Unsupported: ${node::class.java.simpleName}]")
            } else {
                append(node.contentText())
            }
        }
        node = node.next
    }
}

data class MarkdownInlineTextContent(
    val placeholder: Placeholder,
    val children: @Composable (String) -> Unit
)