package com.iffly.compose.markdown.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

data class TypographyStyle(
    val body: SpanStyle = SpanStyle(),
    val strongEmphasis: SpanStyle = SpanStyle(
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
    ),
    val emphasis: SpanStyle = SpanStyle(
        fontStyle = FontStyle.Italic,
        fontFamily = FontFamily.Default,
    ),
    val code: SpanStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
    ),
    val link: TextLinkStyles = TextLinkStyles(
        style = SpanStyle(
            color = Color(0xFF1976D2), // Blue color for normal links
            textDecoration = TextDecoration.Underline
        ),
        hoveredStyle = SpanStyle(
            color = Color(0xFF1565C0), // Darker blue for hover
            textDecoration = TextDecoration.Underline
        ),
        focusedStyle = SpanStyle(
            color = Color(0xFF0D47A1), // Even darker blue for focus
            textDecoration = TextDecoration.Underline
        ),
        pressedStyle = SpanStyle(
            color = Color(0xFF0D47A1), // Same as focused for pressed
            textDecoration = TextDecoration.Underline
        ),
    ),
    val tableHeader: SpanStyle = SpanStyle(
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
    ),
    val head: Map<Int, SpanStyle> = mapOf(
        1 to SpanStyle(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        ),
        2 to SpanStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        ),
        3 to SpanStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        ),
        4 to SpanStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        ),
        5 to SpanStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        ),
        6 to SpanStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        ),
    ),
)

val DefaultTypographyStyle = TypographyStyle()

val LocalTypographyStyleProvider = staticCompositionLocalOf<TypographyStyle?> { null }

@Composable
fun currentTypographyStyle(): TypographyStyle =
    LocalTypographyStyleProvider.current ?: DefaultTypographyStyle