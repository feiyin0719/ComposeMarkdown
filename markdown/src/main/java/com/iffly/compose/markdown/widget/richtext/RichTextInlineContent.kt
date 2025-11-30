package com.iffly.compose.markdown.widget.richtext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.Placeholder

/**
 * Represents inline content that can be inserted into [RichText].
 *
 * @see RichText
 */
sealed interface RichTextInlineContent {
    /**
     * Represents inline content with a fixed size that can be inserted into [RichText].
     * @param placeholder The placeholder defining the size and alignment of the inline content.
     * @param content The composable content of the inline content.
     * @see RichText
     */
    @Immutable
    data class FixedSizeInlineContent(
        val placeholder: Placeholder,
        val content: @Composable (String) -> Unit,
    ) : RichTextInlineContent

    /**
     * Represents a standalone inline text content that can be inserted into [RichText].
     * It creates a separate paragraph for rendering in the text layout.
     * @param modifier The modifier to be applied to the inline text content.
     * @param content The composable content of the inline text content.
     * @see RichText
     */
    @Immutable
    data class StandaloneInlineContent(
        val modifier: Modifier = Modifier,
        val content: @Composable (modifier: Modifier) -> Unit,
    ) : RichTextInlineContent
}

private const val STANDALONE_INLINE_CONTENT_TAG =
    "com.microsoft.copilot.markdown.compose.widget.richtext.StandaloneInlineTextContent"
private const val REPLACEMENT_CHAR = "\uFFFD"

/**
 * Used to insert composables into the text layout. This method can be used together with the
 * inlineContent parameter of [RichText]. It will append the [alternateText] to this
 * [AnnotatedString] and also mark this range of text to be replaced by a composable. [RichText]
 * will try to find an [StandaloneInlineTextContent] in the map defined by inlineContent whose key equals to
 * [id], and it will use the [StandaloneInlineTextContent.content] to replace this range of text.
 * And render the composable in new paragraph in the text layout.
 *
 * @param id The id used to look up the [StandaloneInlineTextContent], it is referred by the inlineContent
 *   parameter of [RichText] to replace the [alternateText] to the corresponding composable.
 * @param alternateText The text to be replaced by the inline content. It's displayed when the
 *   inlineContent parameter of [RichText] doesn't contain [id]. Accessibility features will also
 *   use this text to describe the inline content.
 * @throws IllegalArgumentException if [alternateText] has zero length.
 * @see StandaloneInlineTextContent
 * @see RichText
 */
fun AnnotatedString.Builder.appendStandaloneInlineTextContent(
    id: String,
    alternateText: String = REPLACEMENT_CHAR,
) {
    require(alternateText.isNotEmpty())
    pushStringAnnotation(STANDALONE_INLINE_CONTENT_TAG, id)
    append(alternateText)
    pop()
}

fun AnnotatedString.getStandaloneInlineTextContentAnnotations(
    start: Int = 0,
    end: Int = this.length,
): List<Range<String>> =
    getStringAnnotations(
        tag = STANDALONE_INLINE_CONTENT_TAG,
        start = start,
        end = end,
    )
