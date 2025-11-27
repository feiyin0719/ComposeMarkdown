package com.iffly.compose.markdown.render

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.contentText
import com.vladsch.flexmark.util.ast.Node

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
