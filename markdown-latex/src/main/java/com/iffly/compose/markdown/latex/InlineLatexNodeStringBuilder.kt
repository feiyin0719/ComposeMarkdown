package com.iffly.compose.markdown.latex

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineTextContent
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.TypographyStyle

class InlineLatexNodeStringBuilder(
    private val mathStyle: SpanStyle,
    private val width: TextUnit,
    private val height: TextUnit,
    private val align: TextAlign,
) : IInlineNodeStringBuilder<InlineLatexNode> {
    private val paragraphStyle = ParagraphStyle(lineHeight = height)

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: InlineLatexNode,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
    ) {
        val latexBody = node.formula
        val placeholderId =
            "inline_latex_${node.startOffset}_${node.endOffset}_${latexBody.hashCode()}"
        inlineContentMap[placeholderId] = MarkdownInlineTextContent(
            placeholder = Placeholder(
                width = width,
                height = height,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            LatexImageBox(
                latex = latexBody,
                style = mathStyle,
                align = align,
                modifier = Modifier.fillMaxSize()
            )
        }
        withStyle(paragraphStyle) {
            appendInlineContent(placeholderId, "$${latexBody}$")
        }
    }
}