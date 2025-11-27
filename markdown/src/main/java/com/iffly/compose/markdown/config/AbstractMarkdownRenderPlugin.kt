package com.iffly.compose.markdown.config

import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.vladsch.flexmark.parser.InlineParserExtensionFactory
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.misc.Extension

abstract class AbstractMarkdownRenderPlugin : IMarkdownRenderPlugin {
    override fun blockParserFactories(): List<CustomBlockParserFactory> = emptyList()

    override fun inlineContentParserFactories(): List<InlineParserExtensionFactory> = emptyList()

    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<*>> = emptyMap()

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<*>> = emptyMap()

    override fun extensions(): List<Extension> = emptyList()
}
