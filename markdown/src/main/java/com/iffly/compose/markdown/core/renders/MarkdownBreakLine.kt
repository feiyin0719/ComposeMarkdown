package com.iffly.compose.markdown.core.renders

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.render.IBlockRenderer
import com.vladsch.flexmark.ast.ThematicBreak

/**
 * The renderer for ThematicBreak nodes.
 * @see IBlockRenderer
 */
class BreakLineRenderer : IBlockRenderer<ThematicBreak> {
    @Composable
    override fun Invoke(
        node: ThematicBreak,
        modifier: Modifier,
    ) {
        BreakLine(node, modifier)
    }
}

/**
 * A Composable that renders a horizontal break line.
 * @param node The ThematicBreak node to be rendered.
 * @param modifier The modifier to be applied to the break line.
 */
@Composable
fun BreakLine(
    node: ThematicBreak,
    modifier: Modifier = Modifier,
) {
    val theme = currentTheme()
    // show a horizontal line
    HorizontalDivider(
        color = theme.breakLineColor,
        thickness = theme.breakLineHeight,
        modifier =
            modifier
                .height(theme.breakLineHeight)
                .fillMaxWidth(),
    )
}
