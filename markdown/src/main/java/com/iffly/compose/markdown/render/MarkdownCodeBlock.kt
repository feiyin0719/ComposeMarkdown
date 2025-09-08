package com.iffly.compose.markdown.render

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.iffly.compose.markdown.widget.BasicText
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.IndentedCodeBlock

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
    val typographyStyle = currentTypographyStyle()
    val styledText = buildCodeText(codeText)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = typographyStyle.code.background,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        BasicText(
            text = styledText,
            modifier = Modifier.horizontalScroll(rememberScrollState())
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
