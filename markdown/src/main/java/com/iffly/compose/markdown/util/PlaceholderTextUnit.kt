package com.iffly.compose.markdown.util

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Converts a measured pixel size to a linearly scaled `sp` value for a text placeholder.
 *
 * Unlike Compose's general-purpose `toSp`, this conversion intentionally avoids the platform's
 * non-linear font scaling curve. Placeholder dimensions describe measured layout geometry rather
 * than font glyph sizes.
 */
fun Density.toPlaceholderSp(px: Int): TextUnit = (px.toFloat() / density / fontScale).sp
