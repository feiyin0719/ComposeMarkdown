package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.withLink
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.MarkdownLinkInteractionListener
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.render.buildChildNodeAnnotatedString
import com.iffly.compose.markdown.style.TypographyStyle
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.util.ast.Node

private fun AnnotatedString.Builder.buildStyleString(
    node: Node,
    indentLevel: Int,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    typographyStyle: TypographyStyle,
    renderRegistry: RenderRegistry,
    actionHandler: ActionHandler?,
    isShowNotSupported: Boolean,
) {
    buildChildNodeAnnotatedString(
        node,
        indentLevel,
        inlineContentMap,
        typographyStyle,
        renderRegistry,
        actionHandler,
        isShowNotSupported,
    )
}

class LinkNodeStringBuilder : IInlineNodeStringBuilder<Link> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Link,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        typographyStyle: TypographyStyle,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry
    ) {
        val linkNode = node
        val linkInteractionListener = actionHandler?.let {
            MarkdownLinkInteractionListener(actionHandler = it, node = linkNode)
        }
        val linkAnnotation = LinkAnnotation.Url(
            url = linkNode.url.toString(),
            styles = typographyStyle.link,
            linkInteractionListener = linkInteractionListener,
        )
        withLink(linkAnnotation) {
            buildStyleString(
                node,
                indentLevel,
                inlineContentMap,
                typographyStyle,
                renderRegistry,
                actionHandler,
                isShowNotSupported,
            )
        }
    }
}

