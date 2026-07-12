@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/** Dependencies supplied by the caller for custom renderers and node string builders. */
val LocalRenderDependencies = staticCompositionLocalOf<Map<String, Any>> { emptyMap() }

/** Returns the dependencies supplied to the current Markdown rendering tree. */
@Composable
@ReadOnlyComposable
fun currentRenderDependencies(): Map<String, Any> = LocalRenderDependencies.current
