package com.iffly.compose.markdown.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.widget.BasicText
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.Text

@Composable
fun MarkdownContent(root: Node, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MarkdownNode(root, modifier = modifier)
    }
}

@Composable
fun ColumnScope.MarkdownNode(
    parent: Node,
    modifier: Modifier = Modifier,
) {
    var node = parent.firstChild
    while (node != null) {
        when (node) {
            is Paragraph -> {
                BasicText(
                    text = buildAnnotatedString(node),
                    modifier = modifier,
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

fun buildAnnotatedString(parent: Node): String {
    var node = parent.firstChild
    val stringBuilder = StringBuilder()
    while (node != null) {
        when (node) {
            is Text -> stringBuilder.append(node.literal ?: "")
            else -> stringBuilder.append(buildAnnotatedString(node))
        }
        node = node.next
    }
    return stringBuilder.toString()
}
