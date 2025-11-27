package com.iffly.compose.markdown.core.renders

import androidx.compose.foundation.Image
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
import com.iffly.compose.markdown.widget.LoadingView
import com.vladsch.flexmark.ast.Image

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
        modifier = modifier,
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
