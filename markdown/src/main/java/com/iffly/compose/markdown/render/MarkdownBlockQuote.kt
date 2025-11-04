package com.iffly.compose.markdown.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent // added for custom left border drawing
import androidx.compose.ui.geometry.Size // size used in custom draw
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.vladsch.flexmark.ast.BlockQuote

object BlockQuoteRenderer : IBlockRenderer<BlockQuote> {
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
                shape = RoundedCornerShape(bottomEnd = 8.dp, topEnd = 8.dp)
            )
            .drawWithContent {
                drawContent()
                val strokeWidthPx = 4.dp.toPx()
                drawRect(
                    color = typographyStyle.blockQuoteBorderColor,
                    size = Size(strokeWidthPx, size.height)
                )
            }
            .padding(12.dp)
    ) {
        var child = node.firstChild
        while (child != null) {
            MarkdownBlock(child, Modifier)
            child = child.next
        }
    }
}
