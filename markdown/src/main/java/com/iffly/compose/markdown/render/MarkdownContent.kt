package com.iffly.compose.markdown.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.parser.ParserFactory
import com.iffly.compose.markdown.style.LocalTypographyStyleProvider
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.util.MarkdownPreview
import org.commonmark.node.BulletList
import org.commonmark.node.Heading
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph

@Composable
fun MarkdownContent(root: Node, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = modifier.fillMaxSize()
    ) {
        MarkdownNode(root, modifier = Modifier.fillMaxWidth())
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
                MarkdownText(node, modifier = modifier)
            }
            is Heading -> {
                MarkdownText(node, modifier = modifier)
            }
            is BulletList -> {
                MarkdownText(node, modifier = modifier, indentLevel = 1)
            }
            is OrderedList -> {
                MarkdownText(node, modifier = modifier, indentLevel = 1)
            }
            else -> {
                // Handle other node types if necessary
                // For now, we just skip them
            }
        }
        Spacer(Modifier.height(8.dp))
        node = node.next
    }
}


@MarkdownPreview
@Composable
private fun MarkdownContentPreview() {
    val testText = """
        # Sample Markdown Content

        This is a **bold text** and this is *italic text*.[baidu](https://www.baidu.com)
        
        [baidu](https://www.baidu.com)
        
        ## Subheading
        
        ### h3 Heading

        ---
        
        - **Item 1**
        - Item 2
        - Item 3
        
        1. Ordered Item 1
        2. Ordered Item 2
        3. *Ordered Item 3*
    """.trimIndent()
    val node = ParserFactory().build().parse(testText)
    CompositionLocalProvider(
        LocalTypographyStyleProvider provides TypographyStyle(
            body = SpanStyle(
                fontSize = 20.sp
            )
        )
    ) {
        MarkdownContent(node, Modifier.height(500.dp))
    }
}
