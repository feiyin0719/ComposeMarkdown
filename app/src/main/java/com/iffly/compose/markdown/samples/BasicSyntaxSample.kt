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
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.vladsch.flexmark.util.ast.Node

@Composable
fun BasicSyntaxExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
    ) {
        MarkdownView(
            content =
                """
                # Basic Syntax Example
                
                This is an example demonstrating basic Markdown syntax.
                
                ## Text Formatting
                
                **Bold text** and *italic text* as well as `inline code`
                
                ## Lists
                
                ### Ordered Lists
                1. First item
                2. Second item
                   1. Nested item
                      - 1
                      - 2
                   2. Another nested item
                3. Third item
                4. 1. Fourth item with number
                   2. Another fourth item
                
                ### Loose List
                1. Item 1
                    
                    This is a loose list item with multiple paragraphs.
                    
                    Another paragraph in the same list item.
                    
                    > Blockquote inside loose list item.
                    > Another line of blockquote.
                    
                    1. Nested ordered list inside loose item.
                    
                    2. ``` kotlin
                       val x = 10
                       ```
                       
                2. > quote as second item
                   >> another line of quote
                   
                   test paragraph after quote.
                   
                   ```java
                     System.out.println("Hello, World!");
                   ```
                   
                              
                
                ### Unordered Lists
                - Item A
                - Item B
                  - Nested item
                  - Another nested item
                - Item C
                
                ## Quotes
                
                > This is a quote block
                > Can contain multiple lines of content
                
                ### Quote with List
                > - Quote list item 1
                > - Quote list item 2
                > 1. Nested quote list item
                > 2. Another nested quote list item
                >>  - Nested quote list item
                >>  - Another nested quote list item
                > # Quote with Heading1
                > ## Quote with Heading2
                > Regular paragraph inside quote.
                > ```python
                > def hello():
                >     print("Hello from quote!")
                > ```
                
                ## Dividers
                
                ---
                
                ## Links
                
                [GitHub](https://github.com) | [Google](https://google.com)
                
                ### Reference Links
                
                This is a [reference link][ref1].
                
                This is another [reference link][ref2].
                
                [ref1]: https://kotlinlang.org
                [ref2]: https://android.com
                
                ### Collapsed Reference Links
                
                This is a [collapsed reference link][].
                
                [collapsed reference link]: https://compose.google.com
                
                ### Shortcut Reference Links
                
                [Shortcut Link]
                
                [Shortcut Link]: https://developer.android.com
                
                ## Code
                
                ```kotlin
                fun greetUser(name: String) {
                    println("Hello, ${'$'}name!")
                }
                
                greetUser("Compose")
                
                """.trimIndent(),
            markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
            modifier = Modifier.padding(16.dp),
            actionHandler =
                object : ActionHandler {
                    override fun handleUrlClick(
                        url: String,
                        node: Node,
                    ) {
                        Log.d("BasicSyntax", "Clicked link: $url")
                    }
                },
        )
    }
}
