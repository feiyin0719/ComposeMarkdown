package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.style.MarkdownTheme
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript

class StrikethroughNodeStringBuilder : CompositeChildNodeStringBuilder<Strikethrough>() {
    override fun getSpanStyle(node: Strikethrough, markdownTheme: MarkdownTheme): SpanStyle? {
        return markdownTheme.strikethrough
    }
}

class SubscriptNodeStringBuilder : CompositeChildNodeStringBuilder<Subscript>() {
    override fun getSpanStyle(node: Subscript, markdownTheme: MarkdownTheme): SpanStyle? {
        return markdownTheme.subscript
    }
}

class StrongEmphasisNodeStringBuilder : CompositeChildNodeStringBuilder<StrongEmphasis>() {
    override fun getSpanStyle(node: StrongEmphasis, markdownTheme: MarkdownTheme): SpanStyle? {
        return markdownTheme.strongEmphasis
    }
}

class EmphasisNodeStringBuilder : CompositeChildNodeStringBuilder<Emphasis>() {
    override fun getSpanStyle(node: Emphasis, markdownTheme: MarkdownTheme): SpanStyle? {
        return markdownTheme.emphasis
    }
}

