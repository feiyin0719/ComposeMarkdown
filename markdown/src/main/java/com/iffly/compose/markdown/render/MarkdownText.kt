package com.iffly.compose.markdown.render

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
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.config.currentInlineNodeStringBuilders
import com.iffly.compose.markdown.config.currentLinkClickListener
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.util.contentText
import com.iffly.compose.markdown.util.getMarkerText
import com.iffly.compose.markdown.util.getNodeStyle
import com.iffly.compose.markdown.widget.BasicText
import com.vladsch.flexmark.ext.tables.TableCell
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.HardLineBreak
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.SoftLineBreak
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.ast.util.Parsing
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
) {
    val typographyStyle = currentTypographyStyle()
    val inlineNodeStringBuilders = currentInlineNodeStringBuilders()
    val linkInteractionListener = currentLinkClickListener()
    val (text, inlineContent) = remember(
        parent,
        typographyStyle,
        inlineNodeStringBuilders
    ) {
        markdownText(
            parent,
            typographyStyle,
            inlineNodeStringBuilders,
            linkInteractionListener,
            1,
        )
    }

    val textAlign = if (parent is TableCell) {
        parent.alignment.toTextAlign()
    } else {
        null
    }
    BasicText(
        text = text,
        inlineContent = inlineContent,
        modifier = modifier,
        textAlign = textAlign,
        style = typographyStyle.textStyle ?: LocalTextStyle.current
    )
}

private fun TableCell.Alignment?.toTextAlign(): TextAlign? {
    return when (this) {
        TableCell.Alignment.CENTER -> TextAlign.Center
        TableCell.Alignment.RIGHT -> TextAlign.Right
        else -> null
    }
}

fun markdownText(
    node: Node,
    typographyStyle: TypographyStyle,
    inlineNodeStringBuilders: InlineNodeStringBuilders,
    linkInteractionListener: LinkInteractionListener? = null,
    indentLevel: Int = 0
): Pair<AnnotatedString, Map<String, InlineTextContent>> {
    val inlineContentMap = mutableMapOf<String, InlineTextContent>()

    val annotatedString = buildAnnotatedString {
        val style: SpanStyle = typographyStyle.getNodeStyle(node)
        withStyle(style) {
            buildAnnotatedString(
                node,
                indentLevel,
                inlineContentMap,
                typographyStyle,
                inlineNodeStringBuilders,
                linkInteractionListener,
            )
        }
    }

    return Pair(annotatedString, inlineContentMap)
}

fun AnnotatedString.Builder.buildAnnotatedString(
    parent: Node,
    indentLevel: Int = 1,
    inlineContentMap: MutableMap<String, InlineTextContent>,
    typographyStyle: TypographyStyle,
    inlineNodeStringBuilders: InlineNodeStringBuilders,
    linkInteractionListener: LinkInteractionListener? = null,
) {
    var node = parent.firstChild
    while (node != null) {
        when (node) {
            is Text -> append(node.contentText())
            is HardLineBreak, is SoftLineBreak -> appendLine()
            is Paragraph -> {
                buildAnnotatedString(
                    node,
                    indentLevel,
                    inlineContentMap,
                    typographyStyle,
                    inlineNodeStringBuilders,
                    linkInteractionListener,
                )
            }

            is OrderedList, is BulletList -> {
                buildAnnotatedString(
                    node,
                    indentLevel + 1,
                    inlineContentMap,
                    typographyStyle,
                    inlineNodeStringBuilders,
                    linkInteractionListener,
                )
            }

            is Emphasis -> withStyle(typographyStyle.emphasis) {
                buildAnnotatedString(
                    node,
                    indentLevel,
                    inlineContentMap,
                    typographyStyle,
                    inlineNodeStringBuilders,
                    linkInteractionListener,
                )
            }

            is StrongEmphasis -> withStyle(typographyStyle.strongEmphasis) {
                buildAnnotatedString(
                    node,
                    indentLevel,
                    inlineContentMap,
                    typographyStyle,
                    inlineNodeStringBuilders,
                    linkInteractionListener,
                )
            }

            is Link -> {
                val linkAnnotation = LinkAnnotation.Url(
                    url = node.url.toString(),
                    styles = typographyStyle.link,
                    linkInteractionListener = linkInteractionListener,
                )
                withLink(linkAnnotation) {
                    buildAnnotatedString(
                        node,
                        indentLevel,
                        inlineContentMap,
                        typographyStyle,
                        inlineNodeStringBuilders,
                        linkInteractionListener,
                    )
                }
            }

            is ListItem -> {
                buildListItem(
                    child = node,
                    indentLevel = indentLevel,
                    marker = node.getMarkerText(),
                    inlineContentMap = inlineContentMap,
                    typographyStyle = typographyStyle,
                    inlineNodeStringBuilders,
                    linkInteractionListener,
                )
            }

            is Code -> {
                val codeText = node.contentText()
                withStyle(typographyStyle.code) {
                    append(codeText)
                }
            }

            is Image -> {
                buildImage(node, inlineContentMap)
            }

            else -> {
                val customBuilder =
                    inlineNodeStringBuilders[node::class.java]
                customBuilder?.buildAnnotatedString(
                    node,
                    inlineContentMap,
                    typographyStyle,
                    indentLevel,
                    linkInteractionListener,
                    this
                )

            }
        }
        node = node.next
    }
}

fun AnnotatedString.Builder.buildListItem(
    child: ListItem,
    indentLevel: Int,
    marker: String,
    inlineContentMap: MutableMap<String, InlineTextContent>,
    typographyStyle: TypographyStyle,
    inlineNodeStringBuilders: InlineNodeStringBuilders,
    linkInteractionListener: LinkInteractionListener? = null,
) {
    appendLine()
    append("$nbsp".repeat( indentLevel))

    // Checking if there is an ordered list
    if (marker.toIntOrNull() != null) {
        val listNode = child.parent as OrderedList
        append(marker)
        append(listNode.delimiter)
    } else {
        append(marker)
    }
    append("$nbsp")
    buildAnnotatedString(
        child,
        indentLevel,
        inlineContentMap,
        typographyStyle,
        inlineNodeStringBuilders,
        linkInteractionListener,
    )
}

fun AnnotatedString.Builder.buildImage(
    node: Image,
    inlineContentMap: MutableMap<String, InlineTextContent>
) {
    val imageNode = node
    val imageId = "image_${imageNode.hashCode()}"
    inlineContentMap[imageId] = InlineTextContent(
        placeholder = Placeholder(
            width = 100.sp,
            height = 100.sp,
            placeholderVerticalAlign = PlaceholderVerticalAlign.Top
        )
    ) {
        MarkdownImage(
            node = imageNode,
            modifier = Modifier.wrapContentSize()
        )
    }
    appendInlineContent(imageId, "[${imageNode.title}]")
}
