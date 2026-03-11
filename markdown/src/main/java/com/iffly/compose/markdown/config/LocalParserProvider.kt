@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.vladsch.flexmark.parser.Parser

internal val LocalParserProvider =
    staticCompositionLocalOf<Parser> {
        error("No Parser provided")
    }

@Composable
@ReadOnlyComposable
fun currentParser(): Parser = LocalParserProvider.current
