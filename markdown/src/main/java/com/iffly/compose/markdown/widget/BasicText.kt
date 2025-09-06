package com.iffly.compose.markdown.widget

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

@Composable
fun BasicStringText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        textAlign = textAlign,
        modifier = modifier,
        style = style,
    )
}

@Composable
fun BasicText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
) {
    Text(
        text = text,
        inlineContent = inlineContent,
        textAlign = textAlign,
        modifier = modifier,
        style = style,
    )
}