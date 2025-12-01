package com.iffly.compose.markdown.render

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.contentText
import com.vladsch.flexmark.util.ast.Node

/**
 * Builds an AnnotatedString for the child nodes of the given parent node.
 * @param parent The parent node whose child nodes will be processed.
 * @param indentLevel The current indentation level for nested lists.
 * @param inlineContentMap A map to hold any inline content (like images) that needs to be rendered.
 * @param markdownTheme The markdown typography style.
 * @param renderRegistry The render registry containing all renderers and builders.
 * @param actionHandler Optional listener for link interactions.
 * @param isShowNotSupported Whether to show unsupported node indicators.
 * @param measureContext The context for measuring text.
 */
fun AnnotatedString.Builder.buildChildNodeAnnotatedString(
    parent: Node,
    indentLevel: Int = 1,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    markdownTheme: MarkdownTheme,
    renderRegistry: RenderRegistry,
    actionHandler: ActionHandler? = null,
    isShowNotSupported: Boolean,
    measureContext: TextMeasureContext,
) {
    var node = parent.firstChild
    while (node != null) {
        val customBuilder =
            renderRegistry.getInlineNodeStringBuilder(node::class.java)
        customBuilder?.buildMarkdownInlineNodeString(
            node,
            inlineContentMap,
            markdownTheme,
            indentLevel,
            actionHandler,
            renderRegistry,
            isShowNotSupported,
            this,
            measureContext,
        ) ?: run {
            if (isShowNotSupported) {
                append("[Unsupported: ${node::class.java.simpleName}]")
            } else {
                append(node.contentText())
            }
        }
        node = node.next
    }
}

/**
 * A base implementation of [IInlineNodeStringBuilder] for nodes that primarily
 * serve as containers for other inline nodes.
 * @param T The type of the inline node, must be a subclass of [Node].
 * @see IInlineNodeStringBuilder
 */
open class CompositeChildNodeStringBuilder<T : Node> : IInlineNodeStringBuilder<T> {
    open fun getSpanStyle(
        node: T,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = null

    open fun getParagraphStyle(
        node: T,
        markdownTheme: MarkdownTheme,
    ): ParagraphStyle? = null

    fun <R : Any> AnnotatedString.Builder.withSpanStyle(
        node: T,
        markdownTheme: MarkdownTheme,
        content: AnnotatedString.Builder.() -> R,
    ) {
        val style = getSpanStyle(node, markdownTheme)
        if (style != null) {
            withStyle(style) {
                content()
            }
        } else {
            content()
        }
    }

    fun <R : Any> AnnotatedString.Builder.withParagraphStyle(
        node: T,
        markdownTheme: MarkdownTheme,
        content: AnnotatedString.Builder.() -> R,
    ) {
        val style = getParagraphStyle(node, markdownTheme)
        if (style != null) {
            withStyle(style) {
                content()
            }
        } else {
            content()
        }
    }

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: T,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        measureContext: TextMeasureContext,
    ) {
        withParagraphStyle(node = node, markdownTheme = markdownTheme) {
            withSpanStyle(node = node, markdownTheme = markdownTheme) {
                buildChildNodeAnnotatedString(
                    parent = node,
                    indentLevel = indentLevel,
                    inlineContentMap = inlineContentMap,
                    markdownTheme = markdownTheme,
                    renderRegistry = renderRegistry,
                    actionHandler = actionHandler,
                    isShowNotSupported = isShowNotSupported,
                    measureContext = measureContext,
                )
            }
        }
    }
}
