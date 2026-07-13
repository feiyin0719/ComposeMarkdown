package com.iffly.compose.markdown.widget.richtext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
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
import com.iffly.compose.markdown.util.toPlaceholderSp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

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
    val (fixedSizeInlineContent, adaptiveInlineContent) =
        remember(inlineContent) { groupInlineContent(inlineContent) }

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
            inlineContent = fixedSizeInlineContent,
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
    adaptiveInlineContent: ImmutableList<Pair<String, RichTextInlineContent.EmbeddedRichTextInlineContent>> = persistentListOf(),
    fixedSizeInlineContent: ImmutableMap<String, InlineTextContent> = persistentMapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val density = LocalDensity.current
    SubcomposeLayout(modifier = modifier) { constraints ->
        val measuredAdaptiveInlineContent: ImmutableMap<String, InlineTextContent> =
            measureAdaptiveInlineContentSize(adaptiveInlineContent, constraints, density)

        val combinedInlineContent =
            persistentMapOf<String, InlineTextContent>()
                .builder()
                .apply {
                    putAll(fixedSizeInlineContent)
                    putAll(measuredAdaptiveInlineContent)
                }.build()

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
                    inlineContent = combinedInlineContent,
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
    adaptiveInlineContent: ImmutableList<Pair<String, RichTextInlineContent.EmbeddedRichTextInlineContent>>,
    constraints: Constraints,
    density: Density,
): ImmutableMap<String, InlineTextContent> {
    // 1. Measure adaptive inline contents off-screen in this layout pass
    // adaptive inline contents are measured without constraints to get their "natural" size
    val adaptiveInlineConstraints =
        constraints.copy(
            minWidth = 0,
            minHeight = 0,
        )
    val placeables =
        subcompose("adaptive_inline") {
            adaptiveInlineContent.fastForEach { (id, value) ->
                key(id) {
                    Box(modifier = Modifier.wrapContentSize()) {
                        value.content(id)
                    }
                }
            }
        }.map { it.measure(adaptiveInlineConstraints) }

    val measuredAdaptiveInlineContent = persistentMapOf<String, InlineTextContent>().builder()
    adaptiveInlineContent.forEachIndexed { index, (id, value) ->
        val placeable = placeables.getOrNull(index)
        val width = placeable?.width?.let(density::toPlaceholderSp) ?: value.placeholder.width
        val height = placeable?.height?.let(density::toPlaceholderSp) ?: value.placeholder.height
        measuredAdaptiveInlineContent[id] =
            InlineTextContent(
                placeholder = value.placeholder.copy(width = width, height = height),
                children = { alternateText ->
                    key(id) {
                        value.content(alternateText)
                    }
                },
            )
    }
    return measuredAdaptiveInlineContent.build()
}

private fun groupInlineContent(
    inlineContent: ImmutableMap<String, RichTextInlineContent.EmbeddedRichTextInlineContent>,
): GroupedInlineContent {
    val fixed = persistentMapOf<String, InlineTextContent>().builder()
    val adaptive = persistentListOf<Pair<String, RichTextInlineContent.EmbeddedRichTextInlineContent>>().builder()
    inlineContent.forEach { (id, value) ->
        if (value.adjustSizeByContent) {
            adaptive.add(id to value)
        } else {
            fixed[id] =
                InlineTextContent(
                    placeholder = value.placeholder,
                    children = { alternateText ->
                        key(id) {
                            value.content(alternateText)
                        }
                    },
                )
        }
    }
    return GroupedInlineContent(fixed = fixed.build(), adaptive = adaptive.build())
}

private data class GroupedInlineContent(
    val fixed: ImmutableMap<String, InlineTextContent>,
    val adaptive: ImmutableList<Pair<String, RichTextInlineContent.EmbeddedRichTextInlineContent>>,
)
