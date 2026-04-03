package com.iffly.compose.markdown.html

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.NodeStringBuilderContext
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.MarkdownTheme
import com.vladsch.flexmark.ast.HtmlBlock

/**
 * Fallback string builder for [HtmlBlock] nodes that cannot be converted to Markdown.
 *
 * This is used by [HtmlBlockRenderer] when [FlexmarkHtmlConverter] converts an HTML block
 * but the resulting Markdown is re-parsed into another single [HtmlBlock] (i.e. the converter
 * could not map the HTML to any Markdown syntax). In that case, [HtmlBlockRenderer] falls back
 * to [MarkdownInlineText] to render the original HTML as plain text, preventing infinite recursion.
 */
class FallbackHtmlBlockNodeStringBuilder : IInlineNodeStringBuilder<HtmlBlock> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HtmlBlock,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append(node.chars.toString().trim())
    }
}
