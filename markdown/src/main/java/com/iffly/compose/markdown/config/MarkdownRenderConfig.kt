package com.iffly.compose.markdown.config

import com.iffly.compose.markdown.core.plugins.CorePlugin
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.TypographyStyle
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

    var parser: Parser
        private set

    var htmlRenderer: HtmlRenderer
        private set

    var renderRegistry: RenderRegistry
        private set

    private constructor(
        typographyStyle: TypographyStyle?,
        renderRegistry: RenderRegistry,
        parser: Parser,
        htmlRenderer: HtmlRenderer,
    ) {
        this.typographyStyle = typographyStyle ?: TypographyStyle()
        this.renderRegistry = renderRegistry
        this.parser = parser
        this.htmlRenderer = htmlRenderer
    }

    companion object {
        private val internalPlugins = listOf<IMarkdownRenderPlugin>(
            // Add internal plugins here if needed
            CorePlugin()
        )
    }

    class Builder {
        private val plugins = mutableListOf(
            *internalPlugins.toTypedArray()
        )

        private var typographyStyle: TypographyStyle? = null

        private val inlineNodeStringBuilders =
            mutableMapOf<Class<out Node>, IInlineNodeStringBuilder<out Node>>()

        private val blockRenderers = mutableMapOf<Class<out Block>, IBlockRenderer<out Block>>()
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
            inlineNodeStringBuilders[nodeClass] = builder
            return this
        }

        fun addBlockRenderer(
            blockClass: Class<out Block>,
            renderer: IBlockRenderer<out Block>
        ): Builder {
            blockRenderers[blockClass] = renderer
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
                pluginExtensions.plus(extensions)
            )
            val parserBuilder = Parser.builder(options)
            val htmlRendererBuilder = HtmlRenderer.builder(options)

            plugins.forEach { plugin ->
                plugin.inlineNodeStringBuilders().forEach { (nodeClass, builder) ->
                    addInlineNodeStringBuilder(nodeClass = nodeClass, builder = builder)
                }
                plugin.blockRenderers().forEach { (blockClass, renderer) ->
                    addBlockRenderer(blockClass = blockClass, renderer = renderer)
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
                RenderRegistry(
                    blockRenderers.toMap(),
                    inlineNodeStringBuilders.toMap()
                ),
                parserBuilder.build(),
                htmlRendererBuilder.build()
            )
        }
    }
}