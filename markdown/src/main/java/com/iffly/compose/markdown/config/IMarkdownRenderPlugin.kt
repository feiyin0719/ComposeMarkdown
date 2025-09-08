package com.iffly.compose.markdown.config

import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.vladsch.flexmark.parser.InlineParserExtensionFactory
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node

interface IMarkdownRenderPlugin {

    fun blockParserFactories(): List<CustomBlockParserFactory>

    fun inlineContentParserFactories(): List<InlineParserExtensionFactory>

    fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<out Block>>

    fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<out Node>>

}