package com.iffly.compose.markdown.core.renders

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.iffly.compose.markdown.R
import com.iffly.compose.markdown.config.currentActionHandler
import com.iffly.compose.markdown.widget.LoadingView
import com.vladsch.flexmark.ast.Image

/**
 * A Composable that renders a markdown image node.
 * @param node The image node to be rendered.
 * @param modifier The modifier to be applied to the image.
 * @param alignment The alignment of the image.
 * @param contentScale The content scale of the image.
 * @param errorView A Composable that is shown when the image fails to load.
 * @param loadingView A Composable that is shown while the image is loading.
 */
@Composable
fun MarkdownImage(
    node: Image,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Inside,
    errorView: @Composable (image: Image, modifier: Modifier) -> Unit = { image, modifier ->
        MarkdownImageErrorView(
            modifier = modifier,
            altText = image.text?.toString() ?: image.title?.toString(),
        )
    },
    loadingView: @Composable (image: Image, modifier: Modifier) -> Unit = { image, modifier ->
        LoadingView(modifier = modifier)
    },
) {
    val url = node.url
    val altText = node.text?.toString() ?: node.title?.toString() ?: ""
    val context = LocalPlatformContext.current
    val actionHandler = currentActionHandler()

    SubcomposeAsyncImage(
        ImageRequest
            .Builder(context)
            .data(url.toString())
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = altText,
        contentScale = contentScale,
        alignment = alignment,
        modifier =
            modifier.clickable {
                actionHandler?.handleImageClick(url.toString(), node)
            },
        loading = {
            loadingView(
                node,
                Modifier,
            )
        },
        error = {
            errorView(
                node,
                Modifier,
            )
        },
    )
}

/**
 * A Composable that shows a default error view when an image fails to load.
 * @param modifier The modifier to be applied to the error view.
 * @param alignment The alignment of the error view.
 * @param contentScale The content scale of the error view.
 * @param altText The alternative text for the error view.
 */
@Composable
fun MarkdownImageErrorView(
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Inside,
    altText: String? = null,
) {
    Image(
        painter = painterResource(id = R.drawable.ic_image_error),
        contentDescription = altText ?: "Image load failed",
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
    )
}
