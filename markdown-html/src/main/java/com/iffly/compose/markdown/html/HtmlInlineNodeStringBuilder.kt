package com.iffly.compose.markdown.html

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.NodeStringBuilderContext
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.StringExt
import com.vladsch.flexmark.ast.HtmlInline

class HtmlInlineNodeStringBuilder(
    private val tagHandlers: Map<String, HtmlInlineTagHandler>,
) : IInlineNodeStringBuilder<HtmlInline> {
    companion object {
        private val BR_REGEX = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)
        private val TAG_NAME_REGEX = Regex("""</?(\w+)""")
    }

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

        if (BR_REGEX.matches(text)) {
            append(StringExt.LINE_SEPARATOR)
            return
        }

        val tagName =
            TAG_NAME_REGEX
                .find(text)
                ?.groupValues
                ?.get(1)
                ?.lowercase() ?: return
        val handler = tagHandlers[tagName] ?: return

        val context =
            HtmlInlineTagContext(
                node = node,
                inlineContentMap = inlineContentMap,
                markdownTheme = markdownTheme,
                actionHandler = actionHandler,
                indentLevel = indentLevel,
                isShowNotSupported = isShowNotSupported,
                renderRegistry = renderRegistry,
                nodeStringBuilderContext = nodeStringBuilderContext,
            )

        if (text.startsWith("</")) {
            handler.onCloseTag(tagName, this, context)
        } else {
            handler.onOpenTag(tagName, text, this, context)
        }
    }
}
