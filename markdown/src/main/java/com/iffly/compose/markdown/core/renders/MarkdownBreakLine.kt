package com.iffly.compose.markdown.core.renders

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.widget.HorizontalLine
import com.vladsch.flexmark.ast.ThematicBreak

class BreakLineRenderer : IBlockRenderer<ThematicBreak> {
    @Composable
    override fun Invoke(node: ThematicBreak, modifier: Modifier) {
        BreakLine(node, modifier)
    }
}

@Composable
fun BreakLine(
    node: ThematicBreak,
    modifier: Modifier = Modifier,
) {
    val theme = currentTheme()
    // show a horizontal line
    HorizontalLine(
        color = theme.breakLineColor,
        modifier = modifier
            .height(theme.breakLineHeight)
            .fillMaxWidth()
    )
}
