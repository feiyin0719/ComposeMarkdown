package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.iffly.compose.markdown.render.BlockRenderers
import com.iffly.compose.markdown.render.defaultBlockRenderers

internal val LocalBlockRenderersProvider =
    staticCompositionLocalOf { defaultBlockRenderers }

@Composable
internal fun currentBlockRenderers(): BlockRenderers =
    LocalBlockRenderersProvider.current