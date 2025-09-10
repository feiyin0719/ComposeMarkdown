package com.iffly.compose.markdown.config

import com.iffly.compose.markdown.render.BlockRenderers
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.InlineNodeStringBuilders
import com.iffly.compose.markdown.style.TypographyStyle
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.InlineParserExtensionFactory
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension

class MarkdownRenderConfig {

    var typographyStyle: TypographyStyle? = null
        private set

    var inlineNodeStringBuilders: InlineNodeStringBuilders
        private set

    var blockRenderers: BlockRenderers
        private set

    var parser: Parser
        private set

    var htmlRenderer: HtmlRenderer
        private set

    private constructor(
        typographyStyle: TypographyStyle?,
        inlineNodeStringBuilders: InlineNodeStringBuilders,
        blockRenderers: BlockRenderers,
        parser: Parser,
        htmlRenderer: HtmlRenderer,
    ) {
        this.typographyStyle = typographyStyle ?: TypographyStyle()
        this.inlineNodeStringBuilders = inlineNodeStringBuilders
        this.blockRenderers = blockRenderers
        this.parser = parser
        this.htmlRenderer = htmlRenderer
    }

    class Builder {
        private val plugins = mutableListOf<IMarkdownRenderPlugin>()

        private var typographyStyle: TypographyStyle? = null

        private val inlineNodeStringBuilder: InlineNodeStringBuilders.Builder =
            InlineNodeStringBuilders.Builder()

        private val blockRendererBuilder: BlockRenderers.Builder =
            BlockRenderers.Builder()

        private val blockParserFactories: MutableList<CustomBlockParserFactory> = mutableListOf()

        private val inlineContentParserFactories: MutableList<InlineParserExtensionFactory> =
            mutableListOf()

        private val delimiterProcessors: MutableList<DelimiterProcessor> =
            mutableListOf()

        private val extensions: MutableList<Extension> = mutableListOf()
        private val options = MutableDataSet()

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

        fun addBlockParserFactory(factory: CustomBlockParserFactory): Builder {
            blockParserFactories.add(factory)
            return this
        }

        fun addInlineContentParserFactory(factory: InlineParserExtensionFactory): Builder {
            inlineContentParserFactories.add(factory)
            return this
        }

        fun addDelimiterProcessor(processor: DelimiterProcessor): Builder {
            delimiterProcessors.add(processor)
            return this
        }

        fun addExtension(extension: Extension): Builder {
            extensions.add(extension)
            return this
        }

        fun build(): MarkdownRenderConfig {
            // Configure flexmark-java extensions
            val pluginExtensions = plugins.flatMap { it.extensions() }
            options.set(
                Parser.EXTENSIONS,
                listOf(
                    TablesExtension.create(),
                    StrikethroughSubscriptExtension.create(),
                ).plus(extensions)
                    .plus(pluginExtensions)
            )
            val parserBuilder = Parser.builder(options)
            val htmlRendererBuilder = HtmlRenderer.builder(options)

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
                    parserBuilder.customInlineParserExtensionFactory(factory)
                }
            }

            blockParserFactories.forEach { factory ->
                parserBuilder.customBlockParserFactory(factory)
            }

            inlineContentParserFactories.forEach { factory ->
                parserBuilder.customInlineParserExtensionFactory(factory)
            }

            delimiterProcessors.forEach { processor ->
                parserBuilder.customDelimiterProcessor(processor)
            }

            return MarkdownRenderConfig(
                typographyStyle,
                inlineNodeStringBuilder.build(),
                blockRendererBuilder.build(),
                parserBuilder.build(),
                htmlRendererBuilder.build()
            )
        }
    }
}