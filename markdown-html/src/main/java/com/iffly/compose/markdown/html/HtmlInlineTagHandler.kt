package com.iffly.compose.markdown.html

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.NodeStringBuilderContext
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.MarkdownTheme
import com.vladsch.flexmark.util.ast.Node

data class HtmlInlineTagContext(
    val node: Node,
    val inlineContentMap: MutableMap<String, MarkdownInlineView>,
    val markdownTheme: MarkdownTheme,
    val actionHandler: ActionHandler?,
    val indentLevel: Int,
    val isShowNotSupported: Boolean,
    val renderRegistry: RenderRegistry,
    val nodeStringBuilderContext: NodeStringBuilderContext,
)

interface HtmlInlineTagHandler {
    val tagNames: Set<String>

    fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    )

    fun onCloseTag(
        tagName: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pop()
    }
}
