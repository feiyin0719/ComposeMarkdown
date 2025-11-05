package com.iffly.compose.markdown.render

import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node

class RenderRegistry(
    private val blockRenderers: Map<Class<out Block>, IBlockRenderer<out Block>>,
    private val inlineNodeStringBuilders: Map<Class<out Node>, IInlineNodeStringBuilder<out Node>>
) {
    fun getBlockRenderer(blockClass: Class<out Block>): IBlockRenderer<Block>? {
        return blockRenderers[blockClass] as? IBlockRenderer<Block>
    }

    fun getInlineNodeStringBuilder(nodeClass: Class<out Node>): IInlineNodeStringBuilder<Node>? {
        return inlineNodeStringBuilders[nodeClass] as? IInlineNodeStringBuilder<Node>
    }
}