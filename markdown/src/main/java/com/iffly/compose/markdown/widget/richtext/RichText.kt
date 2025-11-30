package com.iffly.compose.markdown.widget.richtext

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.iffly.compose.markdown.util.StringExt
import com.iffly.compose.markdown.widget.SelectionFormatText
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap

@Composable
fun RichText(
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
    inlineContent: ImmutableMap<String, RichTextInlineContent> = persistentMapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val standaloneInlineContent =
        inlineContent
            .mapNotNull { (key, value) ->
                if (value is RichTextInlineContent.StandaloneInlineContent) {
                    key to value
                } else {
                    null
                }
            }.toMap()
            .toImmutableMap()
    val textSegments =
        rememberRichTextSegment(
            text = text,
            standaloneInlineContent = standaloneInlineContent,
        )
    val inlineTextContent =
        inlineContent
            .mapNotNull { (key, value) ->
                if (value is RichTextInlineContent.FixedSizeInlineContent) {
                    key to
                        InlineTextContent(
                            placeholder = value.placeholder,
                            children = value.content,
                        )
                } else {
                    null
                }
            }.toMap()
            .toImmutableMap()

    Column(modifier = modifier) {
        textSegments.forEach {
            when (it) {
                is RichTextSegment.Text -> {
                    AutoLineHeightText(
                        text = it.text,
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
                        inlineContent = inlineTextContent,
                        onTextLayout = onTextLayout,
                        style = style,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                    )
                }

                is RichTextSegment.InlineContentSegment -> {
                    val content = it.standaloneInlineTextContent
                    content.content(
                        content.modifier,
                    )
                }
            }
            // Add invisible line break to keep selection-copy format correct across segments
            SelectionFormatText(StringExt.LINE_SEPARATOR)
        }
    }
}

@Composable
private fun rememberRichTextSegment(
    text: AnnotatedString,
    standaloneInlineContent: ImmutableMap<String, RichTextInlineContent.StandaloneInlineContent>,
): List<RichTextSegment> =
    remember(text, standaloneInlineContent) {
        buildRichTextSegments(text, standaloneInlineContent)
    }

private fun buildRichTextSegments(
    text: AnnotatedString,
    standaloneInlineContent: Map<String, RichTextInlineContent.StandaloneInlineContent>,
): List<RichTextSegment> {
    val standaloneInlineTextContentAnnotations = text.getStandaloneInlineTextContentAnnotations()
    // filter out annotations that don't have corresponding inline content
    val validAnnotations =
        standaloneInlineTextContentAnnotations.filter { annotation ->
            standaloneInlineContent.containsKey(annotation.item)
        }

    return buildList {
        validAnnotations
            .fold(0) { lastIndex, annotation ->
                if (annotation.start > lastIndex) {
                    add(RichTextSegment.Text(text.subSequence(lastIndex, annotation.start)))
                }
                standaloneInlineContent[annotation.item]?.let {
                    add(RichTextSegment.InlineContentSegment(it))
                }
                annotation.end
            }.let { lastIndex ->
                if (lastIndex < text.length) {
                    add(RichTextSegment.Text(text.subSequence(lastIndex, text.length)))
                }
            }
    }
}

private sealed interface RichTextSegment {
    data class Text(
        val text: AnnotatedString,
    ) : RichTextSegment

    data class InlineContentSegment(
        val standaloneInlineTextContent: RichTextInlineContent.StandaloneInlineContent,
    ) : RichTextSegment
}
