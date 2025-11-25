package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.withLink
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.MarkdownLinkInteractionListener
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.render.TextMeasureContext
import com.iffly.compose.markdown.render.buildChildNodeAnnotatedString
import com.iffly.compose.markdown.style.MarkdownTheme
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.util.ast.Node

private fun AnnotatedString.Builder.buildStyleString(
    node: Node,
    indentLevel: Int,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    markdownTheme: MarkdownTheme,
    renderRegistry: RenderRegistry,
    actionHandler: ActionHandler?,
    isShowNotSupported: Boolean,
    measureContext: TextMeasureContext,
) {
    buildChildNodeAnnotatedString(
        node,
        indentLevel,
        inlineContentMap,
        markdownTheme,
        renderRegistry,
        actionHandler,
        isShowNotSupported,
        measureContext,
    )
}

class LinkNodeStringBuilder : IInlineNodeStringBuilder<Link> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Link,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        measureContext: TextMeasureContext
    ) {
        val linkNode = node
        val linkInteractionListener = actionHandler?.let {
            MarkdownLinkInteractionListener(actionHandler = it, node = linkNode)
        }
        val linkAnnotation = LinkAnnotation.Url(
            url = linkNode.url.toString(),
            styles = markdownTheme.link,
            linkInteractionListener = linkInteractionListener,
        )
        withLink(linkAnnotation) {
            buildStyleString(
                node,
                indentLevel,
                inlineContentMap,
                markdownTheme,
                renderRegistry,
                actionHandler,
                isShowNotSupported,
                measureContext,
            )
        }
    }
}

