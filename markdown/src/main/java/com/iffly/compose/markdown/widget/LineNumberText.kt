package com.iffly.compose.markdown.widget

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * The composable that displays text with a gutter showing line numbers.
 * Handles soft-wrapping and maps visual lines to original lines for line numbering.
 * @param text The text content to display.
 * @param modifier The modifier to be applied to the layout.
 * @param lineNumberStyle The text style for line numbers.
 * @param textStyle The text style for the main text content.
 * @param contentPadding The padding around the main text content.
 * @param lineNumberPadding The padding around the line numbers in the gutter.
 * @param overflow The overflow behavior for the main text content.
 * @param softWrap Whether the main text content should soft-wrap.
 * If set to false, line numbers will correspond directly to line breaks in the text.
 * And it will enable horizontal scrolling.
 * @param maxLines The maximum number of lines for the main text content.
 * @param minLines The minimum number of lines for the main text content.
 * @param showLineNumber Whether to show line numbers in the gutter.
 * @param onTextLayout Optional callback for text layout results.
 */
@Composable
fun LineNumberText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    lineNumberStyle: TextStyle =
        MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color.Gray,
        ),
    textStyle: TextStyle =
        MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
    contentPadding: PaddingValues = remember { PaddingValues(4.dp) },
    lineNumberPadding: PaddingValues =
        remember {
            PaddingValues(
                start = 4.dp,
                top = 4.dp,
                bottom = 4.dp,
                end = 16.dp,
            )
        },
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    showLineNumber: Boolean = true,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    if (showLineNumber) {
        NumberedText(
            text = text,
            modifier = modifier,
            lineNumberStyle = lineNumberStyle,
            textStyle = textStyle,
            contentPadding = contentPadding,
            lineNumberPadding = lineNumberPadding,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout,
        )
    } else {
        PlainText(
            text = text,
            modifier = modifier,
            textStyle = textStyle,
            contentPadding = contentPadding,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout,
        )
    }
}

@Composable
private fun NumberedText(
    text: AnnotatedString,
    lineNumberStyle: TextStyle,
    textStyle: TextStyle,
    contentPadding: PaddingValues,
    lineNumberPadding: PaddingValues,
    overflow: TextOverflow,
    softWrap: Boolean,
    maxLines: Int,
    minLines: Int,
    modifier: Modifier = Modifier,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    var visualLineStartOffset by remember(text.text) {
        mutableStateOf<ImmutableList<Int>>(persistentListOf())
    }
    val originalLineStartOffset =
        remember(text.text) {
            buildList {
                add(0)
                text.text.forEachIndexed { index, character ->
                    if (character == '\n') add(index + 1)
                }
            }.toImmutableList()
        }
    val resolvedLineNumberStyle =
        remember(lineNumberStyle, textStyle.lineHeight) {
            lineNumberStyle.copy(lineHeight = textStyle.lineHeight)
        }

    Row(modifier = modifier) {
        LineNumberGutter(
            originalLineStartOffset = originalLineStartOffset,
            visualLineStartOffset = visualLineStartOffset,
            modifier = Modifier.wrapContentSize(),
            lineNumberStyle = resolvedLineNumberStyle,
            paddingValues = lineNumberPadding,
        )
        CodeText(
            text = text,
            textStyle = textStyle,
            contentPadding = contentPadding,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = { result ->
                val newVisualLineStartOffset =
                    List(result.lineCount) { lineIndex -> result.getLineStart(lineIndex) }
                        .toImmutableList()
                if (visualLineStartOffset != newVisualLineStartOffset) {
                    visualLineStartOffset = newVisualLineStartOffset
                }
                onTextLayout?.invoke(result)
            },
        )
    }
}

@Composable
private fun PlainText(
    text: AnnotatedString,
    textStyle: TextStyle,
    contentPadding: PaddingValues,
    overflow: TextOverflow,
    softWrap: Boolean,
    maxLines: Int,
    minLines: Int,
    modifier: Modifier = Modifier,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    Row(modifier = modifier) {
        CodeText(
            text = text,
            textStyle = textStyle,
            contentPadding = contentPadding,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout ?: emptyOnTextLayout,
        )
    }
}

@Composable
private fun RowScope.CodeText(
    text: AnnotatedString,
    textStyle: TextStyle,
    contentPadding: PaddingValues,
    overflow: TextOverflow,
    softWrap: Boolean,
    maxLines: Int,
    minLines: Int,
    onTextLayout: (TextLayoutResult) -> Unit = emptyOnTextLayout,
) {
    val scrollModifier =
        if (!softWrap) {
            val scrollState = rememberScrollState()
            Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        } else {
            Modifier
        }

    Text(
        text = text,
        modifier =
            Modifier
                .weight(1f)
                .padding(contentPadding)
                .then(scrollModifier),
        style = textStyle,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
    )
}

private val emptyOnTextLayout: (TextLayoutResult) -> Unit = {}
