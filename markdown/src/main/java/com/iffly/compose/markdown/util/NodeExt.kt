package com.iffly.compose.markdown.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.style.currentTypographyStyle
import org.commonmark.node.Emphasis
import org.commonmark.node.Heading
import org.commonmark.node.Node
import org.commonmark.node.StrongEmphasis

@Composable
fun Node.getSpanStyle(): SpanStyle {
    val typographyStyle = currentTypographyStyle()
    return when (this) {
        is Heading -> this.headStyle()
        is Emphasis -> typographyStyle.emphasis
        is StrongEmphasis -> typographyStyle.strongEmphasis
        else -> typographyStyle.body
    }
}

@Composable
fun Heading.headStyle(): SpanStyle {
    val typographyStyle = currentTypographyStyle()
    return typographyStyle.head[this.level] ?: typographyStyle.body
}