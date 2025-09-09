package com.iffly.compose.markdown.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.vladsch.flexmark.ast.BlockQuote

object BlockQuoteRenderer : IBlockRenderer<BlockQuote> {
    @Composable
    override fun Invoke(
        node: BlockQuote,
        modifier: Modifier
    ) {
        val typographyStyle = currentTypographyStyle()
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            // Left border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        color = typographyStyle.blockQuoteBorderColor,
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    )
                    .padding(vertical = 8.dp)
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = typographyStyle.blockQuoteContentBackgroundColor,
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(12.dp)
            ) {
                var child = node.firstChild
                while (child != null) {
                    MarkdownBlock(child, Modifier)
                    child = child.next
                }
            }
        }
    }
}
