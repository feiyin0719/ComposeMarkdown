package com.iffly.compose.markdown.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.style.currentTypographyStyle
import com.iffly.compose.markdown.util.getSpanStyle
import com.iffly.compose.markdown.widget.BasicText
import org.commonmark.internal.util.Parsing
import org.commonmark.node.BulletList
import org.commonmark.node.Emphasis
import org.commonmark.node.HardLineBreak
import org.commonmark.node.ListBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import kotlin.text.Typography.nbsp


@Composable
fun MarkdownText(parent: Node, modifier: Modifier = Modifier, indentLevel: Int = 0) {
    BasicText(
        text = markdownText(parent, indentLevel),
        modifier = modifier,
    )
}

@Composable
fun markdownText(node: Node, indentLevel: Int = 0): AnnotatedString = buildAnnotatedString {
    val style: SpanStyle = node.getSpanStyle()
    withStyle(style) {
        buildAnnotatedString(node, indentLevel)
    }
}

@Composable
fun AnnotatedString.Builder.buildAnnotatedString(parent: Node, indentLevel: Int = 0) {
    val typographyStyle = currentTypographyStyle()
    var node = parent.firstChild
    while (node != null) {
        when (node) {
            is Text -> append(node.literal)
            is HardLineBreak -> appendLine()
            is Paragraph -> {
                buildAnnotatedString(node, indentLevel)
            }

            is Emphasis -> withStyle(typographyStyle.emphasis) {
                buildAnnotatedString(node, indentLevel)
            }

            is StrongEmphasis -> withStyle(typographyStyle.strongEmphasis) {
                buildAnnotatedString(node, indentLevel)
            }

            is BulletList -> {
                buildAnnotatedString(node, indentLevel + 1)
            }

            is ListItem -> {
                ListWrapper(
                    child = node,
                    indentLevel = indentLevel,
                    marker = BULLET_POINT,
                    addToListNodes = { node, marker ->

                    },
                    getText = { node, level ->
                        buildAnnotatedString(
                            parent = node,
                            indentLevel = level,
                        )
                    },
                )
            }

            else -> {
                // Handle other node types if necessary
                // For now, we just skip them
            }
        }
        node = node.next
    }
}

private const val BULLET_POINT = "•"

@Composable
internal fun AnnotatedString.Builder.ListWrapper(
    child: ListItem,
    indentLevel: Int,
    marker: String,
    addToListNodes: (ListBlock, String) -> Unit,
    getText: @Composable AnnotatedString.Builder.(Node, Int) -> Unit,
) {
    appendLine()
    append("$nbsp".repeat(Parsing.CODE_BLOCK_INDENT * indentLevel))

    // Checking if there is an ordered list
    if (marker.toIntOrNull() != null) {
        val listNode = child.parent as OrderedList
        addToListNodes(listNode, marker.toIntOrNull()?.inc()?.toString() ?: BULLET_POINT)
        append(marker)
        append(listNode.delimiter)
    } else {
        append(marker)
    }
    append("$nbsp")
    getText(child, indentLevel)
}