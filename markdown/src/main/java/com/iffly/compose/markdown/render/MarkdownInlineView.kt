package com.iffly.compose.markdown.render

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.widget.richtext.StandaloneInlineTextContent

sealed interface MarkdownInlineView {

    sealed interface MarkdownInlineTextContent : MarkdownInlineView {
        fun toInlineTextContent(): InlineTextContent

        @Immutable
        data class FixedSizeMarkdownInlineView(
            val inlineTextContent: InlineTextContent
        ) : MarkdownInlineTextContent {
            override fun toInlineTextContent(): InlineTextContent = inlineTextContent
        }
    }

    data class MarkdownStandaloneInlineView(
        val modifier: Modifier,
        val content: @Composable (modifier: Modifier) -> Unit
    ) : MarkdownInlineView {
        fun toStandaloneInlineTextContent(): StandaloneInlineTextContent {
            return StandaloneInlineTextContent(
                modifier = modifier,
                content = content,
            )
        }
    }


}

fun InlineTextContent.toFixedSizeMarkdownInlineTextContent(): MarkdownInlineView.MarkdownInlineTextContent.FixedSizeMarkdownInlineView {
    return MarkdownInlineView.MarkdownInlineTextContent.FixedSizeMarkdownInlineView(this)
}