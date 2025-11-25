package com.iffly.compose.markdown.render

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * Text measurement context used during markdown rendering.
 * @param maxTextWidth The maximum width available for text rendering.
 * @param density The density information for converting between dp and pixels.
 * @param textMeasurer The TextMeasurer instance for measuring text.
 */
data class TextMeasureContext(
    val maxTextWidth: Dp,
    val density: Density,
    val textMeasurer: TextMeasurer
)