package com.iffly.compose.markdown

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.render.MarkdownContent
import com.vladsch.flexmark.util.ast.Node

/**
 * A Composable that displays markdown content using LazyColumn.
 * Unlike [LazyMarkdownView] which lazily loads and parses content from a file,
 * this component parses markdown upfront and only uses LazyColumn for
 * efficient rendering of the parsed nodes.
 *
 * @param content The markdown content as a String.
 * @param markdownRenderConfig Configuration for rendering the markdown.
 * @param modifier Modifier to be applied to the LazyColumn.
 * @param showNotSupportedText Whether to show text for unsupported elements.
 * @param actionHandler An optional ActionHandler to handle actions within the markdown content.
 * @param lazyListState The state of the LazyColumn for scroll control.
 */
@Composable
fun LazyMarkdownColumn(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    actionHandler: ActionHandler? = null,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val parser = markdownRenderConfig.parser
    val document =
        remember(content, parser) {
            parser.parse(content)
        }

    val children =
        remember(document) {
            buildList {
                var child = document.firstChild
                while (child != null) {
                    add(child)
                    child = child.next
                }
            }
        }

    MarkdownLocalProviders(
        markdownRenderConfig,
        showNotSupportedText,
        actionHandler = actionHandler,
    ) {
        val theme = markdownRenderConfig.markdownTheme
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
        ) {
            itemsIndexed(children, key = { index, node -> System.identityHashCode(node) }) { index, node ->
                MarkdownContent(
                    node = node,
                    modifier = Modifier,
                )
                if (index != children.lastIndex && theme.spacerTheme.showSpacer) {
                    Spacer(Modifier.height(theme.spacerTheme.spacerHeight))
                }
            }
        }
    }
}
