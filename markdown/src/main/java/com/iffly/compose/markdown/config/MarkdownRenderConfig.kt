package com.iffly.compose.markdown.config

import com.iffly.compose.markdown.core.plugins.CorePlugin
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.MarkdownTheme
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.InlineParserExtensionFactory
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension

/**
 * The configuration for Markdown rendering.
 * Includes the markdown theme, parser, HTML renderer, and render registry.
 */
class MarkdownRenderConfig {
    var markdownTheme: MarkdownTheme
        private set

    var parser: Parser
        private set

    var htmlRenderer: HtmlRenderer
        private set

    var renderRegistry: RenderRegistry
        private set

    private constructor(
        markdownTheme: MarkdownTheme,
        renderRegistry: RenderRegistry,
        parser: Parser,
        htmlRenderer: HtmlRenderer,
    ) {
        this.markdownTheme = markdownTheme
        this.renderRegistry = renderRegistry
        this.parser = parser
        this.htmlRenderer = htmlRenderer
    }

    companion object {
        private val internalPlugins =
            listOf<IMarkdownRenderPlugin>(
                // Add internal plugins here if needed
                CorePlugin(),
            )
    }

    /**
     * Builder for [MarkdownRenderConfig].
     * Allows for customization of the markdown rendering configuration.
     * @see MarkdownRenderConfig
     */
    class Builder {
        private val plugins =
            mutableListOf(
                *internalPlugins.toTypedArray(),
            )

        private var markdownTheme: MarkdownTheme? = null

        private val inlineNodeStringBuilders =
            mutableMapOf<Class<out Node>, IInlineNodeStringBuilder<*>>()

        private val blockRenderers = mutableMapOf<Class<out Block>, IBlockRenderer<*>>()
        private val blockParserFactories: MutableList<CustomBlockParserFactory> = mutableListOf()

        private val inlineContentParserFactories: MutableList<InlineParserExtensionFactory> =
            mutableListOf()

        private val delimiterProcessors: MutableList<DelimiterProcessor> =
            mutableListOf()

        private val extensions: MutableList<Extension> = mutableListOf()
        private val options = MutableDataSet()

        /**
         * Sets the [MarkdownTheme] for the configuration.
         * @param markdownTheme The [MarkdownTheme] to set.
         * @return The [Builder] instance for chaining.
         * @see MarkdownTheme
         */
        fun markdownTheme(markdownTheme: MarkdownTheme): Builder {
            this.markdownTheme = markdownTheme
            return this
        }

        /**
         * Adds a [IMarkdownRenderPlugin] to the configuration.
         * @param plugin The [IMarkdownRenderPlugin] to add.
         * @return The [Builder] instance for chaining.
         * @see IMarkdownRenderPlugin
         */
        fun addPlugin(plugin: IMarkdownRenderPlugin): Builder {
            plugins.add(plugin)
            return this
        }

        /**
         * Adds an inline node string builder for a specific node class.
         * @param nodeClass The class of the node
         * @param builder The inline node string builder to add.
         * @return The [Builder] instance for chaining.
         * @see IInlineNodeStringBuilder
         */
        fun addInlineNodeStringBuilder(
            nodeClass: Class<out Node>,
            builder: IInlineNodeStringBuilder<*>,
        ): Builder {
            inlineNodeStringBuilders[nodeClass] = builder
            return this
        }

        /**
         * Adds a block renderer for a specific block class.
         * @param blockClass The class of the block.
         * @param renderer The block renderer to add.
         * @return The [Builder] instance for chaining.
         * @see IBlockRenderer
         */
        fun addBlockRenderer(
            blockClass: Class<out Block>,
            renderer: IBlockRenderer<*>,
        ): Builder {
            blockRenderers[blockClass] = renderer
            return this
        }

        /**
         * Adds a custom block parser factory to the configuration.
         * @param factory The custom block parser factory to add.
         * @return The [Builder] instance for chaining.
         */
        fun addBlockParserFactory(factory: CustomBlockParserFactory): Builder {
            blockParserFactories.add(factory)
            return this
        }

        /**
         * Adds a custom inline content parser factory to the configuration.
         * @param factory The custom inline content parser factory to add.
         * @return The [Builder] instance for chaining.
         * @see InlineParserExtensionFactory
         */
        fun addInlineContentParserFactory(factory: InlineParserExtensionFactory): Builder {
            inlineContentParserFactories.add(factory)
            return this
        }

        /**
         * Adds a custom delimiter processor to the configuration.
         * @param processor The custom delimiter processor to add.
         * @return The [Builder] instance for chaining.
         * @see DelimiterProcessor
         */
        fun addDelimiterProcessor(processor: DelimiterProcessor): Builder {
            delimiterProcessors.add(processor)
            return this
        }

        /**
         * Adds a flexmark-java [Extension] to the configuration.
         * @param extension The [Extension] to add.
         * @return The [Builder] instance for chaining.
         * @see Extension
         */
        fun addExtension(extension: Extension): Builder {
            extensions.add(extension)
            return this
        }

        /**
         * Builds the [MarkdownRenderConfig] with the configured settings.
         * @return The constructed [MarkdownRenderConfig].
         */
        fun build(): MarkdownRenderConfig {
            // Configure flexmark-java extensions
            val pluginExtensions = plugins.flatMap { it.extensions() }
            options.set(
                Parser.EXTENSIONS,
                pluginExtensions.plus(extensions),
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
                markdownTheme ?: MarkdownTheme(),
                RenderRegistry(
                    blockRenderers.toMap(),
                    inlineNodeStringBuilders.toMap(),
                ),
                parserBuilder.build(),
                htmlRendererBuilder.build(),
            )
        }
    }
}
