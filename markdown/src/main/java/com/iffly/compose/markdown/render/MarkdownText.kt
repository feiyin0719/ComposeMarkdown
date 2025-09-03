package com.iffly.compose.markdown.render

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.style.currentTypographyStyle
import com.iffly.compose.markdown.util.getMarkerText
import com.iffly.compose.markdown.util.getNodeStyle
import com.iffly.compose.markdown.widget.BasicText
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.internal.util.Parsing
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.Image
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
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
    val inlineNodeAnnotatedStringBuilders = currentInlineNodeAnnotatedStringBuilders()
    val (text, inlineContent) = remember(
        parent,
        typographyStyle,
        inlineNodeAnnotatedStringBuilders
    ) {
        markdownText(
            parent,
            typographyStyle,
            inlineNodeAnnotatedStringBuilders,
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
            )
        }
    }

    return Pair(annotatedString, inlineContentMap)
}

internal fun AnnotatedString.Builder.buildListItem(
    child: ListItem,
    indentLevel: Int,
    marker: String,
    inlineContentMap: MutableMap<String, InlineTextContent>,
    typographyStyle: TypographyStyle,
    inlineNodeStringBuilders: InlineNodeStringBuilders
) {
    appendLine()
    append("$nbsp".repeat(Parsing.CODE_BLOCK_INDENT * indentLevel))

    // Checking if there is an ordered list
    if (marker.toIntOrNull() != null) {
        val listNode = child.parent as OrderedList
        append(marker)
        append(listNode.markerDelimiter)
    } else {
        append(marker)
    }
    append("$nbsp")
    buildAnnotatedString(
        child,
        indentLevel,
        inlineContentMap,
        typographyStyle,
        inlineNodeStringBuilders
    )
}

fun AnnotatedString.Builder.buildAnnotatedString(
    parent: Node,
    indentLevel: Int = 1,
    inlineContentMap: MutableMap<String, InlineTextContent>,
    typographyStyle: TypographyStyle,
    inlineNodeStringBuilders: InlineNodeStringBuilders
) {
    var node = parent.firstChild
    while (node != null) {
        when (node) {
            is Text -> append(node.literal)
            is HardLineBreak, is SoftLineBreak -> appendLine()
            is Paragraph -> {
                buildAnnotatedString(
                    node,
                    indentLevel,
                    inlineContentMap,
                    typographyStyle,
                    inlineNodeStringBuilders
                )
            }

            is OrderedList, is BulletList -> {
                buildAnnotatedString(
                    node,
                    indentLevel + 1,
                    inlineContentMap,
                    typographyStyle,
                    inlineNodeStringBuilders
                )
            }
            is Emphasis -> withStyle(typographyStyle.emphasis) {
                buildAnnotatedString(
                    node,
                    indentLevel,
                    inlineContentMap,
                    typographyStyle,
                    inlineNodeStringBuilders
                )
            }
            is StrongEmphasis -> withStyle(typographyStyle.strongEmphasis) {
                buildAnnotatedString(
                    node,
                    indentLevel,
                    inlineContentMap,
                    typographyStyle,
                    inlineNodeStringBuilders
                )
            }
            is Link -> {
                val linkAnnotation = LinkAnnotation.Url(
                    url = node.destination,
                    styles = typographyStyle.link
                )
                withLink(linkAnnotation) {
                    buildAnnotatedString(
                        node,
                        indentLevel,
                        inlineContentMap,
                        typographyStyle,
                        inlineNodeStringBuilders
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
                )
            }
            is Code -> {
                val codeText = node.literal
                withStyle(typographyStyle.code) {
                    append(codeText)
                }
            }
            is Image -> {
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
            else -> {
                val customBuilder =
                    inlineNodeStringBuilders[node::class.java]
                customBuilder?.buildAnnotatedString(
                    node,
                    inlineContentMap,
                    typographyStyle,
                    indentLevel,
                    this
                )

            }
        }
        node = node.next
    }
}
