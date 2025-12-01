package com.iffly.compose.markdown.core.renders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.MarkdownText
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.util.ast.Block

/**
 * Base renderer for block nodes that render text content.
 * @param T The type of the block node, must be a subclass of [Block].
 * @see IBlockRenderer
 * @see MarkdownText
 */
open class MarkdownTextRenderer<T> : IBlockRenderer<T> where T : Block {
    @Composable
    override fun Invoke(
        node: T,
        modifier: Modifier,
    ) {
        MarkdownText(parent = node, modifier = modifier)
    }
}

/**
 * Renderer for Paragraph nodes.
 * @see MarkdownTextRenderer
 */
class ParagraphRenderer : MarkdownTextRenderer<Paragraph>()

/**
 * Renderer for Heading nodes.
 * @see MarkdownTextRenderer
 */
class HeadingRenderer : MarkdownTextRenderer<Heading>()
