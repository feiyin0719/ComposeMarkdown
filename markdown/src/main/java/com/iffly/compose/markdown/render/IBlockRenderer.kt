package com.iffly.compose.markdown.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vladsch.flexmark.util.ast.Block

interface IBlockRenderer<T> where T : Block {

    @Composable
    fun Invoke(node: T, modifier: Modifier)
}