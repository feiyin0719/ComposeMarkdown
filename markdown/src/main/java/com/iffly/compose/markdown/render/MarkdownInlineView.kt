package com.iffly.compose.markdown.render

import com.iffly.compose.markdown.widget.richtext.RichTextInlineContent

/**
 * Represents an inline view in Markdown rendering.
 * It is used to render inline content inside text.
 * Such as images, and rich content inline node etc.
 * @see RichTextInlineContent
 */
sealed interface MarkdownInlineView {
    /**
     * Represents inline content that can be inserted into text.
     * @param inlineContent The rich text inline content to be rendered.
     */
    data class MarkdownRichTextInlineContent(
        val inlineContent: RichTextInlineContent,
    ) : MarkdownInlineView
}
