package com.iffly.compose.markdown.image

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.config.AbstractMarkdownRenderPlugin
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.ImageRef
import com.vladsch.flexmark.util.ast.Node

/**
 * Theme for images in Markdown.
 * @param alignment The alignment of the image.
 * @param contentScale The content scale of the image.
 * @param shape The shape of the image.
 * @param modifier The modifier for the image.
 * @param errorPlaceholderColor The placeholder color when image fails to load.
 */
@Immutable
data class ImageTheme(
    val alignment: Alignment = Alignment.Center,
    val contentScale: ContentScale = ContentScale.Inside,
    val shape: Shape = RoundedCornerShape(8.dp),
    val modifier: Modifier = Modifier,
    val errorPlaceholderColor: Color = Color(0xFFE0E0E0),
)

/**
 * Markdown image plugin providing image support.
 * @see AbstractMarkdownRenderPlugin
 */
class ImageMarkdownPlugin(
    private val imageTheme: ImageTheme = ImageTheme(),
    private val loadingView: ImageWidgetRenderer = LoadingImageWidgetRenderer(),
    errorView: ImageWidgetRenderer? = null,
) : AbstractMarkdownRenderPlugin() {
    private val errorView: ImageWidgetRenderer =
        errorView ?: ErrorImageWidgetRenderer(imageTheme)

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(
            Image::class.java to
                ImageNodeStringBuilder(
                    imageTheme = imageTheme,
                    loadingView = loadingView,
                    errorView = errorView,
                ),
            ImageRef::class.java to
                ImageRefNodeStringBuilder(
                    imageTheme = imageTheme,
                    loadingView = loadingView,
                    errorView = errorView,
                ),
        )
}
