package com.iffly.compose.markdown.latex

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.util.LRUCache
import ru.noties.jlatexmath.JLatexMathDrawable
import ru.noties.jlatexmath.JLatexMathDrawable.ALIGN_CENTER
import ru.noties.jlatexmath.JLatexMathDrawable.ALIGN_LEFT
import ru.noties.jlatexmath.JLatexMathDrawable.ALIGN_RIGHT

data class LatexConfig(
    val textSize: Float,
    val color: Int,
    val backgroundColor: Int,
    val align: Int,
    val padding: android.graphics.Rect,
)

fun TextStyle.toLatexConfig(
    density: Density,
    paddingValues: PaddingValues,
): LatexConfig =
    LatexConfig(
        textSize = with(density) { if (fontSize.isUnspecified) 16.sp.toPx() else fontSize.toPx() },
        color = this.color.toArgb(),
        backgroundColor = this.background.toArgb(),
        align = this.textAlign.toJLatexMathAlign(),
        padding =
            android.graphics.Rect(
                with(density) {
                    paddingValues.calculateLeftPadding(LayoutDirection.Ltr).toPx().toInt()
                },
                with(density) { paddingValues.calculateTopPadding().toPx().toInt() },
                with(density) {
                    paddingValues.calculateRightPadding(LayoutDirection.Ltr).toPx().toInt()
                },
                with(density) { paddingValues.calculateBottomPadding().toPx().toInt() },
            ),
    )

private fun TextAlign.toJLatexMathAlign(): Int =
    when (this) {
        TextAlign.Left -> ALIGN_LEFT
        TextAlign.Start -> ALIGN_RIGHT
        TextAlign.Center -> ALIGN_CENTER
        else -> ALIGN_CENTER
    }

object LatexBitmapLoader {
    private val cache: LRUCache<String, Bitmap> = LRUCache(50)

    private val drawableCache = LRUCache<String, JLatexMathDrawable>(50)

    fun createDrawable(
        mathExpression: String,
        config: LatexConfig,
    ): JLatexMathDrawable? =
        drawableCache[mathExpression] ?: run {
            try {
                val drawable =
                    JLatexMathDrawable
                        .builder(mathExpression)
                        .textSize(config.textSize)
                        .color(config.color)
                        .background(config.backgroundColor)
                        .align(config.align)
                        .padding(
                            config.padding.left,
                            config.padding.top,
                            config.padding.right,
                            config.padding.bottom,
                        ).build()
                drawableCache[mathExpression] = drawable
                drawable
            } catch (_: Exception) {
                return null
            }
        }
}
