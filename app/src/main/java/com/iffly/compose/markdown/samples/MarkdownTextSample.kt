package com.iffly.compose.markdown.samples

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.MarkdownText
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.vladsch.flexmark.util.ast.Node

@Composable
fun MarkdownTextExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
    ) {

        SelectionContainer {
            MarkdownText(
                content =
                    """
                    # MarkdownText Example

                    This example uses the **Text-based** rendering approach (`MarkdownText`),
                    which renders the entire document through a single `RichText` composable.

                    ## Cross-paragraph Text Selection

                    Try selecting text across these paragraphs. Unlike `MarkdownView`,
                    `MarkdownText` enables continuous text selection across paragraph boundaries.

                    This is another paragraph. You can **select from the first paragraph**
                    all the way down to *this paragraph* in a single gesture.

                    ## Code Blocks

                    ```kotlin
                    fun greet(name: String) {
                        println("Hello, ${'$'}name!")
                    }
                    ```

                    ## Block Quotes

                    > This is a blockquote rendered as embedded inline content.
                    > It adjusts its size based on the actual content.

                    ## Lists

                    - Item 1
                    - Item 2
                      - Nested item A
                      - Nested item B
                    - Item 3

                    ### Ordered List
                    1. First
                    2. Second
                    3. Third

                    ## Links

                    Visit [GitHub](https://github.com) or [Google](https://google.com).

                    ---

                    *End of MarkdownText example.*
                    """.trimIndent(),
                markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentHeight(),
                actionHandler =
                    object : ActionHandler {
                        override fun handleUrlClick(
                            url: String,
                            node: Node,
                        ) {
                            Log.d("MarkdownText", "Clicked link: $url")
                        }
                    },
            )
        }
    }
}
