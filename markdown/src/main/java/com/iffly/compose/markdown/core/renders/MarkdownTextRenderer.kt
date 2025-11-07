package com.iffly.compose.markdown.core.renders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.MarkdownText
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.util.ast.Block

open class MarkdownTextRenderer<T> : IBlockRenderer<T> where T : Block {
    @Composable
    override fun Invoke(
        node: T,
        modifier: Modifier
    ) {
        MarkdownText(parent = node, modifier = modifier)
    }
}

class ParagraphRenderer : MarkdownTextRenderer<Paragraph>()

class HeadingRenderer : MarkdownTextRenderer<Heading>()