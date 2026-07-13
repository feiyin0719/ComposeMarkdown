package com.iffly.compose.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.dispatcher.MarkdownThreadPool
import com.iffly.compose.markdown.render.MarkdownContent
import com.iffly.compose.markdown.util.PreviewMarkdown
import com.vladsch.flexmark.util.ast.Node
import kotlinx.coroutines.CoroutineDispatcher

/**
 * A Composable function that renders Markdown content.
 * This version parses the Markdown content synchronously within a remember block.
 * Use this for small to medium-sized content where parsing time is negligible.
 * For larger content, consider using the other overload that supports asynchronous parsing.
 * @param content The Markdown content as a String.
 * @param markdownRenderConfig Configuration for rendering the Markdown. Create custom instances with [remember].
 * @param modifier Modifier to be applied to the Markdown view.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
 * @param renderDependencies Dependencies available to custom renderers and node string builders.
 * @param isStreaming Whether [content] is an append-only partial stream. Setting it to `false`
 * forces a final full parse. If no streaming parser factory is configured, normal full parsing is
 * used regardless of this value.
 * @param onError Composable to display in case of an error during parsing.
 */
@Composable
fun MarkdownView(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    isStreaming: Boolean = false,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {
    val streamingParser =
        remember(markdownRenderConfig, isStreaming) {
            if (isStreaming) markdownRenderConfig.createStreamingMarkdownParser() else null
        }
    val markdownState =
        rememberMarkdownState(
            content = content,
            isStreaming = isStreaming,
            streamingParser = streamingParser,
            fallbackParser = markdownRenderConfig.parser,
        )

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
                renderDependencies = renderDependencies,
            )
        }

        is MarkdownState.Error -> {
            onError?.invoke(markdownState.throwable)
        }
    }
}

/**
 * A Composable function that renders Markdown content.
 * This version parses the Markdown content asynchronously using a coroutine.
 * Use this for larger content where parsing time may be significant.
 * @param content The Markdown content as a String.
 * @param markdownRenderConfig Configuration for rendering the Markdown. Create custom instances with [remember].
 * @param modifier Modifier to be applied to the Markdown view.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
 * @param renderDependencies Dependencies available to custom renderers and node string builders.
 * @param parseDispatcher Optional dispatcher for parsing. Defaults to a background thread pool.
 * @param isStreaming Whether [content] is an append-only partial stream. Setting it to `false`
 * forces a final full parse. If no streaming parser factory is configured, normal full parsing is
 * used regardless of this value.
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
    renderDependencies: Map<String, Any> = emptyMap(),
    parseDispatcher: CoroutineDispatcher? = null,
    isStreaming: Boolean = false,
    onLoading: (@Composable () -> Unit)? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {
    val streamingParser =
        remember(markdownRenderConfig, isStreaming) {
            if (isStreaming) markdownRenderConfig.createStreamingMarkdownParser() else null
        }
    val markdownState by
        rememberAsyncMarkdownState(
            content = content,
            isStreaming = isStreaming,
            streamingParser = streamingParser,
            fallbackParser = markdownRenderConfig.parser,
            dispatcher = parseDispatcher ?: MarkdownThreadPool.dispatcher,
        )

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
                renderDependencies = renderDependencies,
            )
        }

        is MarkdownState.Error -> {
            onError?.invoke(state.throwable)
        }
    }
}

/**
 * A Composable function that renders a parsed Markdown AST node.
 * @param node The node of the parsed Markdown content.
 * @param markdownRenderConfig Configuration for rendering the Markdown. Create custom instances with [remember].
 * @param modifier Modifier to be applied to the Markdown view.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the Markdown content.
 * @param renderDependencies Dependencies available to custom renderers and node string builders.
 *
 */
@Composable
fun MarkdownView(
    node: Node,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
) {
    MarkdownLocalProviders(
        markdownRenderConfig = markdownRenderConfig,
        showNotSupportedText = showNotSupportedText,
        actionHandler = actionHandler,
        renderDependencies = renderDependencies,
    ) {
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
        markdownRenderConfig = remember { MarkdownRenderConfig.Builder().build() },
    )
}
