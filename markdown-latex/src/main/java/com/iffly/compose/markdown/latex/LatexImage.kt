package com.iffly.compose.markdown.latex

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun LatexImage(
    latex: String,
    latexConfig: LatexConfig,
    modifier: Modifier = Modifier,
) {
    var drawable by remember {
        mutableStateOf<Drawable?>(null)
    }

    LaunchedEffect(latexConfig) {
        drawable =
            withContext(Dispatchers.Default) {
                LatexBitmapLoader.createDrawable(latex, latexConfig)
            }
    }

    drawable?.let {
        // Display the bitmap if it's ready
        AsyncImage(
            model = drawable,
            contentDescription = "LaTeX Image: $latex",
            modifier = modifier,
            contentScale = ContentScale.Inside,
            alignment = Alignment.Center,
        )
    }
}
