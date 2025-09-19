package com.iffly.compose.markdown.render

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.config.currentInlineNodeStringBuilders
import com.iffly.compose.markdown.config.currentLinkClickListener
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.iffly.compose.markdown.config.isShowNotSupported
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.util.contentText
import com.iffly.compose.markdown.util.getMarkerText
import com.iffly.compose.markdown.util.getNodeStyle
import com.iffly.compose.markdown.widget.BasicText
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.HardLineBreak
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.SoftLineBreak
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript
import com.vladsch.flexmark.util.ast.Node
import kotlin.text.Typography.nbsp


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
        val style: SpanStyle = typographyStyle.getNodeStyle(node)
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
    val buildStringFun = { node: Node ->
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
    while (node != null) {
        when {
            node::class.java == Text::class.java -> append(node.contentText())
            node::class.java == HardLineBreak::class.java || node::class.java == SoftLineBreak::class.java -> appendLine()
            node::class.java == Paragraph::class.java -> {
                buildStringFun(node)
            }

            node::class.java == OrderedList::class.java || node::class.java == BulletList::class.java -> {
                buildStringFun(node)
            }

            node::class.java == Emphasis::class.java -> withStyle(typographyStyle.emphasis) {
                buildStringFun(node)
            }

            node::class.java == StrongEmphasis::class.java -> withStyle(typographyStyle.strongEmphasis) {
                buildStringFun(node)
            }

            node::class.java == Link::class.java -> {
                val linkNode = node as Link
                val linkAnnotation = LinkAnnotation.Url(
                    url = linkNode.url.toString(),
                    styles = typographyStyle.link,
                    linkInteractionListener = linkInteractionListener,
                )
                withLink(linkAnnotation) {
                    buildStringFun(node)
                }
            }

            node::class.java == Strikethrough::class.java -> {
                withStyle(typographyStyle.strikethrough) {
                    buildStringFun(node)
                }
            }

            node::class.java == Subscript::class.java -> {
                withStyle(typographyStyle.subscript) {
                    buildStringFun(node)
                }
            }

            node::class.java == ListItem::class.java -> {
                val listItemNode = node as ListItem
                buildListItem(
                    child = listItemNode,
                    indentLevel = indentLevel,
                    marker = listItemNode.getMarkerText(),
                    inlineContentMap = inlineContentMap,
                    typographyStyle = typographyStyle,
                    inlineNodeStringBuilders,
                    linkInteractionListener,
                    isShowNotSupported,
                )
            }

            node::class.java == Code::class.java -> {
                val codeNode = node as Code
                val codeText = codeNode.contentText()
                withStyle(typographyStyle.code) {
                    append(codeText)
                }
            }

            node::class.java == Image::class.java -> {
                val imageNode = node as Image
                buildImage(imageNode, inlineContentMap)
            }

            else -> {
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

            }
        }
        node = node.next
    }
}

fun AnnotatedString.Builder.buildListItem(
    child: ListItem,
    indentLevel: Int,
    marker: String,
    inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
    typographyStyle: TypographyStyle,
    inlineNodeStringBuilders: InlineNodeStringBuilders,
    linkInteractionListener: LinkInteractionListener? = null,
    isShowNotSupported: Boolean,
) {
    appendLine()
    append("$nbsp".repeat(indentLevel))

    // Checking if there is an ordered list
    if (marker.toIntOrNull() != null) {
        val listNode = child.parent as OrderedList
        append(marker)
        append(listNode.delimiter)
    } else {
        append(marker)
    }
    append("$nbsp")
    buildMarkdownAnnotatedString(
        child,
        indentLevel,
        inlineContentMap,
        typographyStyle,
        inlineNodeStringBuilders,
        linkInteractionListener,
        isShowNotSupported,
    )
}

fun AnnotatedString.Builder.buildImage(
    node: Image,
    inlineContentMap: MutableMap<String, MarkdownInlineTextContent>
) {
    val imageNode = node
    val imageId = "image_${imageNode.hashCode()}"
    inlineContentMap[imageId] = MarkdownInlineTextContent(
        placeholder = Placeholder(
            width = 300.sp,
            height = 300.sp,
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
    withStyle(ParagraphStyle(lineHeight = 300.sp)) {
        appendInlineContent(imageId, "[${imageNode.title ?: imageNode.text}]")
    }
}

data class MarkdownInlineTextContent(
    val placeholder: Placeholder,
    val children: @Composable (String) -> Unit
)