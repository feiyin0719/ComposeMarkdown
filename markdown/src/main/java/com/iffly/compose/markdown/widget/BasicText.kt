package com.iffly.compose.markdown.widget

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString

@Composable
fun BasicText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
) {
    Text(
        text = text,
        inlineContent = inlineContent,
        modifier = modifier
    )
}