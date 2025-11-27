package com.iffly.compose.markdown.core.renders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.MarkdownContent
import com.vladsch.flexmark.util.ast.Document

@Composable
fun Document(
    node: Document,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = modifier.wrapContentSize(),
    ) {
        val theme = currentTheme()
        var child = node.firstChild
        while (child != null) {
            MarkdownContent(child, Modifier)
            if (child.next != null && theme.spacerTheme.showSpacer) {
                Spacer(Modifier.height(theme.spacerTheme.spacerHeight))
            }
            child = child.next
        }
    }
}

class DocumentRenderer : IBlockRenderer<Document> {
    @Composable
    override fun Invoke(
        node: Document,
        modifier: Modifier,
    ) {
        Document(node, modifier)
    }
}
