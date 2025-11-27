package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.render.TextMeasureContext
import com.iffly.compose.markdown.style.MarkdownTheme
import com.vladsch.flexmark.ast.HardLineBreak
import com.vladsch.flexmark.ast.SoftLineBreak

class SoftLineBreakNodeStringBuilder : IInlineNodeStringBuilder<SoftLineBreak> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: SoftLineBreak,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        measureContext: TextMeasureContext,
    ) {
        append(" ")
    }
}

class HardLineBreakNodeStringBuilder : IInlineNodeStringBuilder<HardLineBreak> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HardLineBreak,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        measureContext: TextMeasureContext,
    ) {
        append("\n")
    }
}
