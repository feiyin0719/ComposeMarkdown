package com.iffly.compose.markdown.core.renders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.MarkdownBlock
import com.iffly.compose.markdown.render.MarkdownContent
import com.vladsch.flexmark.ast.BlockQuote

class BlockQuoteRenderer : IBlockRenderer<BlockQuote> {
    @Composable
    override fun Invoke(
        node: BlockQuote, modifier: Modifier
    ) {
        MarkdownBlockQuote(modifier, node)
    }
}

@Composable
private fun MarkdownBlockQuote(
    modifier: Modifier, node: BlockQuote
) {
    val typographyStyle = currentTypographyStyle()
    // Content
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = typographyStyle.blockQuoteContentBackgroundColor,
            )
            .drawWithContent {
                drawContent()
                val strokeWidthPx = 4.dp.toPx()
                drawRect(
                    color = typographyStyle.blockQuoteBorderColor,
                    size = Size(strokeWidthPx, size.height)
                )
            }
            .padding(12.dp, bottom = 0.dp, top = 0.dp, end = 12.dp)
    ) {
        var child = node.firstChild
        Spacer(modifier = Modifier.height(typographyStyle.spaceHeight))
        while (child != null) {
            MarkdownContent(child, Modifier)
            if (child.next != null && typographyStyle.showSpace) {
                Spacer(modifier = Modifier.height(typographyStyle.spaceHeight))
            }
            child = child.next
        }
        if (node.lastChild !is BlockQuote) {
            Spacer(modifier = Modifier.height(typographyStyle.spaceHeight))
        }
    }
}
