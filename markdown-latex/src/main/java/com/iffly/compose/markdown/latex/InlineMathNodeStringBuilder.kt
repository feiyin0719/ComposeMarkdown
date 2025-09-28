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
import com.iffly.compose.markdown.render.InlineNodeStringBuilders
import com.iffly.compose.markdown.render.MarkdownInlineTextContent
import com.iffly.compose.markdown.style.TypographyStyle
import com.vladsch.flexmark.ext.gitlab.GitLabInlineMath

/**
 * Inline math renderer using jlatexmath to draw a LaTeX formula to a bitmap.
 */
class InlineMathNodeStringBuilder(
    private val mathStyle: SpanStyle,
    private val width: TextUnit,
    private val height: TextUnit,
    private val align: TextAlign,
) :
    IInlineNodeStringBuilder<GitLabInlineMath> {
    private val paragraphStyle = ParagraphStyle(
        lineHeight = height,
    )

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: GitLabInlineMath,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders
    ) {
        val latexBody = node.text.toString()
        val placeholderId =
            "inline_math_${node.startOffset}_${node.endOffset}_${latexBody.hashCode()}"
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
            appendInlineContent(placeholderId, "${'$'}$latexBody${'$'}")
        }
    }
}
