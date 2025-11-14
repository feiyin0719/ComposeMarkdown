package com.iffly.compose.markdown

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import com.vladsch.flexmark.util.ast.Node

interface ActionHandler {
    fun handleUrlClick(url: String, node: Node) {
        // Default implementation does nothing
    }
}

internal class MarkdownLinkInteractionListener(
    private val actionHandler: ActionHandler,
    private val node: Node,
) : LinkInteractionListener {
    override fun onClick(link: LinkAnnotation) {
        (link as? LinkAnnotation.Url)?.let {
            actionHandler.handleUrlClick(link.url, node)
        }
    }

}