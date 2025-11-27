package com.iffly.compose.markdown.widget.richtext

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * A Text composable that automatically adjusts line height for inline content with specified height in sp.
 *
 * @param text The annotated string to display.
 * @param modifier The modifier to be applied to the Text.
 * @param color The color of the text.
 * @param fontSize The size of the font.
 * @param fontStyle The style of the font.
 * @param fontWeight The weight of the font.
 * @param fontFamily The family of the font.
 * @param letterSpacing The spacing between letters.
 * @param textDecoration The decoration to be applied to the text.
 * @param textAlign The alignment of the text.
 * @param lineHeight The height of the lines.
 * @param overflow The overflow behavior of the text.
 * @param softWrap Whether the text should wrap softly.
 * @param maxLines The maximum number of lines to display.
 * @param minLines The minimum number of lines to display.
 * @param inlineContent The inline content to be displayed within the text.
 * @param onTextLayout Callback that is invoked when the text layout is calculated.
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
    var adjustLineHeightRequestList by remember {
        mutableStateOf<List<AdjustLineHeightRequest>>(listOf())
    }
    val inlineContentAnnotations =
        remember(text) {
            text.getInlineContentAnnotations()
        }

    var adjustedText by remember {
        mutableStateOf(text)
    }

    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        snapshotFlow {
            Pair(adjustedText, adjustLineHeightRequestList)
        }.distinctUntilChanged()
            .collect { (currentText, requests) ->
                if (requests.isEmpty()) {
                    return@collect
                }
                val sortedRequests = requests.sortedByDescending { it.startIndex }
                val newText =
                    buildAnnotatedString {
                        append(currentText)
                        for (request in sortedRequests) {
                            addStyle(
                                style =
                                    ParagraphStyle(
                                        lineHeight = request.lineHeight,
                                    ),
                                start = request.startIndex,
                                end = request.endIndex,
                            )
                        }
                    }
                adjustedText = newText
                adjustLineHeightRequestList = listOf()
            }
    }

    Text(
        text = adjustedText,
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
        onTextLayout = { layoutResult: TextLayoutResult ->
            adjustLineHeightRequestList =
                calculateAdjustLineHeightRequests(
                    inlineTextContentAnnotations = inlineContentAnnotations,
                    layoutResult = layoutResult,
                    inlineContent = inlineContent,
                    density = density,
                )
            onTextLayout(layoutResult)
        },
        modifier = modifier,
        style = style,
    )
}

private const val INLINE_CONTENT_TAG = "androidx.compose.foundation.text.inlineContent"

private fun AnnotatedString.getInlineContentAnnotations(): List<AnnotatedString.Range<String>> =
    this.getStringAnnotations(tag = INLINE_CONTENT_TAG, start = 0, end = this.length)

private fun calculateAdjustLineHeightRequests(
    inlineTextContentAnnotations: List<AnnotatedString.Range<String>>,
    layoutResult: TextLayoutResult,
    inlineContent: Map<String, InlineTextContent>,
    density: Density,
): List<AdjustLineHeightRequest> {
    val adjustLineHeightRequestMap = mutableMapOf<Int, AdjustLineHeightRequest>()
    for (annotation in inlineTextContentAnnotations) {
        val inlineContentItem = inlineContent[annotation.item] ?: continue
        if (!inlineContentItem.placeholder.height.isSp) {
            // only adjust line height for inline content with height in sp
            continue
        }
        val startIndex = annotation.start
        // get the line number where the inline content starts
        val startLineNumber = layoutResult.getLineForOffset(startIndex)
        // get the line height of that line
        val lineHeight =
            layoutResult.getLineBottom(startLineNumber) - layoutResult.getLineTop(startLineNumber)
        val existingRequest = adjustLineHeightRequestMap[startLineNumber]
        val lineHeightSp = with(density) { lineHeight.toSp() }
        val existingLineHeight = existingRequest?.lineHeight ?: 0.sp
        val inlineContentLineHeight = inlineContentItem.placeholder.height
        // get the max of line height and existing line height
        val finalLineHeight =
            if (lineHeightSp > existingLineHeight) {
                lineHeightSp
            } else {
                existingLineHeight
            }
        if (inlineContentLineHeight > finalLineHeight) {
            adjustLineHeightRequestMap[startLineNumber] =
                AdjustLineHeightRequest(
                    startIndex = layoutResult.getLineStart(startLineNumber),
                    endIndex = layoutResult.getLineEnd(startLineNumber),
                    lineHeight = inlineContentLineHeight,
                )
        }
    }
    return adjustLineHeightRequestMap.values.toList()
}

private data class AdjustLineHeightRequest(
    val startIndex: Int,
    val endIndex: Int,
    val lineHeight: TextUnit,
)
