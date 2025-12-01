package com.iffly.compose.markdown.widget.richtext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap

/**
 * A Composable that displays rich text with support for both fixed-size and adaptive inline content.
 * It calculates the size of adaptive inline content at runtime and adjusts the text layout accordingly.
 * @param text The annotated string containing the rich text to be displayed.
 * @param modifier The modifier to be applied to the rich text.
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
 * If the inlineContent key not changed, the content will not be re-measured to avoid recomposition loop.
 * @param onTextLayout A callback that is invoked when the text layout is calculated.
 * @param style The style to be applied to the text.
 * @see AutoLineHeightText
 * @see RichTextInlineContent
 */
@Composable
fun AdaptiveInlineContentText(
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
    inlineContent: ImmutableMap<String, RichTextInlineContent.EmbeddedRichTextInlineContent> = persistentMapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val (fixedSizeInlineContent, adaptiveInlineContent) = groupInlineContent(inlineContent)

    // use keys as dependency to avoid recomposition loop
    // We assume that when the key is the same, the content will not change.
    val calculatedInlineContent =
        remember(adaptiveInlineContent.keys) {
            mutableStateOf(
                adaptiveInlineContent.mapValues {
                    InlineTextContent(
                        placeholder = it.value.placeholder,
                        children = it.value.content,
                    )
                },
            )
        }
    val onContentMeasure =
        remember(calculatedInlineContent) {
            { measuredContent: Map<String, InlineTextContent> ->
                calculatedInlineContent.value = measuredContent
            }
        }

    Box(modifier = modifier) {
        if (adaptiveInlineContent.isNotEmpty()) {
            AdaptiveInlineContentMeasurer(
                adaptiveInlineContent = adaptiveInlineContent.toImmutableMap(),
                onContentMeasure = onContentMeasure,
            )
        }

        val combinedInlineContent = fixedSizeInlineContent + calculatedInlineContent.value
        AutoLineHeightText(
            text = text,
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
            inlineContent = combinedInlineContent.toImmutableMap(),
            onTextLayout = onTextLayout,
            style = style,
        )
    }
}

private fun groupInlineContent(
    inlineContent: Map<String, RichTextInlineContent.EmbeddedRichTextInlineContent>,
): Pair<Map<String, InlineTextContent>, Map<String, RichTextInlineContent.EmbeddedRichTextInlineContent>> {
    val fixedSizeInlineContent =
        inlineContent
            .mapNotNull { (key, value) ->
                if (!value.adjustSizeByContent) {
                    key to
                        InlineTextContent(
                            placeholder = value.placeholder,
                            children = value.content,
                        )
                } else {
                    null
                }
            }.toMap()

    val adaptiveInlineContent =
        inlineContent
            .mapNotNull { (key, value) ->
                if (value.adjustSizeByContent) {
                    key to value
                } else {
                    null
                }
            }.toMap()
    return Pair(fixedSizeInlineContent, adaptiveInlineContent)
}

/**
 * A Composable that measures the size of adaptive inline content and reports the measured sizes
 * back via a callback.
 * It will layout the inline content offscreen to get their sizes.
 * @param adaptiveInlineContent A map of adaptive inline content to be measured.
 * @param onContentMeasure A callback that is invoked with the measured inline content sizes.
 * @param modifier The modifier to be applied to the measurer.
 */
@Composable
private fun AdaptiveInlineContentMeasurer(
    adaptiveInlineContent: ImmutableMap<String, RichTextInlineContent.EmbeddedRichTextInlineContent>,
    onContentMeasure: (Map<String, InlineTextContent>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    Layout(content = {
        adaptiveInlineContent.forEach { (key, value) ->
            Box(modifier = Modifier.wrapContentSize()) {
                value.content(key)
            }
        }
    }, modifier = modifier) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val measuredContent =
            adaptiveInlineContent.mapValues { (key, value) ->
                val placeable = placeables.getOrNull(adaptiveInlineContent.keys.indexOf(key))
                val width =
                    placeable?.width?.let {
                        with(density) { it.toSp() }
                    } ?: value.placeholder.width
                val height =
                    placeable?.height?.let {
                        with(density) { it.toSp() }
                    } ?: value.placeholder.height
                InlineTextContent(
                    placeholder =
                        value.placeholder.copy(
                            width = width,
                            height = height,
                        ),
                    children = value.content,
                )
            }
        onContentMeasure(measuredContent)
        layout(0, 0) {
        }
    }
}
