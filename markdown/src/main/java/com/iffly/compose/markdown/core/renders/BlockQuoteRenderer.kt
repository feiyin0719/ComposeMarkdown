package com.iffly.compose.markdown.core.renders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.MarkdownChildren
import com.vladsch.flexmark.ast.BlockQuote

/**
 * The renderer for BlockQuote nodes.
 * @see IBlockRenderer
 */
class BlockQuoteRenderer : IBlockRenderer<BlockQuote> {
    @Composable
    override fun Invoke(
        node: BlockQuote,
        modifier: Modifier,
    ) {
        MarkdownBlockQuote(node = node, modifier = modifier)
    }
}

@Composable
private fun MarkdownBlockQuote(
    node: BlockQuote,
    modifier: Modifier = Modifier,
) {
    val theme = currentTheme()
    val blockQuoteTheme = theme.blockQuoteTheme
    val borderColor = blockQuoteTheme.borderColor
    val spacerHeight = theme.spacerTheme.spacerHeight

    MarkdownChildren(
        parent = node,
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = blockQuoteTheme.backgroundColor, shape = blockQuoteTheme.shape)
                .drawBehind {
                    val borderWidth = blockQuoteTheme.borderWidth.toPx()
                    val x = borderWidth / 2
                    drawLine(
                        color = borderColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = borderWidth,
                    )
                }.padding(blockQuoteTheme.padding),
        childModifierFactory = { Modifier },
        onBeforeAll = {
            Spacer(modifier = Modifier.height(spacerHeight))
        },
        onAfterAll = {
            if (node.lastChild !is BlockQuote) {
                Spacer(modifier = Modifier.height(spacerHeight))
            }
        },
    )
}
