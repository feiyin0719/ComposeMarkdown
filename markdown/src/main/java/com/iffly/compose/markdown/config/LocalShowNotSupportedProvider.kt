@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalShowNotSupportedProvider =
    staticCompositionLocalOf {
        false
    }

@Composable
@ReadOnlyComposable
internal fun isShowNotSupported(): Boolean = LocalShowNotSupportedProvider.current
