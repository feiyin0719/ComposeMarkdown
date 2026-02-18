package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.ParagraphStyle
import com.iffly.compose.markdown.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.getNodeParagraphStyle
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.util.ast.Node

/**
 * String builder for Paragraph nodes.
 * @see CompositeChildNodeStringBuilder
 */
class ParagraphNodeStringBuilder : CompositeChildNodeStringBuilder<Paragraph>() {
    override fun getParagraphStyle(
        node: Paragraph,
        markdownTheme: MarkdownTheme,
    ): ParagraphStyle = markdownTheme.getNodeParagraphStyle(node)
}
