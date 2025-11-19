package com.iffly.compose.markdown.render

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.util.contentText
import com.vladsch.flexmark.util.ast.Node

fun AnnotatedString.Builder.buildChildNodeAnnotatedString(
    parent: Node,
    indentLevel: Int = 1,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    typographyStyle: TypographyStyle,
    renderRegistry: RenderRegistry,
    actionHandler: ActionHandler? = null,
    isShowNotSupported: Boolean,
) {
    var node = parent.firstChild
    while (node != null) {
        val customBuilder =
            renderRegistry.getInlineNodeStringBuilder(node::class.java)
        customBuilder?.buildMarkdownInlineNodeString(
            node,
            inlineContentMap,
            typographyStyle,
            indentLevel,
            actionHandler,
            renderRegistry,
            isShowNotSupported,
            this,

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

open class CompositeChildNodeStringBuilder<T : Node> :
    IInlineNodeStringBuilder<T> {
    open fun getSpanStyle(node: T, typographyStyle: TypographyStyle): SpanStyle? {
        return null
    }

    open fun getParagraphStyle(node: T, typographyStyle: TypographyStyle): ParagraphStyle? {
        return null
    }

    fun <R : Any> AnnotatedString.Builder.withSpanStyle(
        node: T,
        typographyStyle: TypographyStyle,
        content: AnnotatedString.Builder.() -> R
    ) {
        val style = getSpanStyle(node, typographyStyle)
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
        typographyStyle: TypographyStyle,
        content: AnnotatedString.Builder.() -> R
    ) {
        val style = getParagraphStyle(node, typographyStyle)
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
        typographyStyle: TypographyStyle,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
    ) {
        withParagraphStyle(node = node, typographyStyle = typographyStyle) {
            withSpanStyle(node = node, typographyStyle = typographyStyle) {
                buildChildNodeAnnotatedString(
                    parent = node,
                    indentLevel = indentLevel,
                    inlineContentMap = inlineContentMap,
                    typographyStyle = typographyStyle,
                    renderRegistry = renderRegistry,
                    actionHandler = actionHandler,
                    isShowNotSupported = isShowNotSupported,
                )
            }
        }
    }
}