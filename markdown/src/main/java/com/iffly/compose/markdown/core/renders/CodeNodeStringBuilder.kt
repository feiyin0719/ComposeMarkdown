package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.contentText
import com.vladsch.flexmark.ast.Code

class CodeNodeStringBuilder : IInlineNodeStringBuilder<Code> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Code,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry
    ) {
        withStyle(markdownTheme.code.toSpanStyle()) {
            append(node.contentText())
        }
    }
}

