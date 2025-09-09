package com.iffly.compose.markdown.render

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import com.iffly.compose.markdown.style.TypographyStyle
import com.vladsch.flexmark.util.ast.Node


/**
 * Interface for building an AnnotatedString for a specific inline node type.
 * Implement this interface for each inline node type you want to support.
 * @param T The type of the inline node, must be a subclass of [Node].
 */
interface IInlineNodeStringBuilder<T> where T : Node {
    /**
     * Builds an AnnotatedString for the given inline node.
     * @param node The inline node to build the string for.
     * @param inlineContentMap A map to hold any inline content (like images) that needs to be rendered.
     * @param typographyStyle The markdown typography style.
     * @param linkInteractionListener Optional listener for link interactions.
     * @param indentLevel The current indentation level for nested lists.
     *
     */
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