
package com.iffly.compose.markdown.widget

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    text: String,
    modifier: Modifier = Modifier,
    lineNumberStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = Color.Gray,
    ),
    textStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    contentPadding: PaddingValues = PaddingValues(4.dp),
    lineNumberPadding: PaddingValues = PaddingValues(
        start = 4.dp,
        top = 4.dp,
        bottom = 4.dp,
        end = 16.dp,
    ),
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    showLineNumber: Boolean = true,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    var textLayoutResult: TextLayoutResult? by remember { mutableStateOf(null) }
    val originalLineStartOffset = remember(text) {
        text.withIndex()
            .filter { it.value == '\n' }
            .map { it.index + 1 }
            .toMutableList()
            .apply { add(0, 0) } // Add start of first line
            .toList()
    }
    val visualLineStartOffset = remember(textLayoutResult) {
        textLayoutResult?.let { result ->
            val lineCount = result.lineCount
            List(lineCount) { lineIndex ->
                result.getLineStart(lineIndex)
            }
        } ?: emptyList()
    }

    val scrollModifier = if (!softWrap) {
        val scrollState = rememberScrollState()
        Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    } else {
        Modifier
    }

    Row(modifier = modifier) {
        if (showLineNumber) {
            LineNumberGutter(
                originalLineStartOffset = originalLineStartOffset,
                visualLineStartOffset = visualLineStartOffset,
                modifier = Modifier.wrapContentSize(),
                lineNumberStyle = lineNumberStyle.copy(
                    lineHeight = textStyle.lineHeight,
                ),
                paddingValues = lineNumberPadding,
            )
        }

        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .padding(contentPadding)
                .then(scrollModifier),
            style = textStyle,
            softWrap = softWrap,
            overflow = overflow,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = {
                textLayoutResult = it
                onTextLayout?.invoke(it)
            },
        )
    }
}
