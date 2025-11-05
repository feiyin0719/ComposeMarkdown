package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.iffly.compose.markdown.style.DefaultTypographyStyle
import com.iffly.compose.markdown.style.TypographyStyle

internal val LocalTypographyStyleProvider = staticCompositionLocalOf<TypographyStyle?> { null }

@Composable
@ReadOnlyComposable
internal fun currentTypographyStyle(): TypographyStyle =
    LocalTypographyStyleProvider.current ?: DefaultTypographyStyle