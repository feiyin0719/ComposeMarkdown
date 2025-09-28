package com.iffly.compose.markdown.latex

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.iffly.compose.markdown.config.AbstractMarkdownRenderPlugin
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.vladsch.flexmark.ext.gitlab.GitLabExtension
import com.vladsch.flexmark.ext.gitlab.GitLabInlineMath
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.misc.Extension

/**
 * Markdown math plugin providing custom inline `$...$` and display `$$...$$` LaTeX support
 * using custom Flexmark parser extensions, plus (optionally) the GitLab extension if desired.
 */
class MarkdownMathPlugin(
    private val mathStyle: SpanStyle = SpanStyle(fontStyle = FontStyle.Italic),
    private val width: TextUnit,
    private val height: TextUnit,
    private val align: TextAlign = TextAlign.Center,
    /** Whether to also enable GitLabExtension (kept for backward compatibility). */
    private val enableGitLabExtension: Boolean = false,
) : AbstractMarkdownRenderPlugin() {

    override fun extensions(): List<Extension> {
        return buildList {
            add(LatexExtension.create())
            if (enableGitLabExtension) {
                add(GitLabExtension.create())
            }
        }
    }

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<out Node>> =
        buildMap {
            // Custom inline latex
            put(
                InlineLatexNode::class.java,
                InlineLatexNodeStringBuilder(
                    mathStyle,
                    width,
                    height,
                    align
                )
            )
            // Backwards compatibility: still support GitLabInlineMath if extension enabled
            if (enableGitLabExtension) {
                put(
                    GitLabInlineMath::class.java,
                    InlineMathNodeStringBuilder(
                        mathStyle,
                        width,
                        height,
                        align
                    )
                )
            }
        }

    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<out Block>> =
        mapOf(
            LatexBlock::class.java to LatexBlockRenderer(
                style = mathStyle,
                align = align,
            )
        )

}