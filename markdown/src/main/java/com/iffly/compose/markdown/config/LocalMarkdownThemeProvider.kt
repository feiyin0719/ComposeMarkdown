@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.iffly.compose.markdown.style.MarkdownTheme

internal val LocalMarkdownThemeProvider =
    staticCompositionLocalOf<MarkdownTheme> {
        error("No MarkdownTheme provided")
    }

@Composable
@ReadOnlyComposable
internal fun currentTheme(): MarkdownTheme = LocalMarkdownThemeProvider.current
