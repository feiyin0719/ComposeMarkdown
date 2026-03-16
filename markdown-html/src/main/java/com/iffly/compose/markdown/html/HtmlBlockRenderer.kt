package com.iffly.compose.markdown.html

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.config.currentHtmlToMdConverter
import com.iffly.compose.markdown.config.currentParser
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.MarkdownChildren
import com.iffly.compose.markdown.render.MarkdownText
import com.vladsch.flexmark.ast.HtmlBlock
import com.vladsch.flexmark.util.ast.Document

class HtmlBlockRenderer : IBlockRenderer<HtmlBlock> {
    @Composable
    override fun Invoke(
        node: HtmlBlock,
        modifier: Modifier,
    ) {
        val html = node.chars.toString().trim()
        if (html.isEmpty()) return

        val parser = currentParser()
        val htmlToMdConverter = currentHtmlToMdConverter()
        val document =
            remember(html, parser, htmlToMdConverter) {
                val markdown = htmlToMdConverter.convert(html)
                parser.parse(markdown)
            }
        if (document.isSingleHtmlBlock()) {
            MarkdownText(parent = node, modifier = modifier)
        } else {
            MarkdownChildren(parent = document, modifier = modifier)
        }
    }

    private fun Document.isSingleHtmlBlock(): Boolean {
        val firstChild = firstChild ?: return false
        return firstChild is HtmlBlock && firstChild.next == null
    }
}
