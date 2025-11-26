package com.iffly.compose.markdown.latex

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    private val mathStyle: TextStyle = TextStyle(
        fontStyle = FontStyle.Italic,
        textAlign = TextAlign.Center
    ),
    private val paddingValues: PaddingValues = PaddingValues(0.dp),
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

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<*>> =
        buildMap {
            // Custom inline latex
            put(
                InlineLatexNode::class.java,
                InlineLatexNodeStringBuilder(
                    mathStyle,
                    paddingValues,
                )
            )
            // Backwards compatibility: still support GitLabInlineMath if extension enabled
            if (enableGitLabExtension) {
                put(
                    GitLabInlineMath::class.java,
                    GitLabInlineMathNodeStringBuilder(
                        mathStyle,
                        paddingValues,
                    )
                )
            }
        }

    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<*>> =
        mapOf(
            LatexBlock::class.java to LatexBlockRenderer(
                style = mathStyle,
                paddingValues = paddingValues,
            )
        )

}