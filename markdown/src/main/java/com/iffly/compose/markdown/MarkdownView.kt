package com.iffly.compose.markdown

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.dispatcher.MarkdownThreadPool
import com.iffly.compose.markdown.render.MarkdownContent
import com.iffly.compose.markdown.util.PreviewMarkdown
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal sealed class MarkdownState {
    object Loading : MarkdownState()

    data class Success(
        val node: Document,
    ) : MarkdownState()

    data class Error(
        val exception: Throwable,
    ) : MarkdownState()
}

/**
 * A Composable function that renders Markdown content.
 * This version parses the Markdown content synchronously within a remember block.
 * Use this for small to medium-sized content where parsing time is negligible.
 * For larger content, consider using the other overload that supports asynchronous parsing.
 * @param content The Markdown content as a String.
 * @param markdownRenderConfig Configuration for rendering the Markdown.
 * @param modifier Modifier to be applied to the Markdown view.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
 * @param onError Composable to display in case of an error during parsing.
 */
@Composable
fun MarkdownView(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    actionHandler: ActionHandler? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {
    val parser = markdownRenderConfig.parser
    val markdownState =
        remember(content, parser) {
            try {
                MarkdownState.Success(parser.parse(content))
            } catch (e: Exception) {
                MarkdownState.Error(e)
            }
        }

    when (markdownState) {
        is MarkdownState.Loading -> {
            // Loading state is instantaneous here since parsing is done in remember
            // You can implement a more complex loading state if needed
        }

        is MarkdownState.Success -> {
            MarkdownView(
                node = markdownState.node,
                markdownRenderConfig = markdownRenderConfig,
                modifier = modifier,
                showNotSupportedText = showNotSupportedText,
                actionHandler = actionHandler,
            )
        }

        is MarkdownState.Error -> {
            onError?.invoke(markdownState.exception)
        }
    }
}

/**
 * A Composable function that renders Markdown content.
 * This version parses the Markdown content asynchronously using a coroutine.
 * Use this for larger content where parsing time may be significant.
 * @param content The Markdown content as a String.
 * @param markdownRenderConfig Configuration for rendering the Markdown.
 * @param modifier Modifier to be applied to the Markdown view.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
 * @param parseDispatcher Optional dispatcher for parsing. Defaults to a background thread pool.
 * @param onLoading Composable to display while loading.
 * @param onError Composable to display in case of an error during parsing.
 */
@Composable
fun MarkdownView(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    actionHandler: ActionHandler? = null,
    parseDispatcher: CoroutineDispatcher? = null,
    onLoading: (@Composable () -> Unit)? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {
    val parser by rememberUpdatedState(markdownRenderConfig.parser)
    var markdownState by remember { mutableStateOf<MarkdownState>(MarkdownState.Loading) }

    LaunchedEffect(content, parser) {
        markdownState = MarkdownState.Loading
        try {
            val parsedNode =
                withContext(parseDispatcher ?: MarkdownThreadPool.dispatcher) {
                    parser.parse(content)
                }
            markdownState = MarkdownState.Success(parsedNode)
        } catch (e: Exception) {
            markdownState = MarkdownState.Error(e)
        }
    }

    when (val state = markdownState) {
        is MarkdownState.Loading -> {
            onLoading?.invoke()
        }

        is MarkdownState.Success -> {
            MarkdownView(
                node = state.node,
                markdownRenderConfig = markdownRenderConfig,
                modifier = modifier,
                showNotSupportedText = showNotSupportedText,
                actionHandler = actionHandler,
            )
        }

        is MarkdownState.Error -> {
            onError?.invoke(state.exception)
        }
    }
}

/**
 * A Composable function that renders a parsed Markdown AST node.
 * @param node The node of the parsed Markdown content.
 * @param markdownRenderConfig Configuration for rendering the Markdown.
 * @param modifier Modifier to be applied to the Markdown view.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
 *
 */
@Composable
fun MarkdownView(
    node: Node,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    actionHandler: ActionHandler? = null,
) {
    MarkdownLocalProviders(markdownRenderConfig, showNotSupportedText, actionHandler) {
        MarkdownContent(node, modifier)
    }
}

@PreviewMarkdown
@Composable
private fun MarkdownViewPreview() {
    MarkdownView(
        content =
            """
            # Basic Syntax Example
            
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
    )
}
