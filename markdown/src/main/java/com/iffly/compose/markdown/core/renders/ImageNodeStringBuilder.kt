package com.iffly.compose.markdown.core.renders

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.widget.LoadingView
import com.iffly.compose.markdown.widget.richtext.appendStandaloneInlineTextContent
import com.vladsch.flexmark.ast.Image

fun interface ImageWidgetRenderer {
    @Composable
    operator fun invoke(node: Image, modifier: Modifier)
}

class LoadingImageWidgetRenderer : ImageWidgetRenderer {
    @Composable
    override fun invoke(node: Image, modifier: Modifier) {
        LoadingView(modifier)
    }
}

class ErrorImageWidgetRenderer : ImageWidgetRenderer {
    @Composable
    override fun invoke(node: Image, modifier: Modifier) {
        val typographyStyle = currentTypographyStyle()
        MarkdownImageErrorView(
            modifier = modifier
                .background(color = typographyStyle.imageTheme.errorPlaceholderColor)
                .clip(typographyStyle.imageTheme.shape)
                .then(typographyStyle.imageTheme.modifier),
            contentScale = typographyStyle.imageTheme.contentScale,
            alignment = typographyStyle.imageTheme.alignment,
            altText = node.text?.toString() ?: node.title?.toString()
        )
    }
}

class ImageNodeStringBuilder(
    private val loadingView: ImageWidgetRenderer = LoadingImageWidgetRenderer(),
    private val errorView: ImageWidgetRenderer = ErrorImageWidgetRenderer()
) : IInlineNodeStringBuilder<Image> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Image,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        typographyStyle: TypographyStyle,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry
    ) {
        val imageId = "image_$${System.identityHashCode(node)}"
        inlineContentMap[imageId] = MarkdownInlineView.MarkdownStandaloneInlineView(
            modifier = Modifier
        ) { modifier ->
            val typographyStyle = currentTypographyStyle()
            MarkdownImage(
                node,
                modifier = modifier
                    .clip(typographyStyle.imageTheme.shape)
                    .then(typographyStyle.imageTheme.modifier),
                contentScale = typographyStyle.imageTheme.contentScale,
                alignment = typographyStyle.imageTheme.alignment,
                loadingView = { image, modifier ->
                    loadingView.invoke(image, modifier)
                },
                errorView = { image, modifier ->

                    errorView.invoke(image, modifier)
                })
        }
        appendStandaloneInlineTextContent(imageId, "[${node.text ?: node.title ?: "Image"}]")
    }
}

