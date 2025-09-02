package com.iffly.compose.markdown.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.commonmark.node.Block

interface IBlockRenderer<T> where T : Block {

    @Composable
    fun Invoke(node: T, modifier: Modifier)
}