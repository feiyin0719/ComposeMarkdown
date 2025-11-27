package com.iffly.compose.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.iffly.compose.markdown.config.LocalActionHandlerProvider
import com.iffly.compose.markdown.config.LocalHtmlRenderProvider
import com.iffly.compose.markdown.config.LocalMarkdownThemeProvider
import com.iffly.compose.markdown.config.LocalRenderRegistryProvider
import com.iffly.compose.markdown.config.LocalShowNotSupportedProvider
import com.iffly.compose.markdown.config.MarkdownRenderConfig

@Composable
internal fun MarkdownLocalProviders(
    markdownRenderConfig: MarkdownRenderConfig,
    showNotSupportedText: Boolean,
    actionHandler: ActionHandler? = null,
    content: @Composable () -> Unit,
) {
    val theme = markdownRenderConfig.markdownTheme
    val htmlRenderer = markdownRenderConfig.htmlRenderer
    CompositionLocalProvider(
        LocalRenderRegistryProvider provides markdownRenderConfig.renderRegistry,
        LocalMarkdownThemeProvider provides theme,
        LocalShowNotSupportedProvider provides showNotSupportedText,
        LocalActionHandlerProvider provides actionHandler,
        LocalHtmlRenderProvider provides htmlRenderer,
    ) {
        content()
    }
}
