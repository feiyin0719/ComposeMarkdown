package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.iffly.compose.markdown.ActionHandler

internal val LocalActionHandlerProvider = staticCompositionLocalOf<ActionHandler?> {
    null
}

@Composable
@ReadOnlyComposable
internal fun currentActionHandler(): ActionHandler? =
    LocalActionHandlerProvider.current