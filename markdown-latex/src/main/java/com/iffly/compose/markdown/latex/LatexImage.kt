package com.iffly.compose.markdown.latex

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage


@Composable
fun LatexImage(
    latex: String,
    latexConfig: LatexConfig,
    modifier: Modifier = Modifier,
) {

    val drawable =
        remember(latexConfig, latex) {
            LatexBitmapLoader.createDrawable(latex, latexConfig)
        }

    // Display the bitmap if it's ready
    AsyncImage(
        model = drawable,
        contentDescription = "LaTeX Image: $latex",
        modifier = modifier,
        contentScale = ContentScale.Inside,
        alignment = Alignment.Center,
    )
}