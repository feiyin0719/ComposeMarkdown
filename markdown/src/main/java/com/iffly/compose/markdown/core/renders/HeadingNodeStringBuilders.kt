package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.util.getNodeParagraphStyle
import com.iffly.compose.markdown.util.getNodeSpanStyle
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.util.ast.Node

class HeadingNodeStringBuilder : CompositeChildNodeStringBuilder<Heading>() {
    override fun getSpanStyle(node: Heading, typographyStyle: TypographyStyle): SpanStyle? {
        return typographyStyle.getNodeSpanStyle(node)
    }

    override fun getParagraphStyle(
        node: Heading, typographyStyle: TypographyStyle
    ): ParagraphStyle? {
        return typographyStyle.getNodeParagraphStyle(node)
    }
}
