package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.style.MarkdownTheme
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript

/**
 * String builder for Strikethrough nodes.
 * @see CompositeChildNodeStringBuilder
 */
class StrikethroughNodeStringBuilder : CompositeChildNodeStringBuilder<Strikethrough>() {
    override fun getSpanStyle(
        node: Strikethrough,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = markdownTheme.strikethrough
}

/**
 * String builder for Subscript nodes.
 * @see CompositeChildNodeStringBuilder
 */
class SubscriptNodeStringBuilder : CompositeChildNodeStringBuilder<Subscript>() {
    override fun getSpanStyle(
        node: Subscript,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = markdownTheme.subscript
}

/**
 * String builder for StrongEmphasis nodes.
 * @see CompositeChildNodeStringBuilder
 */
class StrongEmphasisNodeStringBuilder : CompositeChildNodeStringBuilder<StrongEmphasis>() {
    override fun getSpanStyle(
        node: StrongEmphasis,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = markdownTheme.strongEmphasis
}

/**
 * String builder for Emphasis nodes.
 * @see CompositeChildNodeStringBuilder
 */
class EmphasisNodeStringBuilder : CompositeChildNodeStringBuilder<Emphasis>() {
    override fun getSpanStyle(
        node: Emphasis,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = markdownTheme.emphasis
}
