package com.iffly.compose.markdown.render

import androidx.compose.foundation.text.InlineTextContent

sealed interface MarkdownInlineTextContent {
    fun toInlineTextContent(): InlineTextContent

    data class FixedSizeMarkdownInlineTextContent(
        val inlineTextContent: InlineTextContent
    ) : MarkdownInlineTextContent {
        override fun toInlineTextContent(): InlineTextContent = inlineTextContent
    }
}

fun InlineTextContent.toFixedSizeMarkdownInlineTextContent(): MarkdownInlineTextContent.FixedSizeMarkdownInlineTextContent {
    return MarkdownInlineTextContent.FixedSizeMarkdownInlineTextContent(this)
}