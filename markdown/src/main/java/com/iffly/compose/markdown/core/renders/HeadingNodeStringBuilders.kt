package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.getNodeParagraphStyle
import com.iffly.compose.markdown.util.getNodeSpanStyle
import com.vladsch.flexmark.ast.Heading

class HeadingNodeStringBuilder : CompositeChildNodeStringBuilder<Heading>() {
    override fun getSpanStyle(node: Heading, markdownTheme: MarkdownTheme): SpanStyle? {
        return markdownTheme.getNodeSpanStyle(node)
    }

    override fun getParagraphStyle(
        node: Heading, markdownTheme: MarkdownTheme
    ): ParagraphStyle? {
        return markdownTheme.getNodeParagraphStyle(node)
    }
}
