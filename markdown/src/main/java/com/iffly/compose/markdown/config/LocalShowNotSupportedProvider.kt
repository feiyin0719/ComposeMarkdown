package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalShowNotSupportedProvider = staticCompositionLocalOf {
    false
}

@Composable
internal fun isShowNotSupported(): Boolean {
    return LocalShowNotSupportedProvider.current
}