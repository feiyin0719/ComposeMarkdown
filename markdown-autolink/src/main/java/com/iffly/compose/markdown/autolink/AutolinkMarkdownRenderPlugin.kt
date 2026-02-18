package com.iffly.compose.markdown.autolink

import androidx.compose.ui.text.TextLinkStyles
import com.iffly.compose.markdown.config.AbstractMarkdownRenderPlugin
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.vladsch.flexmark.ast.AutoLink
import com.vladsch.flexmark.ast.MailLink
import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.misc.Extension

class AutolinkMarkdownRenderPlugin(
    private val linkStyles: TextLinkStyles? = null,
) : AbstractMarkdownRenderPlugin() {
    override fun extensions(): List<Extension> = listOf(AutolinkExtension.create())

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(
            AutoLink::class.java to AutoLinkNodeStringBuilder(linkStyles),
            MailLink::class.java to MailLinkNodeStringBuilder(linkStyles),
        )
}
