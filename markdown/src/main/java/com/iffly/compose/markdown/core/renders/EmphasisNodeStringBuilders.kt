package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.style.TypographyStyle
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript

class StrikethroughNodeStringBuilder : CompositeChildNodeStringBuilder<Strikethrough>() {
    override fun getSpanStyle(node: Strikethrough, typographyStyle: TypographyStyle): SpanStyle? {
        return typographyStyle.strikethrough
    }
}

class SubscriptNodeStringBuilder : CompositeChildNodeStringBuilder<Subscript>() {
    override fun getSpanStyle(node: Subscript, typographyStyle: TypographyStyle): SpanStyle? {
        return typographyStyle.subscript
    }
}

class StrongEmphasisNodeStringBuilder : CompositeChildNodeStringBuilder<StrongEmphasis>() {
    override fun getSpanStyle(node: StrongEmphasis, typographyStyle: TypographyStyle): SpanStyle? {
        return typographyStyle.strongEmphasis
    }
}

class EmphasisNodeStringBuilder : CompositeChildNodeStringBuilder<Emphasis>() {
    override fun getSpanStyle(node: Emphasis, typographyStyle: TypographyStyle): SpanStyle? {
        return typographyStyle.emphasis
    }
}

