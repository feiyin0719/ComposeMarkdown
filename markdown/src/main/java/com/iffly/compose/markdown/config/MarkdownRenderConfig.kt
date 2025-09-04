package com.iffly.compose.markdown.config

import com.iffly.compose.markdown.render.BlockRenderers
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.InlineNodeStringBuilders
import com.iffly.compose.markdown.style.TypographyStyle
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.Block
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.parser.beta.InlineContentParserFactory
import org.commonmark.parser.block.BlockParserFactory
import org.commonmark.parser.delimiter.DelimiterProcessor

class MarkdownRenderConfig {

    var typographyStyle: TypographyStyle? = null
        private set

    var inlineNodeStringBuilders: InlineNodeStringBuilders
        private set

    var blockRenderers: BlockRenderers
        private set

    var parser: Parser
        private set

    private constructor(
        typographyStyle: TypographyStyle?,
        inlineNodeStringBuilders: InlineNodeStringBuilders,
        blockRenderers: BlockRenderers,
        parser: Parser
    ) {
        this.typographyStyle = typographyStyle ?: TypographyStyle()
        this.inlineNodeStringBuilders = inlineNodeStringBuilders
        this.blockRenderers = blockRenderers
        this.parser = parser
    }

    class Builder {
        private val plugins = mutableListOf<IMarkdownRenderPlugin>()

        private var typographyStyle: TypographyStyle? = null

        private val inlineNodeStringBuilder: InlineNodeStringBuilders.Builder =
            InlineNodeStringBuilders.Builder()

        private val blockRendererBuilder: BlockRenderers.Builder =
            BlockRenderers.Builder()

        private val blockParserFactories: MutableList<BlockParserFactory> = mutableListOf()

        private val inlineContentParserFactories: MutableList<InlineContentParserFactory> =
            mutableListOf()

        private val delimiterProcessors: MutableList<DelimiterProcessor> =
            mutableListOf()

        fun typographyStyle(typographyStyle: TypographyStyle): Builder {
            this.typographyStyle = typographyStyle
            return this
        }

        fun addPlugin(plugin: IMarkdownRenderPlugin): Builder {
            plugins.add(plugin)
            return this
        }

        fun addInlineNodeStringBuilder(
            nodeClass: Class<out Node>,
            builder: IInlineNodeStringBuilder<out Node>
        ): Builder {
            inlineNodeStringBuilder.addAnnotatedStringBuilder(nodeClass, builder)
            return this
        }

        fun addBlockRenderer(
            blockClass: Class<out Block>,
            renderer: IBlockRenderer<out Block>
        ): Builder {
            blockRendererBuilder.addRenderer(blockClass, renderer)
            return this
        }

        fun addBlockParserFactory(factory: BlockParserFactory): Builder {
            blockParserFactories.add(factory)
            return this
        }

        fun addInlineContentParserFactory(factory: InlineContentParserFactory): Builder {
            inlineContentParserFactories.add(factory)
            return this
        }

        fun addDelimiterProcessor(processor: DelimiterProcessor): Builder {
            delimiterProcessors.add(processor)
            return this
        }

        fun build(): MarkdownRenderConfig {
            val parserBuilder = Parser.builder()

            parserBuilder.extensions(listOf(TablesExtension.create()))

            plugins.forEach { plugin ->
                plugin.inlineNodeStringBuilders().forEach { (nodeClass, builder) ->
                    inlineNodeStringBuilder.addAnnotatedStringBuilder(nodeClass, builder)
                }
                plugin.blockRenderers().forEach { (blockClass, renderer) ->
                    blockRendererBuilder.addRenderer(blockClass, renderer)
                }
                plugin.blockParserFactories().forEach { factory ->
                    parserBuilder.customBlockParserFactory(factory)
                }
                plugin.inlineContentParserFactories().forEach { factory ->
                    parserBuilder.customInlineContentParserFactory(factory)
                }
            }

            blockParserFactories.forEach { factory ->
                parserBuilder.customBlockParserFactory(factory)
            }

            inlineContentParserFactories.forEach { factory ->
                parserBuilder.customInlineContentParserFactory(factory)
            }

            delimiterProcessors.forEach { processor ->
                parserBuilder.customDelimiterProcessor(processor)
            }

            return MarkdownRenderConfig(
                typographyStyle,
                inlineNodeStringBuilder.build(),
                blockRendererBuilder.build(),
                parserBuilder.build()
            )
        }
    }
}