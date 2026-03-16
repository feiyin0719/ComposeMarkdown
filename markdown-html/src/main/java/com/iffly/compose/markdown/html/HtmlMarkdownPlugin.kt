package com.iffly.compose.markdown.html

import com.iffly.compose.markdown.config.AbstractMarkdownRenderPlugin
import com.iffly.compose.markdown.html.handlers.defaultHtmlInlineTagHandlers
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.vladsch.flexmark.ast.HtmlBlock
import com.vladsch.flexmark.ast.HtmlInline
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node

class HtmlMarkdownPlugin(
    customTagHandlers: List<HtmlInlineTagHandler> = emptyList(),
) : AbstractMarkdownRenderPlugin() {
    private val tagHandlerMap: Map<String, HtmlInlineTagHandler>

    init {
        val map = mutableMapOf<String, HtmlInlineTagHandler>()
        for (handler in defaultHtmlInlineTagHandlers()) {
            for (name in handler.tagNames) {
                map[name] = handler
            }
        }
        for (handler in customTagHandlers) {
            for (name in handler.tagNames) {
                map[name] = handler
            }
        }
        tagHandlerMap = map.toMap()
    }

    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<*>> =
        mapOf(
            HtmlBlock::class.java to HtmlBlockRenderer(),
        )

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(
            HtmlInline::class.java to HtmlInlineNodeStringBuilder(tagHandlerMap),
            HtmlBlock::class.java to FallbackHtmlBlockNodeStringBuilder(),
        )
}
