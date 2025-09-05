package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.iffly.compose.markdown.render.InlineNodeStringBuilders
import com.iffly.compose.markdown.render.defaultInlineNodeStringBuilders

internal val LocalInlineNodeStringBuildersProvider =
    staticCompositionLocalOf { defaultInlineNodeStringBuilders }

@Composable
internal fun currentInlineNodeStringBuilders(): InlineNodeStringBuilders =
    LocalInlineNodeStringBuildersProvider.current