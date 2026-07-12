package com.iffly.compose.markdown.render

/**
 * Supplies the render-relevant content hash for a custom Markdown node.
 *
 * Override [contentHash] when fields on the custom node affect rendering. The default is stable for
 * stateless custom nodes and uses only the node class name.
 */
interface NodeContentHashProvider {
    fun contentHash(): Int = javaClass.name.hashCode()
}
