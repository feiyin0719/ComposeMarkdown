package com.iffly.compose.markdown.render

import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.IndentedCodeBlock
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.ThematicBreak
import com.vladsch.flexmark.ext.tables.TableBlock
import com.vladsch.flexmark.util.ast.Block


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
