package com.iffly.compose.markdown.widget

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign

@Composable
fun BasicText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
) {
    Text(
        text = text,
        inlineContent = inlineContent,
        textAlign = textAlign,
        modifier = modifier
    )
}