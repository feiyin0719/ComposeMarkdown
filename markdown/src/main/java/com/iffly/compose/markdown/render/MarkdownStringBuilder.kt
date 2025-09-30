package com.iffly.compose.markdown.render

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.util.contentText
import com.iffly.compose.markdown.util.getMarkerText
import com.iffly.compose.markdown.util.getNodeParagraphStyle
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.OrderedListItem
import com.vladsch.flexmark.ast.SoftLineBreak
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript
import com.vladsch.flexmark.util.ast.Node
import kotlin.text.Typography.nbsp

private fun AnnotatedString.Builder.buildStyleString(
    node: Node,
    indentLevel: Int,
    inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
    typographyStyle: TypographyStyle,
    inlineNodeStringBuilders: InlineNodeStringBuilders,
    linkInteractionListener: LinkInteractionListener?,
    isShowNotSupported: Boolean,
    style: SpanStyle? = null,
) {
    val buildFun = {
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
    if (style == null) {
        buildFun()
    } else {
        withStyle(style) {
            buildFun()
        }
    }

}

object TextNodeStringBuilder : IInlineNodeStringBuilder<Text> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Text,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        append(node.contentText())
    }
}

object ImageNodeStringBuilder : IInlineNodeStringBuilder<Image> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Image,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        val imageNode = node
        val imageId = "image_${imageNode.hashCode()}"
        val imageParagraphStyle = typographyStyle.imageParagraphStyle
        inlineContentMap[imageId] = MarkdownInlineTextContent(
            placeholder = Placeholder(
                width = imageParagraphStyle.lineHeight,
                height = imageParagraphStyle.lineHeight,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            ),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                MarkdownImage(
                    node = imageNode,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 2.dp)
                )
            }

        }
        withStyle(imageParagraphStyle) {
            appendInlineContent(imageId, "[${imageNode.title ?: imageNode.text}]")
        }
    }
}

object SoftLineBreakNodeStringBuilder :
    IInlineNodeStringBuilder<SoftLineBreak> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: SoftLineBreak,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        append("\n")
    }
}

object HardLineBreakNodeStringBuilder :
    IInlineNodeStringBuilder<com.vladsch.flexmark.ast.HardLineBreak> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: com.vladsch.flexmark.ast.HardLineBreak,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        append("\n")
    }
}

object StrikethroughNodeStringBuilder :
    IInlineNodeStringBuilder<Strikethrough> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Strikethrough,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        buildStyleString(
            node,
            indentLevel,
            inlineContentMap,
            typographyStyle,
            inlineNodeStringBuilders,
            linkInteractionListener,
            isShowNotSupported,
            typographyStyle.strikethrough
        )
    }
}

object SubscriptNodeStringBuilder :
    IInlineNodeStringBuilder<Subscript> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Subscript,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        buildStyleString(
            node,
            indentLevel,
            inlineContentMap,
            typographyStyle,
            inlineNodeStringBuilders,
            linkInteractionListener,
            isShowNotSupported,
            typographyStyle.subscript
        )
    }
}

object StrongEmphasisNodeStringBuilder :
    IInlineNodeStringBuilder<com.vladsch.flexmark.ast.StrongEmphasis> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: com.vladsch.flexmark.ast.StrongEmphasis,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        buildStyleString(
            node,
            indentLevel,
            inlineContentMap,
            typographyStyle,
            inlineNodeStringBuilders,
            linkInteractionListener,
            isShowNotSupported,
            typographyStyle.strongEmphasis
        )
    }
}

object EmphasisNodeStringBuilder :
    IInlineNodeStringBuilder<com.vladsch.flexmark.ast.Emphasis> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: com.vladsch.flexmark.ast.Emphasis,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        buildStyleString(
            node,
            indentLevel,
            inlineContentMap,
            typographyStyle,
            inlineNodeStringBuilders,
            linkInteractionListener,
            isShowNotSupported,
            typographyStyle.emphasis
        )
    }
}

object CodeNodeStringBuilder : IInlineNodeStringBuilder<com.vladsch.flexmark.ast.Code> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: com.vladsch.flexmark.ast.Code,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        withStyle(typographyStyle.code.toSpanStyle()) {
            append(node.contentText())
        }
    }
}

object LinkNodeStringBuilder : IInlineNodeStringBuilder<Link> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Link,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        val linkNode = node
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
                inlineNodeStringBuilders,
                linkInteractionListener,
                isShowNotSupported,
            )
        }
    }
}

object OrderedListNodeStringBuilder :
    IInlineNodeStringBuilder<OrderedList> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: OrderedList,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        buildStyleString(
            node,
            indentLevel + 1,
            inlineContentMap,
            typographyStyle,
            inlineNodeStringBuilders,
            linkInteractionListener,
            isShowNotSupported,
        )
    }
}

object BulletListNodeStringBuilder :
    IInlineNodeStringBuilder<BulletList> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: BulletList,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        buildStyleString(
            node,
            indentLevel + 1,
            inlineContentMap,
            typographyStyle,
            inlineNodeStringBuilders,
            linkInteractionListener,
            isShowNotSupported,
        )
    }
}

private fun AnnotatedString.Builder.buildListItem(
    node: ListItem,
    indentLevel: Int,
    marker: String,
    inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
    typographyStyle: TypographyStyle,
    inlineNodeStringBuilders: InlineNodeStringBuilders,
    linkInteractionListener: LinkInteractionListener? = null,
    isShowNotSupported: Boolean,
) {
    val paragraphStyle = typographyStyle.getNodeParagraphStyle(node.parent)
    withStyle(paragraphStyle) {
        append("$nbsp".repeat(indentLevel))

        append(marker)
        (node.parent as? OrderedList)?.let {
            append(it.delimiter)
        }
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


object OrderedListItemNodeStringBuilder :
    IInlineNodeStringBuilder<OrderedListItem> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: OrderedListItem,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        buildListItem(
            node,
            indentLevel,
            node.getMarkerText(),
            inlineContentMap,
            typographyStyle,
            inlineNodeStringBuilders,
            linkInteractionListener,
            isShowNotSupported,
        )
    }
}

object BulletListItemNodeStringBuilder :
    IInlineNodeStringBuilder<ListItem> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ListItem,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        buildListItem(
            node,
            indentLevel,
            node.getMarkerText(),
            inlineContentMap,
            typographyStyle,
            inlineNodeStringBuilders,
            linkInteractionListener,
            isShowNotSupported,
        )
    }
}

object ParagraphNodeStringBuilder :
    IInlineNodeStringBuilder<com.vladsch.flexmark.ast.Paragraph> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: com.vladsch.flexmark.ast.Paragraph,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        buildStyleString(
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