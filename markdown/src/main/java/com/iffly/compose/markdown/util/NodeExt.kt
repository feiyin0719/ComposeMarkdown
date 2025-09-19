package com.iffly.compose.markdown.util

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.style.TypographyStyle
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ext.tables.TableCell
import com.vladsch.flexmark.ext.tables.TableHead
import com.vladsch.flexmark.util.ast.Node

fun TypographyStyle.getNodeSpanStyle(node: Node): SpanStyle {
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

fun TypographyStyle.getNodeParagraphStyle(node: Node): ParagraphStyle {
    return when (node) {
        is Heading -> this.headParagraphStyle[node.level] ?: this.paragraphStyle
        is BulletList -> this.bulletListParagraphStyle
        is OrderedList -> this.orderListParagraphStyle
        else -> this.paragraphStyle
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

fun Node.contentText(): String {
    return chars.toString()
}

const val BULLET_POINT = "•"
fun ListItem.getMarkerText(): String {
    return when (this.parent) {
        is BulletList -> BULLET_POINT
        is OrderedList -> {
            var index = 1
            var node = this.parent?.firstChild
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