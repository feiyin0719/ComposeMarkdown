package com.iffly.compose.markdown.autolink

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.withLink
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.NodeStringBuilderContext
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.render.buildChildNodeAnnotatedString
import com.iffly.compose.markdown.style.MarkdownTheme
import com.vladsch.flexmark.ast.MailLink
import com.vladsch.flexmark.util.ast.Node

class MailLinkInteractionListener(
    private val actionHandler: ActionHandler,
    private val node: Node,
) : LinkInteractionListener {
    override fun onClick(link: LinkAnnotation) {
        (link as? LinkAnnotation.Url)?.let {
            actionHandler.handleUrlClick(link.url, node)
        }
    }
}

class MailLinkNodeStringBuilder(
    private val linkStyles: TextLinkStyles? = null,
) : IInlineNodeStringBuilder<MailLink> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: MailLink,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val linkInteractionListener =
            actionHandler?.let {
                MailLinkInteractionListener(actionHandler = it, node = node)
            }

        val email = node.text.toString()
        val url = if (email.startsWith("mailto:")) email else "mailto:$email"

        val linkAnnotation =
            LinkAnnotation.Url(
                url = url,
                styles = linkStyles ?: markdownTheme.link,
                linkInteractionListener = linkInteractionListener,
            )
        withLink(linkAnnotation) {
            buildChildNodeAnnotatedString(
                node,
                indentLevel,
                inlineContentMap,
                markdownTheme,
                renderRegistry,
                actionHandler,
                isShowNotSupported,
                nodeStringBuilderContext,
            )
        }
    }
}
