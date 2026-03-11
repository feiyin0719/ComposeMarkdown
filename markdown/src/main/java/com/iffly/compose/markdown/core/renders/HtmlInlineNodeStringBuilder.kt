package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.NodeStringBuilderContext
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.StringExt
import com.vladsch.flexmark.ast.HtmlInline

class HtmlInlineNodeStringBuilder : IInlineNodeStringBuilder<HtmlInline> {
    private val brRegex = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HtmlInline,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val text = node.chars.toString().trim()
        if (brRegex.matches(text)) {
            append(StringExt.LINE_SEPARATOR)
        }
        // Other HTML inline tags are silently ignored
    }
}
