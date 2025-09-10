package com.iffly.compose.markdown.config

import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.vladsch.flexmark.parser.InlineParserExtensionFactory
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.misc.Extension

/**
 * Interface for a Markdown render plugin.
 * Implement this interface to provide custom block parsers, inline parsers,
 * block renderers, and inline node string builders.
 */
interface IMarkdownRenderPlugin {

    /**
     * Returns a list of custom block parser factories.
     */
    fun blockParserFactories(): List<CustomBlockParserFactory>

    /**
     * Returns a list of inline parser extension factories.
     */
    fun inlineContentParserFactories(): List<InlineParserExtensionFactory>

    /**
     * Returns a map of block renderers.
     * The key is the class of the block node, and the value is the corresponding renderer.
     */
    fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<out Block>>

    /**
     * Returns a map of inline node string builders.
     * The key is the class of the inline node, and the value is the corresponding string builder.
     */
    fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<out Node>>

    fun extensions(): List<Extension>

}