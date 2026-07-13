package com.iffly.compose.markdown.latex

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineView
import com.iffly.compose.markdown.render.NodeStringBuilderContext
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.render.appendMarkdownInlineContent
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.toPlaceholderSp
import com.iffly.compose.markdown.widget.richtext.RichTextInlineContent
import com.vladsch.flexmark.ext.gitlab.GitLabInlineMath
import com.vladsch.flexmark.util.ast.Node

/**
 * Inline math renderer using jlatexmath to draw a LaTeX formula to a bitmap.
 * @param T The type of node to render.
 * @param textStyle The text style to use for rendering the LaTeX formula.
 * @param paddingValues The padding values to use for rendering the LaTeX formula.
 * @param useAdaptiveInlineContent Whether to use adaptive inline content size.
 * @see IInlineNodeStringBuilder
 */
open class InlineMathNodeStringBuilder<T : Node>(
    private val textStyle: TextStyle,
    private val paddingValues: PaddingValues,
    private val useAdaptiveInlineContent: Boolean = true,
) : IInlineNodeStringBuilder<T> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: T,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val latexBody =
            when (node) {
                is GitLabInlineMath -> node.text.toString()
                is InlineLatexNode -> node.formula
                else -> return
            }
        val latexConfig = textStyle.toLatexConfig(nodeStringBuilderContext.layoutContext.density, paddingValues)
        val inlineContent =
            if (useAdaptiveInlineContent) {
                val height = textStyle.fontSize * 1.3f
                RichTextInlineContent.EmbeddedRichTextInlineContent(
                    placeholder =
                        Placeholder(
                            width = 1.sp,
                            height = height,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                        ),
                    adjustSizeByContent = true,
                ) {
                    LatexImage(
                        latex = latexBody,
                        latexConfig = latexConfig,
                        modifier = Modifier.wrapContentSize(),
                    )
                }
            } else {
                val drawable =
                    LatexBitmapLoader.createDrawable(
                        latexBody,
                        latexConfig,
                    )
                if (drawable == null) {
                    append(latexBody)
                    return
                }
                val density = nodeStringBuilderContext.layoutContext.density
                val width = density.toPlaceholderSp(drawable.intrinsicWidth)
                val height = density.toPlaceholderSp(drawable.intrinsicHeight)
                RichTextInlineContent.EmbeddedRichTextInlineContent(
                    placeholder =
                        Placeholder(
                            width = width,
                            height = height,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                        ),
                ) {
                    LatexImage(
                        latex = latexBody,
                        latexConfig = latexConfig,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

        appendMarkdownInlineContent(
            id = node.javaClass.simpleName,
            inlineContent = inlineContent,
            inlineContentMap = inlineContentMap,
            alternateText = "${'$'}$latexBody${'$'}",
        )
    }
}

/**
 * The string builder for inline LaTeX nodes.
 * @param textStyle The text style to use for rendering the LaTeX formula.
 * @param paddingValues The padding values to use for rendering the LaTeX formula.
 * @param useAdaptiveInlineContent Whether to use adaptive inline content size.
 * @see IInlineNodeStringBuilder
 */
class InlineLatexNodeStringBuilder(
    textStyle: TextStyle,
    paddingValues: PaddingValues,
    useAdaptiveInlineContent: Boolean = true,
) : InlineMathNodeStringBuilder<InlineLatexNode>(textStyle, paddingValues, useAdaptiveInlineContent)

/**
 * The string builder for GitLab inline math nodes.
 * @param textStyle The text style to use for rendering the LaTeX formula.
 * @param paddingValues The padding values to use for rendering the LaTeX formula.
 * @param useAdaptiveInlineContent Whether to use adaptive inline content size.
 * @see IInlineNodeStringBuilder
 */
class GitLabInlineMathNodeStringBuilder(
    textStyle: TextStyle,
    paddingValues: PaddingValues,
    useAdaptiveInlineContent: Boolean = true,
) : InlineMathNodeStringBuilder<GitLabInlineMath>(textStyle, paddingValues, useAdaptiveInlineContent)
