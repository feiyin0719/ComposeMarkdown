package com.iffly.compose.markdown.latex

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.render.IBlockRenderer

/** Renderer for display math blocks producing a bitmap using JLatexMath */
class LatexBlockRenderer(
    private val style: SpanStyle,
    private val align: TextAlign,
) : IBlockRenderer<LatexBlock> {
    @Composable
    override fun Invoke(node: LatexBlock, modifier: Modifier) {
        LatexImageBox(
            latex = node.formula,
            style = style,
            align = align,
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}
