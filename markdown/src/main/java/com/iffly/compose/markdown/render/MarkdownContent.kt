package com.iffly.compose.markdown.render

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.config.LocalMarkdownThemeProvider
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.config.currentRenderRegistry
import com.iffly.compose.markdown.config.isShowNotSupported
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.PreviewMarkdown
import com.iffly.compose.markdown.util.contentText
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node

/**
 * A Composable that renders markdown content based on the type of node.
 * @param node The root node of the markdown content to be rendered.
 * @param modifier The modifier to be applied to the markdown content.
 */
@Composable
fun MarkdownContent(
    node: Node,
    modifier: Modifier = Modifier,
) {
    when (node) {
        is Block -> {
            MarkdownBlock(
                node = node,
                modifier = modifier,
            )
        }

        else -> {
            MarkdownText(node, modifier = modifier)
        }
    }
}

/**
 * A Composable that renders a markdown block node.
 * @param node The block node to be rendered.
 * @param modifier The modifier to be applied to the block node.
 */
@Composable
fun MarkdownBlock(
    node: Block,
    modifier: Modifier = Modifier,
) {
    val renderRegistry = currentRenderRegistry()

    val renderer = renderRegistry.getBlockRenderer(node::class.java)
    if (renderer != null) {
        renderer.Invoke(node, modifier)
    } else {
        // Fallback to rendering children if no renderer is found
        Log.i(
            "MarkdownNode",
            "No renderer found for ${node::class.java.simpleName}, rendering children.",
        )
        val isShowNotSupported = isShowNotSupported()
        if (isShowNotSupported) {
            Text(
                text = "Unsupported block: ${node::class.java.simpleName}",
            )
        } else {
            Text(node.contentText())
        }
    }
}

@PreviewMarkdown
@Composable
private fun MarkdownContentPreview() {
    val testText =
        """
        # Sample Markdown Content

        This is a **bold text** and this is *italic text*.[baidu](https://www.baidu.com)
        
        [baidu](https://www.baidu.com)
        
        ## Subheading
        
        ### h3 Heading

        ---
        
        > This is a blockquote example.
        > It can span multiple lines and contain **bold** and *italic* text.
        > 
        > It can also contain nested content like lists:
        > - Item 1
        > - Item 2
        
        Regular paragraph after blockquote.
        
        Subscript: H~2~O, Superscript: E = mc^2^
        
        ~~test~~
        
        - **Item 1**
        - Item 2
        - Item 3
        
        1. Ordered Item 1
           1. Nested Ordered Item 1
           2. Nested Ordered Item 2
        2. Ordered Item 2
        3. *Ordered Item 3*
        
        ```kotlin
        fun main() {
            println("Hello, World!")
            val list = listOf(1, 2, 3)
            list.forEach { println(it) }
        }
        ```
        
        ```javascript
        function greet(name) {
            console.log(`Hello, ${'$'}{name}!`);
        }
        
        greet('World');
        ```
        | Name | Age | City |
        |------|-----|------|
        | John | 25 | New York |
        | Jane | 30 | **San Francisco** |
        | Bob | *22* | [Chicago](https://chicago.com) |
        
        ---
        
        This is an indented code block:
        
            // This is an indented code block
            val x = 10
            val y = 20
            println("Sum: ${'$'}{x + y}")
        
        
        ![Image](https://office.visualstudio.com/2003b897-e349-46b6-a733-61b32410d686/_apis/git/repositories/09de2423-725a-49cf-acff-a50529f2917f/pullRequests/4297383/attachments/image.png)
        """.trimIndent()
    val node =
        MarkdownRenderConfig
            .Builder()
            .build()
            .parser
            .parse(testText)
    CompositionLocalProvider(
        LocalMarkdownThemeProvider provides MarkdownTheme(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState()),
        ) {
            MarkdownContent(node)
        }
    }
}
