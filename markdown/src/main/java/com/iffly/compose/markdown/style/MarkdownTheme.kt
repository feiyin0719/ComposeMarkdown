package com.iffly.compose.markdown.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift.Companion.Subscript
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
data class MarkdownTheme(
    val breakLineHeight: Dp = 1.dp,
    val breakLineColor: Color = Color(0xFFE0E0E0),
    val textStyle: TextStyle =
        TextStyle(
            fontSize = 18.sp,
            fontFamily = FontFamily.Default,
            color = Color.Black,
            lineHeight = 20.sp,
        ),
    val strongEmphasis: SpanStyle =
        SpanStyle(
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
        ),
    val emphasis: SpanStyle =
        SpanStyle(
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Default,
        ),
    val code: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = Color(0xFF37474F),
            background = Color(0xFFF5F5F5),
        ),
    val strikethrough: SpanStyle =
        SpanStyle(
            textDecoration = TextDecoration.LineThrough,
            fontFamily = FontFamily.Default,
        ),
    val subscript: SpanStyle =
        SpanStyle(
            fontFamily = FontFamily.Default,
            baselineShift = Subscript,
        ),
    val link: TextLinkStyles =
        TextLinkStyles(
            style =
                SpanStyle(
                    color = Color(0xFF1976D2),
                    textDecoration = TextDecoration.Underline,
                ),
            hoveredStyle =
                SpanStyle(
                    color = Color(0xFF1565C0),
                    textDecoration = TextDecoration.Underline,
                ),
            focusedStyle =
                SpanStyle(
                    color = Color(0xFF0D47A1),
                    textDecoration = TextDecoration.Underline,
                ),
            pressedStyle =
                SpanStyle(
                    color = Color(0xFF0D47A1),
                    textDecoration = TextDecoration.Underline,
                ),
        ),
    val headStyle: Map<Int, TextStyle> =
        mapOf(
            HEAD1 to
                TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    lineHeight = 36.sp,
                    color = Color.Black,
                ),
            HEAD2 to
                TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    lineHeight = 32.sp,
                    color = Color.Black,
                ),
            HEAD3 to
                TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    lineHeight = 28.sp,
                    color = Color.Black,
                ),
            HEAD4 to
                TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    lineHeight = 24.sp,
                    color = Color.Black,
                ),
            HEAD5 to
                TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    lineHeight = 22.sp,
                    color = Color.Black,
                ),
            HEAD6 to
                TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    lineHeight = 20.sp,
                    color = Color.Black,
                ),
        ),
    val listTheme: ListTheme = ListTheme(),
    val imageTheme: ImageTheme = ImageTheme(),
    val blockQuoteTheme: BlockQuoteTheme = BlockQuoteTheme(),
    val spacerTheme: SpacerTheme = SpacerTheme(),
    val tableTheme: TableTheme = TableTheme(),
    val codeBlockTheme: CodeBlockTheme = CodeBlockTheme(),
) {
    companion object {
        const val HEAD1 = 1
        const val HEAD2 = 2
        const val HEAD3 = 3
        const val HEAD4 = 4
        const val HEAD5 = 5
        const val HEAD6 = 6
    }
}

@Immutable
data class ImageTheme(
    val alignment: Alignment = Alignment.Center,
    val contentScale: ContentScale = ContentScale.Inside,
    val shape: Shape = RoundedCornerShape(8.dp),
    val modifier: Modifier = Modifier,
    val errorPlaceholderColor: Color = Color(0xFFE0E0E0),
)

@Immutable
data class BlockQuoteTheme(
    val borderColor: Color = Color.LightGray,
    val borderWidth: Dp = 5.dp,
    val backgroundColor: Color = Color(0xFFF5F5F5),
    val shape: Shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
    val padding: PaddingValues = PaddingValues(horizontal = 12.dp),
)

@Immutable
data class SpacerTheme(
    val showSpacer: Boolean = true,
    val spacerHeight: Dp = 12.dp,
)

@Immutable
data class ListTheme(
    val markerSpacerWidth: Dp = 4.dp,
    val showSpacerInTightList: Boolean = true,
    val tightListSpacerHeight: Dp = 4.dp,
    val markerTextStyle: TextStyle? =
        TextStyle(
            lineHeight = 24.sp,
            fontSize = 17.sp,
            textAlign = TextAlign.End,
        ),
)

data class TableTheme(
    val borderColor: Color = Color.Gray,
    val borderThickness: Dp = 1.dp,
    val titleBackgroundColor: Color = Color.LightGray,
    val tableHeaderBackgroundColor: Color = Color.White,
    val tableCellBackgroundColor: Color = Color.White,
    val cellTextStyle: TextStyle? =
        TextStyle(
            fontSize = 14.sp,
            fontFamily = FontFamily.Default,
            color = Color.Black,
        ),
    val headerTextStyle: TextStyle? =
        TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
            color = Color.Black,
        ),
    val copyTextStyle: TextStyle =
        TextStyle(
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Black,
        ),
    val shape: Shape = RoundedCornerShape(8.dp),
    val cellPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
)

/**
 * Theme for code blocks.
 * @param backgroundColor The background color of the code block.
 * @param shape The shape of the code block.
 * @param borderColor The border color of the code block.
 * @param borderWidth The border width of the code block.
 * @param contentTheme The theme for the code content.
 * @param codeTitleTextStyle The text style for the code title.
 * @param codeCopyTextStyle The text style for the code copy button.
 * @param blockModifier The modifier for the code block.
 * @param headerModifier The modifier for the code block header.
 * @param showHeader Whether to show the header.
 * @param showCopyButton Whether to show the copy button.
 * @see CodeContentTheme
 */
@Immutable
data class CodeBlockTheme(
    val backgroundColor: Color = Color.White,
    val shape: Shape = RoundedCornerShape(size = 16.dp),
    val borderColor: Color = Color.LightGray,
    val borderWidth: Dp = 0.5.dp,
    val contentTheme: CodeContentTheme = CodeContentTheme(),
    val codeTitleTextStyle: TextStyle =
        TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
        ),
    val codeCopyTextStyle: TextStyle =
        TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Blue,
        ),
    val blockModifier: Modifier =
        Modifier.padding(
            vertical = 12.dp,
        ),
    val headerModifier: Modifier = Modifier.padding(horizontal = 17.dp),
    val showHeader: Boolean = true,
    val showCopyButton: Boolean = true,
) {
    /**
     * Theme for the code content within a code block.
     * @param showLineNumber Whether to show line numbers.
     * @param softWrap Whether to enable soft wrapping.
     * If false, horizontal scrolling will be enabled.
     * @param maxLines The maximum number of lines to display.
     * @param minLines The minimum number of lines to display.
     * @param contentPadding The padding around the code content.
     * @param lineNumberPadding The padding around the line numbers.
     * @param overflow The overflow behavior for the code content.
     * @param codeTextStyle The text style for the code content.
     * @param lineNumberTextStyle The text style for the line numbers.
     * @param modifier The modifier for the code content.
     * @param height The fixed height for the code content, if has fixed height,
     * it will support scrolling.
     */
    @Immutable
    data class CodeContentTheme(
        val showLineNumber: Boolean = true,
        val softWrap: Boolean = true,
        val maxLines: Int = Int.MAX_VALUE,
        val minLines: Int = 1,
        val contentPadding: PaddingValues = PaddingValues(4.dp),
        val lineNumberPadding: PaddingValues =
            PaddingValues(
                start = 4.dp,
                top = 4.dp,
                bottom = 4.dp,
                end = 16.dp,
            ),
        val overflow: TextOverflow = TextOverflow.Clip,
        val codeTextStyle: TextStyle =
            TextStyle(
                fontSize = 14.sp,
                lineHeight = 18.sp,
            ),
        val lineNumberTextStyle: TextStyle =
            TextStyle(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = Color.Gray,
                textAlign = TextAlign.End,
            ),
        val modifier: Modifier = Modifier.padding(start = 17.dp, end = 17.dp, top = 17.dp),
        val height: Dp? = null,
    )
}
