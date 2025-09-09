package com.iffly.compose.markdown.style

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
data class TypographyStyle(
    val showSpace: Boolean = true,
    val spaceHeight: Dp = 8.dp,
    val breakLineHeight: Dp = 1.dp,
    val breakLineColor: Color = Color(0xFFE0E0E0),
    val textStyle: TextStyle? = null,
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
        color = Color(0xFF37474F),
        background = Color(0xFFF5F5F5),
    ),
    val link: TextLinkStyles = TextLinkStyles(
        style = SpanStyle(
            color = Color(0xFF1976D2),
            textDecoration = TextDecoration.Underline
        ),
        hoveredStyle = SpanStyle(
            color = Color(0xFF1565C0),
            textDecoration = TextDecoration.Underline
        ),
        focusedStyle = SpanStyle(
            color = Color(0xFF0D47A1),
            textDecoration = TextDecoration.Underline
        ),
        pressedStyle = SpanStyle(
            color = Color(0xFF0D47A1),
            textDecoration = TextDecoration.Underline
        ),
    ),
    val tableHeader: SpanStyle = SpanStyle(
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
    ),
    val tableCell: SpanStyle = SpanStyle(
        fontFamily = FontFamily.Default,
    ),
    val tableBorderColor: Color = Color.Gray,
    val tableTitleBackgroundColor: Color = Color.LightGray,
    val tableHeaderBackgroundColor: Color = Color.White,
    val tableRowHeaderBackgroundColor: Color = Color.White,
    val tableCopyStyle: TextStyle = TextStyle(
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = Color.Black,
    ),
    val codeTitleBackgroundColor: Color = Color.LightGray,
    val codeTitleStyle: TextStyle = TextStyle(
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = Color.Black,
    ),
    val codeCopyStyle: TextStyle = TextStyle(
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = Color.Blue,
    ),
    val blockQuoteContentBackgroundColor: Color = Color.LightGray,
    val blockQuoteBorderColor: Color = Color.Gray,
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

val DefaultTypographyStyle by lazy { TypographyStyle() }