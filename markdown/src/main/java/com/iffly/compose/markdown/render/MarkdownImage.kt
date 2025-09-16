package com.iffly.compose.markdown.render

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vladsch.flexmark.ast.Image

@Composable
fun MarkdownImage(
    node: Image,
    modifier: Modifier = Modifier
) {
    val url = node.url
    val altText = node.title?.toString()?.takeIf { it.isNotEmpty() } ?: node.text?.toString()
    val context = LocalPlatformContext.current


    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url.toString())
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = altText,
        contentScale = ContentScale.FillHeight,
        modifier = modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp),
        onState = { state ->
            when (state) {
                is AsyncImagePainter.State.Error -> {
                    Log.e(
                        "MarkdownImage",
                        "Failed to load image from $url",
                        state.result.throwable
                    )
                }

                is AsyncImagePainter.State.Success -> {
                    Log.d("MarkdownImage", "Successfully loaded image from $url")
                }

                else -> {
                    // Handle other states if needed
                }
            }
        }
    )
}
