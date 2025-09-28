package com.iffly.compose.markdown.latex

import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import com.iffly.compose.markdown.util.LRUCache
import ru.noties.jlatexmath.JLatexMathDrawable

data class LatexConfig(
    val textSize: Float,
    val color: Int,
    val backgroundColor: Int,
    val align: Int,
)

object LatexBitmapLoader {
    private val cache: LRUCache<String, Bitmap> = LRUCache(50)

    fun createBitmap(mathExpression: String, config: LatexConfig): Bitmap {
        return cache[mathExpression] ?: run {
            val drawable = JLatexMathDrawable.builder(mathExpression)
                .textSize(config.textSize)
                .color(config.color)
                .background(config.backgroundColor)
                .align(config.align)
                .build()
            val bitmap = drawable.toBitmap(
                width = drawable.intrinsicWidth,
                height = drawable.intrinsicHeight,
                config = Bitmap.Config.ARGB_8888
            )
            cache[mathExpression] = bitmap
            bitmap
        }
    }
}