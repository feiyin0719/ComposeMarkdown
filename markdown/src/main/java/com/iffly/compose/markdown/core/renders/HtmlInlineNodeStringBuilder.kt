package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.ActionHandlerState
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.NodeStringBuilderContext
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.StringExt
import com.vladsch.flexmark.ast.HtmlInline

class HtmlInlineNodeStringBuilder : IInlineNodeStringBuilder<HtmlInline> {
    companion object {
        private val BR_REGEX = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)
    }

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HtmlInline,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandlerState?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val text = node.chars.toString().trim()
        if (BR_REGEX.matches(text)) {
            append(StringExt.LINE_SEPARATOR)
        }
    }
}
