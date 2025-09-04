package com.iffly.compose.markdown.config

import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import org.commonmark.node.Block
import org.commonmark.node.Node
import org.commonmark.parser.beta.InlineContentParserFactory
import org.commonmark.parser.block.BlockParserFactory

interface IMarkdownRenderPlugin {

    fun blockParserFactories(): List<BlockParserFactory>

    fun inlineContentParserFactories(): List<InlineContentParserFactory>

    fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<out Block>>

    fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<out Node>>

}