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
