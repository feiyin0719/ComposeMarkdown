package com.iffly.compose.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.LinkInteractionListener
import com.iffly.compose.markdown.config.LocalBlockRenderersProvider
import com.iffly.compose.markdown.config.LocalHtmlRenderProvider
import com.iffly.compose.markdown.config.LocalInlineNodeStringBuildersProvider
import com.iffly.compose.markdown.config.LocalLinkClickListenerProvider
import com.iffly.compose.markdown.config.LocalShowNotSupportedProvider
import com.iffly.compose.markdown.config.LocalTypographyStyleProvider
import com.iffly.compose.markdown.config.MarkdownRenderConfig

@Composable
internal fun MarkdownLocalProviders(
    markdownRenderConfig: MarkdownRenderConfig,
    showNotSupportedText: Boolean,
    linkInteractionListener: LinkInteractionListener? = null,
    content: @Composable () -> Unit
) {
    val linkInteractionListener by rememberUpdatedState(linkInteractionListener)
    val inlineNodeStringBuilders by
    rememberUpdatedState(markdownRenderConfig.inlineNodeStringBuilders)
    val blockRenderers by rememberUpdatedState(markdownRenderConfig.blockRenderers)
    val typographyStyle by rememberUpdatedState(markdownRenderConfig.typographyStyle)
    val htmlRenderer by rememberUpdatedState(markdownRenderConfig.htmlRenderer)
    CompositionLocalProvider(
        LocalInlineNodeStringBuildersProvider provides inlineNodeStringBuilders,
        LocalBlockRenderersProvider provides blockRenderers,
        LocalTypographyStyleProvider provides typographyStyle,
        LocalShowNotSupportedProvider provides showNotSupportedText,
        LocalLinkClickListenerProvider provides linkInteractionListener,
        LocalHtmlRenderProvider provides htmlRenderer,
    ) {
        content()
    }
}