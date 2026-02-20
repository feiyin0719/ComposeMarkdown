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
import com.vladsch.flexmark.ast.ImageRef
import com.vladsch.flexmark.util.ast.Node

/**
 * A renderer for image widgets.
 */
fun interface ImageWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    operator fun invoke(
        url: String,
        contentDescription: String?,
        node: Node,
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
        url: String,
        contentDescription: String?,
        node: Node,
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
        url: String,
        contentDescription: String?,
        node: Node,
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
            altText = contentDescription,
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
        val url = node.url.unescape()
        val contentDescription = node.text?.toString() ?: node.title?.unescape()
        inlineContentMap[imageId] =
            MarkdownInlineView.MarkdownRichTextInlineContent(
                RichTextInlineContent.StandaloneInlineContent(
                    modifier = Modifier,
                ) { modifier ->
                    MarkdownImage(
                        url,
                        contentDescription,
                        node,
                        modifier =
                            modifier
                                .clip(imageTheme.shape)
                                .then(imageTheme.modifier),
                        contentScale = imageTheme.contentScale,
                        alignment = imageTheme.alignment,
                        loadingView = { url, contentDescription, image, modifier ->
                            loadingView.invoke(url, contentDescription, image, modifier)
                        },
                        errorView = { url, contentDescription, image, modifier ->
                            this@ImageNodeStringBuilder.errorView.invoke(
                                url,
                                contentDescription,
                                image,
                                modifier,
                            )
                        },
                    )
                },
            )
        appendStandaloneInlineTextContent(imageId, "[${node.text ?: node.title ?: "Image"}]")
    }
}

class ImageRefNodeStringBuilder(
    private val imageTheme: ImageTheme = ImageTheme(),
    private val loadingView: ImageWidgetRenderer = LoadingImageWidgetRenderer(),
    errorView: ImageWidgetRenderer? = null,
) : IInlineNodeStringBuilder<ImageRef> {
    private val errorView: ImageWidgetRenderer =
        errorView ?: ErrorImageWidgetRenderer(imageTheme)

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ImageRef,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        measureContext: TextMeasureContext,
    ) {
        val referenceNode = node.getReferenceNode(node.document)
        val url =
            referenceNode?.url?.unescape()?.takeIf { node.isDefined } ?: node.reference.unescape()
        val text =
            referenceNode?.title?.unescape()?.takeIf { node.isDefined }
        val imageId = "image_$url"
        inlineContentMap[imageId] =
            MarkdownInlineView.MarkdownRichTextInlineContent(
                RichTextInlineContent.StandaloneInlineContent(
                    modifier = Modifier,
                ) { modifier ->
                    MarkdownImage(
                        url = url,
                        contentDescription = node.text?.toString() ?: text.toString(),
                        node = node,
                        modifier =
                            modifier
                                .clip(imageTheme.shape)
                                .then(imageTheme.modifier),
                        contentScale = imageTheme.contentScale,
                        alignment = imageTheme.alignment,
                        loadingView = { url, contentDescription, image, modifier ->
                            loadingView.invoke(url, contentDescription, image, modifier)
                        },
                        errorView = { url, contentDescription, image, modifier ->
                            this@ImageRefNodeStringBuilder.errorView.invoke(
                                url,
                                contentDescription,
                                image,
                                modifier,
                            )
                        },
                    )
                },
            )
        appendStandaloneInlineTextContent(
            imageId,
            "[${node.text ?: text ?: "ImageRef"}]",
        )
    }
}
