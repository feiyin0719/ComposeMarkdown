package com.iffly.compose.markdown.latex

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import ru.noties.jlatexmath.JLatexMathDrawable.ALIGN_CENTER
import ru.noties.jlatexmath.JLatexMathDrawable.ALIGN_LEFT
import ru.noties.jlatexmath.JLatexMathDrawable.ALIGN_RIGHT

private fun TextAlign.toJLatexMathAlign(): Int {
    return when (this) {
        TextAlign.Left -> ALIGN_LEFT
        TextAlign.Start -> ALIGN_RIGHT
        TextAlign.Center -> ALIGN_CENTER
        else -> ALIGN_CENTER
    }
}

private fun TextAlign.toContentAlignment(): Alignment {
    return when (this) {
        TextAlign.Left, TextAlign.Start -> Alignment.CenterStart
        TextAlign.Right, TextAlign.End -> Alignment.CenterEnd
        TextAlign.Center, TextAlign.Justify -> Alignment.Center
        else -> Alignment.Center
    }
}

@Composable
fun LatexImageBox(
    latex: String,
    style: SpanStyle,
    align: TextAlign,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = align.toContentAlignment(),
        modifier = modifier,
    ) {
        LatexImage(
            latex = latex,
            style = style,
            align = align,
        )
    }
}

@Composable
private fun LatexImage(
    latex: String,
    style: SpanStyle,
    align: TextAlign,
    modifier: Modifier = Modifier,
) {

    val density = LocalDensity.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val latexConfig = remember(style, density) {
        LatexConfig(
            textSize = with(density) { if (style.fontSize.isUnspecified) 16.sp.toPx() else style.fontSize.toPx() },
            color = style.color.toArgb(),
            backgroundColor = style.background.toArgb(),
            align = align.toJLatexMathAlign(),
        )
    }

    LaunchedEffect(latex, latexConfig) {
        // Load and render the LaTeX formula in a background thread
        val bitmap = LatexBitmapLoader.createBitmap(
            latex,
            latexConfig
        )
        imageBitmap = bitmap.asImageBitmap()
    }

    // Display the bitmap if it's ready
    imageBitmap?.let { img ->
        Image(
            bitmap = img,
            contentDescription = "LaTeX formula: $latex",
            modifier = modifier
        )
    }
}