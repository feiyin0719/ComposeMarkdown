package com.iffly.compose.markdown.util

import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.style.TypographyStyle
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.node.BulletList
import org.commonmark.node.Emphasis
import org.commonmark.node.Heading
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.StrongEmphasis

fun TypographyStyle.getNodeStyle(node: Node): SpanStyle {
    return when (node) {
        is Heading -> this.head[node.level] ?: this.body
        is Emphasis -> this.emphasis
        is StrongEmphasis -> this.strongEmphasis
        is TableCell -> {
            // Check if this cell is in a table header
            if (node.isInTableHeader()) this.tableHeader else this.tableCell
        }

        else -> this.body
    }
}

private fun Node.isInTableHeader(): Boolean {
    var parent = this.parent
    while (parent != null) {
        if (parent is TableHead) return true
        parent = parent.parent
    }
    return false
}


const val BULLET_POINT = "•"
fun ListItem.getMarkerText(): String {
    return when (this.parent) {
        is BulletList -> BULLET_POINT
        is OrderedList -> {
            var index = 1
            var node = this.parent.firstChild
            while (node != null) {
                if (node == this) {
                    break
                }
                index++
                node = node.next
            }
            index.toString()
        }

        else -> ""
    }
}