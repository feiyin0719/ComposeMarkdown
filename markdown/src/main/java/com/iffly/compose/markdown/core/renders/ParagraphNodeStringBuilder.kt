package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.ParagraphStyle
import com.iffly.compose.markdown.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.getNodeParagraphStyle
import com.vladsch.flexmark.util.ast.Node

class ParagraphNodeStringBuilder : CompositeChildNodeStringBuilder<Node>() {
    override fun getParagraphStyle(
        node: Node,
        markdownTheme: MarkdownTheme,
    ): ParagraphStyle? = markdownTheme.getNodeParagraphStyle(node)
}
