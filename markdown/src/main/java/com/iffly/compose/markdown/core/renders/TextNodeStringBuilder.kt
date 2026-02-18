package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.render.TextMeasureContext
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.contentText
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.ast.TextBase

/**
 * String builder for Text nodes.
 * @see IInlineNodeStringBuilder
 */
class TextNodeStringBuilder : IInlineNodeStringBuilder<Text> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Text,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        measureContext: TextMeasureContext,
    ) {
        append(node.contentText())
    }
}

/**
 * String builder for TextBase nodes, which are the base class for Text and other text-related nodes.
 * This builder simply builds the child nodes without adding any additional text, as TextBase itself does not contain text content.
 * @see CompositeChildNodeStringBuilder
 */
class TextBaseNodeStringBuilder : CompositeChildNodeStringBuilder<TextBase>()
