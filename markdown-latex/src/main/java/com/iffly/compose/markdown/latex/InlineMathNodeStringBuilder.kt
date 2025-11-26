package com.iffly.compose.markdown.latex

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.render.TextMeasureContext
import com.iffly.compose.markdown.render.toFixedSizeMarkdownInlineTextContent
import com.iffly.compose.markdown.style.MarkdownTheme
import com.vladsch.flexmark.ext.gitlab.GitLabInlineMath
import com.vladsch.flexmark.util.ast.Node

/**
 * Inline math renderer using jlatexmath to draw a LaTeX formula to a bitmap.
 */
open class InlineMathNodeStringBuilder<T : Node>(
    private val textStyle: TextStyle,
    private val paddingValues: PaddingValues,
) :
    IInlineNodeStringBuilder<T> {

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: T,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        measureContext: TextMeasureContext
    ) {
        val latexBody = when (node) {
            is GitLabInlineMath -> node.text.toString()
            is InlineLatexNode -> node.formula
            else -> return
        }
        val placeholderId =
            "inline_math_${node.startOffset}_${node.endOffset}_${latexBody.hashCode()}"
        val latexConfig = textStyle.toLatexConfig(measureContext.density, paddingValues)
        val drawable = LatexBitmapLoader.createDrawable(
            latexBody,
            latexConfig
        )
        val width = with(measureContext.density) { drawable.intrinsicWidth.toSp() }
        val height = with(measureContext.density) { drawable.intrinsicHeight.toSp() }
        inlineContentMap[placeholderId] = InlineTextContent(
            placeholder = Placeholder(
                width = width,
                height = height,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            LatexImage(
                latex = latexBody,
                latexConfig = latexConfig,
                modifier = Modifier.fillMaxWidth()
            )
        }.toFixedSizeMarkdownInlineTextContent()
        appendInlineContent(placeholderId, "${'$'}$latexBody${'$'}")
    }
}

class InlineLatexNodeStringBuilder(
    textStyle: TextStyle,
    paddingValues: PaddingValues,
) : InlineMathNodeStringBuilder<InlineLatexNode>(textStyle, paddingValues)

class GitLabInlineMathNodeStringBuilder(
    textStyle: TextStyle,
    paddingValues: PaddingValues
) : InlineMathNodeStringBuilder<GitLabInlineMath>(textStyle, paddingValues)
