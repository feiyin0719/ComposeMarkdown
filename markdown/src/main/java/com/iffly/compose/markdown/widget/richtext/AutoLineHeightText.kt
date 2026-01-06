package com.iffly.compose.markdown.widget.richtext

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext

/**
 * A Composable that displays text with automatic line height adjustment for inline content.
 * It is used to fix the overlap issue when inline content has a larger height than the surrounding text.
 *
 * This Composable analyzes the inline content within the provided [text] and adjusts the line height
 * of the text segments containing inline content to ensure proper vertical alignment.
 *
 * @param text The annotated string containing the text to be displayed.
 * @param modifier The modifier to be applied to the text.
 * @param color The color of the text.
 * @param fontSize The size of the font.
 * @param fontStyle The style of the font.
 * @param fontWeight The weight of the font.
 * @param fontFamily The font family of the text.
 * @param letterSpacing The spacing between letters.
 * @param textDecoration The decoration to be applied to the text.
 * @param textAlign The alignment of the text.
 * @param lineHeight The height of each line of text.
 * @param overflow The overflow behavior of the text.
 * @param softWrap Whether the text should wrap softly.
 * @param maxLines The maximum number of lines to be displayed.
 * @param minLines The minimum number of lines to be displayed.
 * @param inlineContent A map of inline content to be used within the text.
 * @param onTextLayout A callback that is invoked when the text layout is calculated.
 * @param style The style to be applied to the text.
 */
@Composable
fun AutoLineHeightText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: ImmutableMap<String, InlineTextContent> = persistentMapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val (adjustedText, textLayoutResultState) =
        rememberAdjustedText(
            text = text,
        )

    Text(
        text = adjustedText,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = { layoutResult ->
            textLayoutResultState.value = layoutResult
            onTextLayout(layoutResult)
        },
        style = style,
    )
}

@Composable
private fun rememberAdjustedText(text: AnnotatedString): Pair<AnnotatedString, MutableState<TextLayoutResult?>> {
    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }
    var adjustedText by remember(text) { mutableStateOf(text) }
    val density = LocalDensity.current
    LaunchedEffect(text) {
        snapshotFlow {
            textLayoutResultState.value
        }.distinctUntilChanged()
            .mapNotNull { layoutResult ->
                layoutResult?.let {
                    if (adjustedText != it.layoutInput.text) {
                        // Text has changed, skip processing
                        return@let null
                    }
                    withContext(Dispatchers.Default) {
                        calculateAdjustLineHeightRequest(
                            layoutResult = it,
                            density = density,
                        ).takeIf { map -> map.isNotEmpty() }?.let { requestMap ->
                            buildAdjustLineHeightText(
                                currentText = it.layoutInput.text,
                                requests = requestMap.values.toList(),
                            )
                        }
                    }
                }
            }.collectLatest {
                adjustedText = it
            }
    }
    return Pair(adjustedText, textLayoutResultState)
}

private fun buildAdjustLineHeightText(
    currentText: AnnotatedString,
    requests: List<AdjustLineHeightRequest>,
): AnnotatedString {
    val newText =
        buildAnnotatedString {
            var lastIndex = 0
            requests
                .sortedBy {
                    it.startIndex
                }.fastForEach {
                    if (it.startIndex > lastIndex) {
                        // add new ParagraphStyle will create a new line, so we need to remove the last line separator
                        // to avoid extra empty line
                        appendAndRemoveLastLineSeparator(
                            currentText.subSequence(
                                lastIndex,
                                it.startIndex,
                            ),
                        )
                    }
                    withStyle(ParagraphStyle(lineHeight = it.lineHeight)) {
                        // add new ParagraphStyle will create a new line, so we need to remove the last line separator
                        // to avoid extra empty line
                        appendAndRemoveLastLineSeparator(
                            currentText.subSequence(
                                it.startIndex,
                                it.endIndex,
                            ),
                        )
                    }
                    lastIndex = it.endIndex
                }
            if (lastIndex < currentText.length) {
                append(currentText.subSequence(lastIndex, currentText.length))
            }
        }
    return newText
}

private fun AnnotatedString.Builder.appendAndRemoveLastLineSeparator(subSegment: AnnotatedString) {
    if (subSegment.lastOrNull() == '\n') {
        append(subSegment.subSequence(0, subSegment.length - 1))
    } else {
        append(subSegment)
    }
}

private fun calculateAdjustLineHeightRequest(
    layoutResult: TextLayoutResult,
    density: Density,
): MutableMap<Int, AdjustLineHeightRequest> {
    val adjustLineHeightRequestMap = mutableMapOf<Int, AdjustLineHeightRequest>()
    val annotationRanges = layoutResult.layoutInput.placeholders

    annotationRanges.fastForEach { annotation ->
        if (!annotation.item.height.isSp) {
            return@fastForEach
        }

        val lineNumber = layoutResult.getLineForOffset(annotation.start)
        val textLineHeight =
            layoutResult.getLineBottom(lineNumber) - layoutResult.getLineTop(lineNumber)
        val textLineHeightSp =
            with(density) {
                textLineHeight.toSp()
            }
        val existingRequestLineHeight = adjustLineHeightRequestMap[lineNumber]?.lineHeight ?: 0.sp

        val inlineContentLineHeight = annotation.item.height
        val maxRequestLineHeight =
            if (inlineContentLineHeight > existingRequestLineHeight) {
                inlineContentLineHeight
            } else {
                existingRequestLineHeight
            }

        if (maxRequestLineHeight > textLineHeightSp) {
            adjustLineHeightRequestMap[lineNumber] =
                AdjustLineHeightRequest(
                    startIndex = layoutResult.getLineStart(lineNumber),
                    endIndex = layoutResult.getLineEnd(lineNumber),
                    lineHeight = maxRequestLineHeight,
                )
        }
    }
    return adjustLineHeightRequestMap
}

private data class AdjustLineHeightRequest(
    val startIndex: Int,
    val endIndex: Int,
    val lineHeight: TextUnit,
)
