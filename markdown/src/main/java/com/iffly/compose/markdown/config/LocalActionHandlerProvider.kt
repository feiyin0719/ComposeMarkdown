@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.ActionHandlerState

internal val LocalActionHandlerProvider =
    staticCompositionLocalOf<ActionHandlerState> {
        mutableStateOf(null)
    }

@Composable
@ReadOnlyComposable
fun currentActionHandler(): ActionHandlerState = LocalActionHandlerProvider.current
