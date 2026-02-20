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
import com.vladsch.flexmark.ast.LinkRef
import com.vladsch.flexmark.ast.Reference
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

/**
 * String builder for Link nodes.
 * @see IInlineNodeStringBuilder
 */
class LinkNodeStringBuilder : IInlineNodeStringBuilder<Link> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Link,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        measureContext: TextMeasureContext,
    ) {
        val linkInteractionListener =
            actionHandler?.let {
                MarkdownLinkInteractionListener(actionHandler = it, node = node)
            }
        val linkAnnotation =
            LinkAnnotation.Url(
                url = node.url.toString(),
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

/**
 * String builder for LinkRef nodes, which are reference-style links.
 * This builder is not yet implemented and will require resolving the reference to the actual link URL and content.
 * @see IInlineNodeStringBuilder
 */
class LinkRefNodeStringBuilder : IInlineNodeStringBuilder<LinkRef> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: LinkRef,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        measureContext: TextMeasureContext,
    ) {
        val referenceNode = node.getReferenceNode(node.document)
        val url =
            referenceNode?.url?.unescape()?.takeIf { node.isDefined } ?: node.reference.unescape()
        if (url.isNotBlank()) {
            val linkInteractionListener =
                actionHandler?.let {
                    MarkdownLinkInteractionListener(actionHandler = it, node = node)
                }
            val linkAnnotation =
                LinkAnnotation.Url(
                    url = url,
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
        } else {
            // If the reference is not defined, we can choose to render the reference text or show a placeholder.
            // For now, we'll just render the reference text.
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

/**
 * String builder for Reference nodes, which are used for reference definitions in markdown.
 * This builder does not render any text as reference definitions typically do not contribute to the visible content.
 * @see IInlineNodeStringBuilder
 */
class ReferenceNodeStringBuilder : IInlineNodeStringBuilder<Reference> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Reference,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        measureContext: TextMeasureContext,
    ) {
        // This builder is for the reference definition nodes, which typically do not render any text.
        // We can choose to render the reference label or simply ignore it.
        // For now, we'll ignore it as it does not contribute to the visible content.
    }
}
