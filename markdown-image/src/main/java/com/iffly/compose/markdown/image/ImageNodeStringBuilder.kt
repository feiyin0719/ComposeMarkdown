package com.iffly.compose.markdown.image

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.render.TextMeasureContext
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.widget.LoadingView
import com.iffly.compose.markdown.widget.richtext.RichTextInlineContent
import com.iffly.compose.markdown.widget.richtext.appendStandaloneInlineTextContent
import com.vladsch.flexmark.ast.Image

/**
 * A renderer for image widgets.
 */
fun interface ImageWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    operator fun invoke(
        node: Image,
        modifier: Modifier,
    )
}

/**
 * The default loading view for image widgets.
 * @see ImageWidgetRenderer
 */
class LoadingImageWidgetRenderer : ImageWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    override fun invoke(
        node: Image,
        modifier: Modifier,
    ) {
        LoadingView(modifier)
    }
}

/**
 * The default error view for image widgets.
 * @see ImageWidgetRenderer
 */
class ErrorImageWidgetRenderer(
    private val imageTheme: ImageTheme,
) : ImageWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    override fun invoke(
        node: Image,
        modifier: Modifier,
    ) {
        MarkdownImageErrorView(
            modifier =
                modifier
                    .background(color = imageTheme.errorPlaceholderColor)
                    .clip(imageTheme.shape)
                    .then(imageTheme.modifier),
            contentScale = imageTheme.contentScale,
            alignment = imageTheme.alignment,
            altText = node.text?.toString() ?: node.title?.toString(),
        )
    }
}

/**
 * The string builder for image nodes.
 * @param loadingView The Composable to be shown while the image is loading.
 * @param errorView The Composable to be shown when the image fails to load.
 * @see IInlineNodeStringBuilder
 */
class ImageNodeStringBuilder(
    private val imageTheme: ImageTheme = ImageTheme(),
    private val loadingView: ImageWidgetRenderer = LoadingImageWidgetRenderer(),
    errorView: ImageWidgetRenderer? = null,
) : IInlineNodeStringBuilder<Image> {
    private val errorView: ImageWidgetRenderer = errorView ?: ErrorImageWidgetRenderer(imageTheme)

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Image,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        measureContext: TextMeasureContext,
    ) {
        val imageId = "image_${node.url}"
        inlineContentMap[imageId] =
            MarkdownInlineView.MarkdownRichTextInlineContent(
                RichTextInlineContent.StandaloneInlineContent(
                    modifier = Modifier,
                ) { modifier ->
                    MarkdownImage(
                        node,
                        modifier =
                            modifier
                                .clip(imageTheme.shape)
                                .then(imageTheme.modifier),
                        contentScale = imageTheme.contentScale,
                        alignment = imageTheme.alignment,
                        loadingView = { image, modifier ->
                            loadingView.invoke(image, modifier)
                        },
                        errorView = { image, modifier ->
                            this@ImageNodeStringBuilder.errorView.invoke(image, modifier)
                        },
                    )
                },
            )
        appendStandaloneInlineTextContent(imageId, "[${node.text ?: node.title ?: "Image"}]")
    }
}
