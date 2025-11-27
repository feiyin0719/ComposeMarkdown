package com.iffly.compose.markdown.render

import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node

class RenderRegistry(
    private val blockRenderers: Map<Class<out Block>, IBlockRenderer<*>>,
    private val inlineNodeStringBuilders: Map<Class<out Node>, IInlineNodeStringBuilder<*>>,
) {
    @Suppress("UNCHECKED_CAST")
    fun getBlockRenderer(blockClass: Class<out Block>): IBlockRenderer<Block>? = blockRenderers[blockClass] as? IBlockRenderer<Block>

    @Suppress("UNCHECKED_CAST")
    fun getInlineNodeStringBuilder(nodeClass: Class<out Node>): IInlineNodeStringBuilder<Node>? =
        inlineNodeStringBuilders[nodeClass] as? IInlineNodeStringBuilder<Node>
}
