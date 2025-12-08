package com.iffly.compose.markdown.widget.richtext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMapIndexed
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

    if (adaptiveInlineContent.isEmpty()) {
        // Fast path: no adaptive content, fall back to simple text rendering
        AutoLineHeightText(
            text = text,
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
            inlineContent = fixedSizeInlineContent.toImmutableMap(),
            onTextLayout = onTextLayout,
            style = style,
        )
    } else {
        TextWithAdaptiveInlineContent(
            text = text,
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
            adaptiveInlineContent = adaptiveInlineContent,
            fixedSizeInlineContent = fixedSizeInlineContent,
            onTextLayout = onTextLayout,
            style = style,
        )
    }
}

@Composable
private fun TextWithAdaptiveInlineContent(
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
    adaptiveInlineContent: Map<String, RichTextInlineContent.EmbeddedRichTextInlineContent> = persistentMapOf(),
    fixedSizeInlineContent: Map<String, InlineTextContent> = persistentMapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val density = LocalDensity.current
    SubcomposeLayout(modifier = modifier) { constraints ->
        val measuredAdaptiveInlineContent: Map<String, InlineTextContent> =
            measureAdaptiveInlineContentSize(adaptiveInlineContent, constraints, density)

        val combinedInlineContent = fixedSizeInlineContent + measuredAdaptiveInlineContent

        // 2. Compose and measure the text with the final inlineContent in the same layout pass
        val textPlaceables =
            subcompose("text") {
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
            }.map { it.measure(constraints) }

        val textPlaceable = textPlaceables.singleOrNull()

        val width = textPlaceable?.width ?: constraints.minWidth
        val height = textPlaceable?.height ?: constraints.minHeight

        layout(width, height) {
            textPlaceable?.place(0, 0)
        }
    }
}

private fun SubcomposeMeasureScope.measureAdaptiveInlineContentSize(
    adaptiveInlineContent: Map<String, RichTextInlineContent.EmbeddedRichTextInlineContent>,
    constraints: Constraints,
    density: Density,
): Map<String, InlineTextContent> {
    // 1. Measure adaptive inline contents off-screen in this layout pass
    val adaptiveKeys = adaptiveInlineContent.keys.toList()
    // adaptive inline contents are measured without constraints to get their "natural" size
    val adaptiveInlineConstraints =
        constraints.copy(
            minWidth = 0,
            minHeight = 0,
        )
    val placeables =
        subcompose("adaptive_inline") {
            adaptiveKeys.fastForEach { key ->
                val value = adaptiveInlineContent.getValue(key)
                Box(modifier = Modifier.wrapContentSize()) {
                    value.content(key)
                }
            }
        }.map { it.measure(adaptiveInlineConstraints) }

    val measuredAdaptiveInlineContent: Map<String, InlineTextContent> =
        adaptiveKeys
            .fastMapIndexed { index, key ->
                val value = adaptiveInlineContent.getValue(key)
                val placeable = placeables.getOrNull(index)
                val width =
                    placeable?.width?.let { with(density) { it.toSp() } }
                        ?: value.placeholder.width
                val height =
                    placeable?.height?.let { with(density) { it.toSp() } }
                        ?: value.placeholder.height

                key to
                    InlineTextContent(
                        placeholder =
                            value.placeholder.copy(
                                width = width,
                                height = height,
                            ),
                        children = value.content,
                    )
            }.toMap()
    return measuredAdaptiveInlineContent
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
