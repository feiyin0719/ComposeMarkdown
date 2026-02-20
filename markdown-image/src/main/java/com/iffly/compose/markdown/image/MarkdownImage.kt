package com.iffly.compose.markdown.image

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.iffly.compose.markdown.config.currentActionHandler
import com.vladsch.flexmark.util.ast.Node

/**
 * A Composable that renders a markdown image node.
 * @param url The image url to be rendered.
 * @param contentDescription The content description of the image.
 * @param node The image node to be rendered.
 * @param modifier The modifier to be applied to the image.
 * @param alignment The alignment of the image.
 * @param contentScale The content scale of the image.
 * @param errorView A Composable that is shown when the image fails to load.
 * @param loadingView A Composable that is shown while the image is loading.
 */
@Composable
fun MarkdownImage(
    url: String,
    contentDescription: String?,
    node: Node,
    errorView: ImageWidgetRenderer,
    loadingView: ImageWidgetRenderer,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Inside,
) {
    val context = LocalPlatformContext.current
    val actionHandler = currentActionHandler()

    SubcomposeAsyncImage(
        ImageRequest
            .Builder(context)
            .data(url)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        alignment = alignment,
        modifier =
            modifier.clickable {
                actionHandler?.handleImageClick(url, node)
            },
        loading = {
            loadingView(
                url,
                contentDescription,
                node,
                Modifier,
            )
        },
        error = {
            errorView(
                url,
                contentDescription,
                node,
                Modifier,
            )
        },
    )
}
