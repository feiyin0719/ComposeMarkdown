package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.TypographyStyle
import com.iffly.compose.markdown.widget.richtext.appendStandaloneInlineTextContent
import com.vladsch.flexmark.ast.Image

class ImageNodeStringBuilder : IInlineNodeStringBuilder<Image> {
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
            MarkdownImage(node, modifier = modifier)
        }
        appendStandaloneInlineTextContent(imageId, "[${node.text ?: node.title ?: "Image"}]")
    }
}

