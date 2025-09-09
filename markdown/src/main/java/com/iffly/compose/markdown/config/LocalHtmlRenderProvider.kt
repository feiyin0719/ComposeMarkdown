package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import com.vladsch.flexmark.html.HtmlRenderer

internal val LocalHtmlRenderProvider =
    androidx.compose.runtime.staticCompositionLocalOf<HtmlRenderer> {
        HtmlRenderer.builder().build()
    }

@Composable
internal fun currentHtmlRenderer(): HtmlRenderer {
    return LocalHtmlRenderProvider.current
}