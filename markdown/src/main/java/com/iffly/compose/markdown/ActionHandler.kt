package com.iffly.compose.markdown

import org.w3c.dom.Node

interface ActionHandler {
    fun handleUrlClick(url: String, node: Node)
}