package com.iffly.compose.markdown.widget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList

/**
 * A composable that displays a gutter of line numbers for text with soft wrapping.
 * Line numbers are based on explicit '\n' in the original text; soft wraps do not create new line numbers.
 * @param originalLineStartOffset List of start offsets for each original line in the text.
 * @param visualLineStartOffset List of start offsets for each visual line in the laid-out
 * text.
 * @param modifier Modifier to be applied to the gutter.
 * @param lineNumberStyle TextStyle for the line numbers.
 * height from [lineNumberStyle].
 * @param continuationPlaceholder Placeholder string for continuation lines (soft-wrapped
 * lines).
 * @param calculateGutterLineNumber Function to map visual lines to original lines.
 */
@Composable
fun LineNumberGutter(
    originalLineStartOffset: ImmutableList<Int>,
    visualLineStartOffset: ImmutableList<Int>,
    modifier: Modifier = Modifier,
    lineNumberStyle: TextStyle =
        MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        ),
    continuationPlaceholder: String = " ",
    paddingValues: PaddingValues = PaddingValues(horizontal = 4.dp),
    calculateGutterLineNumber: CalculateGutterLineNumber = CalculateGutterLineNumber.DefaultCalculateGutterLineNumber,
) {
    val visualToOriginalLine =
        remember(originalLineStartOffset, visualLineStartOffset) {
            calculateGutterLineNumber(
                originalLineStartOffset = originalLineStartOffset,
                visualLineStartOffset = visualLineStartOffset,
            )
        }

    val gutterText =
        remember(visualToOriginalLine, continuationPlaceholder) {
            if (visualToOriginalLine.isEmpty()) {
                ""
            } else {
                buildString {
                    visualToOriginalLine.forEachIndexed { index, orig ->
                        if (orig != CalculateGutterLineNumber.EMPTY_LINE_INDEX) {
                            append(orig + 1)
                        } else {
                            append(continuationPlaceholder)
                        }
                        if (index < visualToOriginalLine.lastIndex) append('\n')
                    }
                }
            }
        }
    DisableSelection {
        Text(
            text = gutterText,
            modifier =
                modifier
                    .padding(paddingValues = paddingValues),
            style = lineNumberStyle,
        )
    }
}

/**
 * A function that maps visual line indices to original line indices.
 * Used to determine which line numbers to show in a gutter for soft-wrapped text.
 */
fun interface CalculateGutterLineNumber {
    companion object {
        /**
         * Special value indicating that show no line number for this visual line
         * It's usually used for continuation of the previous original line.
         */
        const val EMPTY_LINE_INDEX = Int.MIN_VALUE

        /**
         * Default implementation of [CalculateGutterLineNumber].
         * Maps visual lines to original lines based on explicit line breaks.
         */
        val DefaultCalculateGutterLineNumber =
            CalculateGutterLineNumber { originalLineStartOffset, visualLineStartOffset ->
                val visualToOriginalLine = mutableListOf<Int>()
                var originalLineIndex = 0
                var lastOriginalLineEnd = -1

                visualLineStartOffset.forEach { currentVisualLineStartOffset ->
                    // Determine if this visual line starts a new original line
                    if (currentVisualLineStartOffset > lastOriginalLineEnd) {
                        visualToOriginalLine.add(originalLineIndex)
                        originalLineIndex++
                        lastOriginalLineEnd =
                            originalLineStartOffset.getOrElse(originalLineIndex) { Int.MAX_VALUE } - 1
                    } else {
                        // This visual line is a continuation of the previous original line
                        visualToOriginalLine.add(EMPTY_LINE_INDEX)
                    }
                }

                visualToOriginalLine
            }
    }

    /**
     * Maps visual line indices to original line indices.
     * @param originalLineStartOffset List of start offsets for each original line in the text.
     * @param visualLineStartOffset List of start offsets for each visual line in the laid-out text.
     * @return A list where the index is the visual line index and the value is the original line index.
     */
    operator fun invoke(
        originalLineStartOffset: List<Int>,
        visualLineStartOffset: List<Int>,
    ): List<Int>
}
