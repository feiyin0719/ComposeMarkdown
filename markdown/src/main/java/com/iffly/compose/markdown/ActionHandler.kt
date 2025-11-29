package com.iffly.compose.markdown

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import com.vladsch.flexmark.util.ast.Node

/**
 * Marker interface for custom events.
 * Implement this interface to define specific custom events that can be handled
 * by the ActionHandler.
 * When you create a custom node in markdown, you can also create a corresponding CustomEvent
 * implementation to represent events related to that node.
 */
interface CustomEvent

/**
 * Interface for handling various user actions within markdown content.
 * Implement this interface to define custom behavior for URL clicks, copy actions,
 * image clicks, and other custom events.
 */
interface ActionHandler {
    /**
     * Handle URL click events.
     * @param url The URL that was clicked.
     * @param node The markdown node associated with the URL.
     */
    fun handleUrlClick(
        url: String,
        node: Node,
    ) {
        // Default implementation does nothing
    }

    /**
     * Handle copy click events.
     * @param node The markdown node associated with the copy action.
     */
    fun handleCopyClick(node: Node) {
        // Default implementation does nothing
    }

    /**
     * Handle image click events.
     * @param imageUrl The URL of the image that was clicked.
     * @param node The markdown node associated with the image.
     */
    fun handleImageClick(
        imageUrl: String,
        node: Node,
    ) {
        // Default implementation does nothing
    }

    /**
     * Handle custom events.
     * @param event The custom event to handle.
     * @param node The markdown node associated with the event.
     */
    fun handleCustomEvent(
        event: CustomEvent,
        node: Node,
    ) {
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
