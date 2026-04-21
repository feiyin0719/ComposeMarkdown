package com.iffly.compose.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.iffly.compose.markdown.config.LocalActionHandlerProvider
import com.iffly.compose.markdown.config.LocalHtmlRenderProvider
import com.iffly.compose.markdown.config.LocalHtmlToMdConverterProvider
import com.iffly.compose.markdown.config.LocalMarkdownThemeProvider
import com.iffly.compose.markdown.config.LocalNodeDataMap
import com.iffly.compose.markdown.config.LocalParserProvider
import com.iffly.compose.markdown.config.LocalRenderRegistryProvider
import com.iffly.compose.markdown.config.LocalShowNotSupportedProvider
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.vladsch.flexmark.util.ast.Node

@Composable
internal fun MarkdownLocalProviders(
    markdownRenderConfig: MarkdownRenderConfig,
    showNotSupportedText: Boolean,
    actionHandler: ActionHandler? = null,
    content: @Composable () -> Unit,
) {
    val theme = markdownRenderConfig.markdownTheme
    val htmlRenderer = markdownRenderConfig.htmlRenderer
    val nodeDataMap = remember { mutableStateMapOf<Node, Any>() }
    CompositionLocalProvider(
        LocalRenderRegistryProvider provides markdownRenderConfig.renderRegistry,
        LocalParserProvider provides markdownRenderConfig.parser,
        LocalMarkdownThemeProvider provides theme,
        LocalShowNotSupportedProvider provides showNotSupportedText,
        LocalActionHandlerProvider provides actionHandler,
        LocalHtmlRenderProvider provides htmlRenderer,
        LocalHtmlToMdConverterProvider provides markdownRenderConfig.htmlToMdConverter,
        LocalNodeDataMap provides nodeDataMap,
    ) {
        content()
    }
}
