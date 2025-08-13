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
import org.commonmark.node.Emphasis
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text


@Composable
fun MarkdownText(parent: Node, modifier: Modifier = Modifier) {
    BasicText(
        text = markdownText(parent),
        modifier = modifier,
    )
}

@Composable
fun markdownText(node: Node): AnnotatedString = buildAnnotatedString {
    val style: SpanStyle = node.getSpanStyle()
    withStyle(style) {
        buildAnnotatedString(node)
    }
}

@Composable
fun AnnotatedString.Builder.buildAnnotatedString(parent: Node) {
    val typographyStyle = currentTypographyStyle()
    var node = parent.firstChild
    while (node != null) {
        when (node) {
            is Text -> append(node.literal)
            is HardLineBreak -> appendLine()
            is Paragraph -> {
                buildAnnotatedString(node)
            }

            is Emphasis -> withStyle(typographyStyle.emphasis) {
                buildAnnotatedString(node)
            }

            is StrongEmphasis -> withStyle(typographyStyle.strongEmphasis) {
                buildAnnotatedString(node)
            }

            else -> {
                // Handle other node types if necessary
                // For now, we just skip them
            }
        }
        node = node.next
    }
}