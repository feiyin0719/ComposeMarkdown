package com.iffly.compose.markdown.render

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.iffly.compose.markdown.widget.BasicText
import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.IndentedCodeBlock
import com.vladsch.flexmark.util.ast.Block

object FencedCodeBlockRenderer : IBlockRenderer<FencedCodeBlock> {
    @Composable
    override fun Invoke(
        node: FencedCodeBlock,
        modifier: Modifier
    ) {
        MarkdownCodeBlock(node = node, modifier = modifier)
    }

}

object IndentedCodeBlockRenderer : IBlockRenderer<IndentedCodeBlock> {
    @Composable
    override fun Invoke(
        node: IndentedCodeBlock,
        modifier: Modifier
    ) {
        MarkdownCodeBlock(node = node, modifier = modifier)
    }

}

@Composable
fun MarkdownCodeBlock(
    node: Block,
    modifier: Modifier = Modifier
) {
    if (node !is FencedCodeBlock && node !is IndentedCodeBlock) return
    val codeText = node.contentChars.toString()
    val language = if (node is FencedCodeBlock) {
        node.info.toString().trim().takeIf { it.isNotEmpty() } ?: "text"
    } else {
        "text"
    }
    val typographyStyle = currentTypographyStyle()
    val styledText = buildCodeText(codeText)
    Column {
        // Header with separate background
        CodeHeader(
            language,
            codeText,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = typographyStyle.code.background,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
        ) {
            // Code content with top padding to account for header
            BasicText(
                text = styledText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
                    .horizontalScroll(rememberScrollState())
            )
        }
    }

}

@Composable
private fun CodeHeader(
    language: String,
    codeText: String,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val typographyStyle = currentTypographyStyle()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = typographyStyle.codeTitleBackgroundColor,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Language label
        Text(
            text = language.uppercase(),
            style = typographyStyle.codeTitleStyle,
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )

        // Copy button
        Text(
            text = "Copy",
            style = typographyStyle.codeCopyStyle,
            modifier = Modifier
                .clickable {
                    clipboardManager.setText(AnnotatedString(codeText))
                }
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun buildCodeText(codeText: String): AnnotatedString {
    val typographyStyle = currentTypographyStyle()

    val styledText = AnnotatedString.Builder().apply {
        withStyle(
            style = typographyStyle.code
        ) {
            append(codeText)
        }
    }.toAnnotatedString()
    return styledText
}
