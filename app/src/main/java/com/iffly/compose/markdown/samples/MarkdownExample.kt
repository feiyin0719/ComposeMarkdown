package com.iffly.compose.markdown.samples

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

data class MarkdownExample(
    val title: String,
    val description: String,
    val content: @Composable (PaddingValues) -> Unit,
)
