package com.iffly.compose.markdown.render

import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.node.Block
import org.commonmark.node.BulletList
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.ThematicBreak


class BlockRenderers private constructor(private val renderers: Map<Class<out Block>, IBlockRenderer<out Block>>) {

    companion object {
        private val originalRenderers: Map<Class<out Block>, IBlockRenderer<out Block>> = mapOf(
            TableBlock::class.java to TableRenderer,
            Paragraph::class.java to ParagraphRenderer,
            Heading::class.java to HeadingRenderer,
            OrderedList::class.java to OrderedListRenderer,
            BulletList::class.java to BulletListRenderer,
            FencedCodeBlock::class.java to FencedCodeBlockRenderer,
            IndentedCodeBlock::class.java to IndentedCodeBlockRenderer,
            ThematicBreak::class.java to BreakLineRenderer,
        )

    }

    class Builder {
        private val renderers: MutableMap<Class<out Block>, IBlockRenderer<out Block>> =
            originalRenderers.toMutableMap()

        fun addRenderer(
            blockClass: Class<out Block>,
            renderer: IBlockRenderer<out Block>
        ): Builder {
            renderers[blockClass] = renderer
            return this
        }

        fun build(): BlockRenderers {
            val blockRenderers = BlockRenderers(renderers.toMap())
            return blockRenderers
        }
    }

    operator fun get(blockClass: Class<out Block>): IBlockRenderer<Block>? {
        return renderers[blockClass] as? IBlockRenderer<Block>
    }
}

val defaultBlockRenderers by lazy { BlockRenderers.Builder().build() }
