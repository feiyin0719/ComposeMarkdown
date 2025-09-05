package com.iffly.compose.markdown.render

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.config.LocalTypographyStyleProvider
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.config.currentBlockRenderers
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.util.MarkdownPreview
import org.commonmark.node.Block
import org.commonmark.node.Node

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
fun MarkdownNode(
    parent: Node,
    modifier: Modifier = Modifier,
) {
    var node = parent.firstChild
    val blockRenderers = currentBlockRenderers()
    while (node != null) {
        if (node is Block) {
            val renderer = blockRenderers[node::class.java]
            if (renderer != null) {
                renderer.Invoke(node, modifier)
            } else {
                // Fallback to rendering children if no renderer is found
                Log.i(
                    "MarkdownNode",
                    "No renderer found for ${node::class.java.simpleName}, rendering children."
                )
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
        
        This is an indented code block:
        
            // This is an indented code block
            val x = 10
            val y = 20
            println("Sum: ${'$'}{x + y}")
        
        
        ![Image](https://office.visualstudio.com/2003b897-e349-46b6-a733-61b32410d686/_apis/git/repositories/09de2423-725a-49cf-acff-a50529f2917f/pullRequests/4297383/attachments/image.png)
    """.trimIndent()
    val node = MarkdownRenderConfig.Builder().build().parser.parse(testText)
    CompositionLocalProvider(
        LocalTypographyStyleProvider provides TypographyStyle(
            body = SpanStyle(
                fontSize = 20.sp
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
        ) {
            MarkdownContent(node)
        }
    }
}
