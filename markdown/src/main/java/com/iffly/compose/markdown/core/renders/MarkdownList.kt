package com.iffly.compose.markdown.core.renders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.MarkdownContent
import com.iffly.compose.markdown.util.getMarkerText
import com.vladsch.flexmark.ast.BulletListItem
import com.vladsch.flexmark.ast.ListBlock
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.OrderedListItem

open class ListBlockRenderer : IBlockRenderer<ListBlock> {
    @Composable
    override fun Invoke(node: ListBlock, modifier: Modifier) {
        Column(
            verticalArrangement = Arrangement.Top, modifier = modifier.wrapContentSize()
        ) {
            val typographyStyle = currentTypographyStyle()
            var child = node.firstChild
            val spacerHeight =
                if (node.isLoose) typographyStyle.spaceHeight else typographyStyle.listTightSpaceSize
            while (child != null) {
                MarkdownContent(child, Modifier)
                if (child.next != null && typographyStyle.showSpace) {
                    Spacer(Modifier.height(spacerHeight))
                }
                child = child.next
            }
        }
    }
}

class OrderedListBlockRenderer : ListBlockRenderer()

class BulletListBlockRenderer : ListBlockRenderer()

abstract class ListItemRenderer<in T : ListItem> : IBlockRenderer<T> {
    abstract fun getMarker(node: T): String

    @Composable
    override fun Invoke(node: T, modifier: Modifier) {
        val typographyStyle = currentTypographyStyle()
        val marker = getMarker(node)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()

        ) {
            Text(
                marker,
                modifier = Modifier,
                style = typographyStyle.textStyle,
                textAlign = TextAlign.End,
            )
            Spacer(modifier = Modifier.width(typographyStyle.listMarkerSpaceSize))
            val spacerHeight =
                if (node.isLoose) typographyStyle.spaceHeight else typographyStyle.listTightSpaceSize
            Column(verticalArrangement = Arrangement.Top, modifier = Modifier.wrapContentSize()) {
                var child = node.firstChild
                while (child != null) {
                    MarkdownContent(child, Modifier)
                    if (child.next != null && typographyStyle.showSpace) {
                        Spacer(Modifier.height(spacerHeight))
                    }
                    child = child.next
                }

            }
        }
    }
}

class OrderedListItemRenderer : ListItemRenderer<OrderedListItem>() {
    override fun getMarker(node: OrderedListItem): String {
        val delimiter = (node.parent as? OrderedList)?.delimiter ?: "."
        return "${node.getMarkerText()}${delimiter}"
    }

}

class BulletListItemRenderer : ListItemRenderer<BulletListItem>() {
    override fun getMarker(node: BulletListItem): String {
        return node.getMarkerText()
    }

}

