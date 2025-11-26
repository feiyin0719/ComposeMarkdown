package com.iffly.compose.markdown.latex

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import com.iffly.compose.markdown.render.IBlockRenderer

/** Renderer for display math blocks producing a bitmap using JLatexMath */
class LatexBlockRenderer(
    private val style: TextStyle,
    private val paddingValues: PaddingValues,
) : IBlockRenderer<LatexBlock> {
    @Composable
    override fun Invoke(node: LatexBlock, modifier: Modifier) {
        val density = LocalDensity.current
        val latexConfig = remember {
            style.toLatexConfig(density, paddingValues)
        }
        LatexImage(
            latex = node.formula,
            latexConfig = latexConfig,
            modifier = modifier
                .fillMaxWidth()
        )
    }
}
