package com.iffly.compose.markdown.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.style.DefaultTypographyStyle
import com.iffly.compose.markdown.style.LocalTypographyStyleProvider
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.style.toHeadStyle
import com.iffly.compose.markdown.widget.BasicText
import org.commonmark.node.Emphasis
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text


@Composable
fun MarkdownText(parent: Node, modifier: Modifier = Modifier) {
    val typographyStyle = LocalTypographyStyleProvider.current ?: DefaultTypographyStyle
    BasicText(
        text = markdownText(parent, typographyStyle),
        modifier = modifier,
    )
}

fun markdownText(
    node: Node,
    typographyStyle: TypographyStyle,
): AnnotatedString = buildAnnotatedString {
    val style: SpanStyle = when (node) {
        is Heading -> node.level.toHeadStyle(typographyStyle)
        is Emphasis -> typographyStyle.emphasis
        is StrongEmphasis -> typographyStyle.strongEmphasis
        else -> typographyStyle.body // Handle other node types if necessary
    }
    withStyle(style) {
        buildAnnotatedString(node, typographyStyle)
    }
}

fun AnnotatedString.Builder.buildAnnotatedString(
    parent: Node,
    typographyStyle: TypographyStyle
) {
    var node = parent.firstChild
    while (node != null) {
        when (node) {
            is Text -> append(node.literal)
            is HardLineBreak -> appendLine()
            is Paragraph -> {
                buildAnnotatedString(node, typographyStyle)
            }

            is Emphasis -> withStyle(typographyStyle.emphasis) {
                buildAnnotatedString(node, typographyStyle)
            }

            is StrongEmphasis -> withStyle(typographyStyle.strongEmphasis) {
                buildAnnotatedString(node, typographyStyle)
            }

            else -> {
                // Handle other node types if necessary
                // For now, we just skip them
            }
        }
        node = node.next
    }
}