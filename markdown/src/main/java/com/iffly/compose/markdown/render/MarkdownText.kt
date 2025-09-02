package com.iffly.compose.markdown.render

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
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
import com.iffly.compose.markdown.style.currentTypographyStyle
import com.iffly.compose.markdown.util.getMarkerText
import com.iffly.compose.markdown.util.getSpanStyle
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
    val (text, inlineContent) = markdownText(parent, 1)
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

@Composable
fun markdownText(
    node: Node,
    indentLevel: Int = 0
): Pair<AnnotatedString, Map<String, InlineTextContent>> {
    val inlineContentMap = mutableMapOf<String, InlineTextContent>()

    val annotatedString = buildAnnotatedString {
        val style: SpanStyle = node.getSpanStyle()
        withStyle(style) {
            BuildAnnotatedString(node, indentLevel, inlineContentMap)
        }
    }

    return Pair(annotatedString, inlineContentMap)
}

@Composable
internal fun AnnotatedString.Builder.ListWrapper(
    child: ListItem,
    indentLevel: Int,
    marker: String,
    getText: @Composable AnnotatedString.Builder.(Node, Int) -> Unit,
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
    getText(child, indentLevel)
}

@Composable
fun AnnotatedString.Builder.BuildAnnotatedString(
    parent: Node,
    indentLevel: Int = 1,
    inlineContentMap: MutableMap<String, InlineTextContent>
) {
    val typographyStyle = currentTypographyStyle()
    var node = parent.firstChild
    while (node != null) {
        when (node) {
            is Text -> append(node.literal)
            is HardLineBreak -> appendLine()
            is Paragraph -> {
                BuildAnnotatedString(node, indentLevel, inlineContentMap)
            }
            is Emphasis -> withStyle(typographyStyle.emphasis) {
                BuildAnnotatedString(node, indentLevel, inlineContentMap)
            }
            is StrongEmphasis -> withStyle(typographyStyle.strongEmphasis) {
                BuildAnnotatedString(node, indentLevel, inlineContentMap)
            }
            is Link -> {
                val linkAnnotation = LinkAnnotation.Url(
                    url = node.destination,
                    styles = typographyStyle.link
                )
                withLink(linkAnnotation) {
                    BuildAnnotatedString(node, indentLevel, inlineContentMap)
                }
            }
            is BulletList -> {
                BuildAnnotatedString(node, indentLevel + 1, inlineContentMap)
            }
            is OrderedList -> {
                BuildAnnotatedString(node, indentLevel + 1, inlineContentMap)
            }
            is ListItem -> {
                ListWrapper(
                    child = node,
                    indentLevel = indentLevel,
                    marker = node.getMarkerText(),
                    getText = { node, level ->
                        BuildAnnotatedString(
                            parent = node,
                            indentLevel = level,
                            inlineContentMap = inlineContentMap
                        )
                    },
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

                // 添加 InlineTextContent 到 map
                inlineContentMap[imageId] = InlineTextContent(
                    placeholder = Placeholder(
                        width = 100.sp,
                        height = 100.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    MarkdownImage(
                        node = imageNode,
                        modifier = Modifier
                    )
                }

                // 在文本中插入占位符
                appendInlineContent(imageId, "[${imageNode.title}]")
            }
            else -> {
                // Handle other node types if necessary
                // For now, we just skip them
            }
        }
        node = node.next
    }
}
