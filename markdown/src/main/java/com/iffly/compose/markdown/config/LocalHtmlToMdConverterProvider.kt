@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter

internal val LocalHtmlToMdConverterProvider =
    staticCompositionLocalOf<FlexmarkHtmlConverter> {
        error("No FlexmarkHtmlConverter provided")
    }

@Composable
@ReadOnlyComposable
fun currentHtmlToMdConverter(): FlexmarkHtmlConverter = LocalHtmlToMdConverterProvider.current
