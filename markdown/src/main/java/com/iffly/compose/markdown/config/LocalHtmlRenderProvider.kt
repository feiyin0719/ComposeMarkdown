@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.vladsch.flexmark.html.HtmlRenderer

internal val LocalHtmlRenderProvider =
    staticCompositionLocalOf<HtmlRenderer> {
        error("No HtmlRenderer provided")
    }

@Composable
@ReadOnlyComposable
fun currentHtmlRenderer(): HtmlRenderer = LocalHtmlRenderProvider.current
