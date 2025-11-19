package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.MarkdownLinkInteractionListener
import com.iffly.compose.markdown.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.render.buildChildNodeAnnotatedString
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.util.contentText
import com.iffly.compose.markdown.util.getNodeParagraphStyle
import com.iffly.compose.markdown.util.getNodeSpanStyle
import com.iffly.compose.markdown.widget.richtext.appendStandaloneInlineTextContent
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.HardLineBreak
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.SoftLineBreak
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript
import com.vladsch.flexmark.util.ast.Node

private fun AnnotatedString.Builder.buildStyleString(
    node: Node,
    indentLevel: Int,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    typographyStyle: TypographyStyle,
    renderRegistry: RenderRegistry,
    actionHandler: ActionHandler?,
    isShowNotSupported: Boolean,
) {
    buildChildNodeAnnotatedString(
        node,
        indentLevel,
        inlineContentMap,
        typographyStyle,
        renderRegistry,
        actionHandler,
        isShowNotSupported,
    )
}

class TextNodeStringBuilder : IInlineNodeStringBuilder<Text> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Text,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        typographyStyle: TypographyStyle,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry
    ) {
        append(node.contentText())
    }
}

class ImageNodeStringBuilder : IInlineNodeStringBuilder<Image> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Image,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        typographyStyle: TypographyStyle,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry
    ) {
        val imageId = "image_${node.hashCode()}"
        inlineContentMap[imageId] = MarkdownInlineView.MarkdownStandaloneInlineView(
            modifier = Modifier
        ) { modifier ->
            MarkdownImage(node, modifier = modifier)
        }
        appendStandaloneInlineTextContent(imageId, "[${node.title ?: node.text}]")
    }
}

class SoftLineBreakNodeStringBuilder : IInlineNodeStringBuilder<SoftLineBreak> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: SoftLineBreak,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        typographyStyle: TypographyStyle,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry
    ) {
        append(" ")
    }
}

class HardLineBreakNodeStringBuilder : IInlineNodeStringBuilder<HardLineBreak> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HardLineBreak,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        typographyStyle: TypographyStyle,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry
    ) {
        append("\n")
    }
}

class StrikethroughNodeStringBuilder : CompositeChildNodeStringBuilder<Strikethrough>() {
    override fun getSpanStyle(node: Strikethrough, typographyStyle: TypographyStyle): SpanStyle? {
        return typographyStyle.strikethrough
    }
}

class SubscriptNodeStringBuilder : CompositeChildNodeStringBuilder<Subscript>() {
    override fun getSpanStyle(node: Subscript, typographyStyle: TypographyStyle): SpanStyle? {
        return typographyStyle.subscript
    }
}

class StrongEmphasisNodeStringBuilder : CompositeChildNodeStringBuilder<StrongEmphasis>() {
    override fun getSpanStyle(node: StrongEmphasis, typographyStyle: TypographyStyle): SpanStyle? {
        return typographyStyle.strongEmphasis
    }
}

class EmphasisNodeStringBuilder : CompositeChildNodeStringBuilder<Emphasis>() {
    override fun getSpanStyle(node: Emphasis, typographyStyle: TypographyStyle): SpanStyle? {
        return typographyStyle.emphasis
    }
}

class CodeNodeStringBuilder : IInlineNodeStringBuilder<Code> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Code,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        typographyStyle: TypographyStyle,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry
    ) {
        withStyle(typographyStyle.code.toSpanStyle()) {
            append(node.contentText())
        }
    }
}

class LinkNodeStringBuilder : IInlineNodeStringBuilder<Link> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Link,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        typographyStyle: TypographyStyle,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry
    ) {
        val linkNode = node
        val linkInteractionListener = actionHandler?.let {
            MarkdownLinkInteractionListener(actionHandler = it, node = linkNode)
        }
        val linkAnnotation = LinkAnnotation.Url(
            url = linkNode.url.toString(),
            styles = typographyStyle.link,
            linkInteractionListener = linkInteractionListener,
        )
        withLink(linkAnnotation) {
            buildStyleString(
                node,
                indentLevel,
                inlineContentMap,
                typographyStyle,
                renderRegistry,
                actionHandler,
                isShowNotSupported,
            )
        }
    }
}

class HeadingNodeStringBuilder() : CompositeChildNodeStringBuilder<Heading>() {
    override fun getSpanStyle(node: Heading, typographyStyle: TypographyStyle): SpanStyle? {
        return typographyStyle.getNodeSpanStyle(node)
    }

    override fun getParagraphStyle(
        node: Heading, typographyStyle: TypographyStyle
    ): ParagraphStyle? {
        return typographyStyle.getNodeParagraphStyle(node)
    }
}

class ParagraphNodeStringBuilder : CompositeChildNodeStringBuilder<Node>() {

    override fun getParagraphStyle(
        node: Node, typographyStyle: TypographyStyle
    ): ParagraphStyle? {
        return typographyStyle.getNodeParagraphStyle(node)
    }
}

class TableCellNodeStringBuilder : CompositeChildNodeStringBuilder<Node>()