package com.iffly.compose.markdown.render

import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node

/**
 * Registry for block renderers and inline node string builders.
 * @param blockRenderers A map of block renderers.
 * @param inlineNodeStringBuilders A map of inline node string builders.
 * @param markdownContentRenderer An optional renderer for markdown content nodes.
 * @param markdownInlineTextRenderer An optional renderer for inline markdown text nodes.
 */
data class RenderRegistry(
    private val blockRenderers: Map<Class<out Block>, IBlockRenderer<*>>,
    private val inlineNodeStringBuilders: Map<Class<out Node>, IInlineNodeStringBuilder<*>>,
    val markdownContentRenderer: MarkdownContentRenderer? = null,
    val markdownInlineTextRenderer: MarkdownInlineTextRenderer? = null,
) {
    @Suppress("UNCHECKED_CAST")
    fun getBlockRenderer(blockClass: Class<out Block>): IBlockRenderer<Block>? = blockRenderers[blockClass] as? IBlockRenderer<Block>

    @Suppress("UNCHECKED_CAST")
    fun getInlineNodeStringBuilder(nodeClass: Class<out Node>): IInlineNodeStringBuilder<Node>? =
        inlineNodeStringBuilders[nodeClass] as? IInlineNodeStringBuilder<Node>

    /**
     * Creates an augmented [RenderRegistry] for Text-based rendering ([MarkdownText]).
     *
     * For each block renderer that has no corresponding [IInlineNodeStringBuilder],
     * a [BlockRendererInlineStringBuilder] wrapper is created and registered. A
     * [DocumentInlineStringBuilder] is also added if not already present.
     *
     * This allows [MarkdownText] to render all block nodes as inline content
     * without modifying the base registry at config-build time.
     */
    @Suppress("UNCHECKED_CAST")
    fun textModeRegistry(): RenderRegistry {
        val augmented = inlineNodeStringBuilders.toMutableMap()
        if (!augmented.containsKey(Document::class.java)) {
            augmented[Document::class.java] = DocumentInlineStringBuilder()
        }
        for ((blockClass, renderer) in blockRenderers) {
            if (!augmented.containsKey(blockClass)) {
                augmented[blockClass] =
                    BlockRendererInlineStringBuilder(renderer as IBlockRenderer<Block>)
            }
        }
        return copy(inlineNodeStringBuilders = augmented.toMap())
    }
}
