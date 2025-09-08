package com.iffly.compose.markdown.samples

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig

@Composable
fun BasicSyntaxExample(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        MarkdownView(
            content = """
                # Basic Syntax Example
                
                This is an example demonstrating basic Markdown syntax.
                
                ## Text Formatting
                
                **Bold text** and *italic text* as well as `inline code`
                
                ## Lists
                
                ### Ordered Lists
                1. First item
                2. Second item
                   1. Nested item
                   2. Another nested item
                3. Third item
                
                ### Unordered Lists
                - Item A
                - Item B
                  - Nested item
                  - Another nested item
                - Item C
                
                ## Quotes
                
                > This is a quote block
                > Can contain multiple lines of content
                
                ## Dividers
                
                ---
                
                ## Links
                
                [GitHub](https://github.com) | [Google](https://google.com)
                
                ## Code
                
                ```kotlin
                fun greetUser(name: String) {
                    println("Hello, ${'$'}name!")
                }
                
                greetUser("Compose")
             
            """.trimIndent(),
            markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
            modifier = Modifier.padding(16.dp),
            linkInteractionListener = { linkAnnotation ->
                Log.d("BasicSyntax", "Clicked link: $linkAnnotation")
            }
        )
    }
}
