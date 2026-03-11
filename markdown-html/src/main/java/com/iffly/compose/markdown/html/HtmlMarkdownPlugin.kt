package com.iffly.compose.markdown.html

import com.iffly.compose.markdown.config.AbstractMarkdownRenderPlugin
import com.iffly.compose.markdown.render.IBlockRenderer
import com.vladsch.flexmark.ast.HtmlBlock
import com.vladsch.flexmark.util.ast.Block

class HtmlMarkdownPlugin : AbstractMarkdownRenderPlugin() {
    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<*>> =
        mapOf(
            HtmlBlock::class.java to HtmlBlockRenderer(),
        )
}
