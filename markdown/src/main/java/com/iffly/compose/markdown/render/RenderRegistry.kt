package com.iffly.compose.markdown.render

import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node

/**
 * Registry for block renderers and inline node string builders.
 * @param blockRenderers A map of block renderers.
 * @param inlineNodeStringBuilders A map of inline node string builders.
 * @param markdownContentRenderer An optional renderer for markdown content nodes.
 * @param markdownTextRenderer An optional renderer for markdown text nodes.
 */
data class RenderRegistry(
    private val blockRenderers: Map<Class<out Block>, IBlockRenderer<*>>,
    private val inlineNodeStringBuilders: Map<Class<out Node>, IInlineNodeStringBuilder<*>>,
    val markdownContentRenderer: MarkdownContentRenderer? = null,
    val markdownTextRenderer: MarkdownTextRenderer? = null,
) {
    @Suppress("UNCHECKED_CAST")
    fun getBlockRenderer(blockClass: Class<out Block>): IBlockRenderer<Block>? = blockRenderers[blockClass] as? IBlockRenderer<Block>

    @Suppress("UNCHECKED_CAST")
    fun getInlineNodeStringBuilder(nodeClass: Class<out Node>): IInlineNodeStringBuilder<Node>? =
        inlineNodeStringBuilders[nodeClass] as? IInlineNodeStringBuilder<Node>
}
