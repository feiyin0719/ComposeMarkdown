package com.iffly.compose.markdown.render

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import com.iffly.compose.markdown.style.TypographyStyle
import com.vladsch.flexmark.util.ast.Node


interface IInlineNodeStringBuilder<T> where T : Node {
    fun AnnotatedString.Builder.buildInlineNodeString(
        node: T,
        inlineContentMap: MutableMap<String, InlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
    )
}

fun <T : Node> IInlineNodeStringBuilder<T>.buildAnnotatedString(
    node: T,
    inlineContentMap: MutableMap<String, InlineTextContent>,
    typographyStyle: TypographyStyle,
    indentLevel: Int,
    linkInteractionListener: LinkInteractionListener? = null,
    builder: AnnotatedString.Builder
) {
    with(builder) {
        buildInlineNodeString(
            node,
            inlineContentMap,
            typographyStyle,
            linkInteractionListener,
            indentLevel
        )
    }
}