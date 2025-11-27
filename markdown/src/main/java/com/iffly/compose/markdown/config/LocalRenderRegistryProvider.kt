@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.iffly.compose.markdown.render.RenderRegistry

internal val LocalRenderRegistryProvider =
    staticCompositionLocalOf<RenderRegistry> {
        error("No RenderRegistry provided")
    }

@Composable
@ReadOnlyComposable
fun currentRenderRegistry(): RenderRegistry = LocalRenderRegistryProvider.current
