package com.iffly.compose.markdown.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vladsch.flexmark.util.ast.Block

/**
 * Interface for rendering a specific block node type.
 * Implement this interface for each block node type you want to support.
 * @param T The type of the block node, must be a subclass of [Block].
 */
interface IBlockRenderer<in T> where T : Block {
    /**
     * Whether to skip rendering for the given node entirely.
     * When true, the block will not be rendered and no spacer will be added around it.
     * Default is false.
     */
    fun shouldSkipRender(node: T): Boolean = false

    /**
     * Whether this block renderer supports text-mode rendering via [MarkdownText].
     * When false, this renderer will not be wrapped as inline content in
     * [RenderRegistry.textModeRegistry] and the block will not appear in text mode.
     * Default is true.
     */
    fun supportTextMode(): Boolean = true

    /**
     * Composable function to render the given block node.
     * @param node The block node to render.
     * @param modifier Modifier to be applied to the rendered content.
     */
    @Composable
    fun Invoke(
        node: T,
        modifier: Modifier,
    )
}
