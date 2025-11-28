package com.iffly.compose.markdown.util

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.style.MarkdownTheme
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ext.tables.TableHead
import com.vladsch.flexmark.util.ast.Node

fun MarkdownTheme.getNodeSpanStyle(node: Node): SpanStyle =
    when (node) {
        is Heading -> this.headStyle[node.level]?.toSpanStyle() ?: this.textStyle.toSpanStyle()
        else -> this.textStyle.toSpanStyle()
    }

fun MarkdownTheme.getNodeParagraphStyle(node: Node?): ParagraphStyle =
    when (node) {
        is Heading -> {
            this.headStyle[node.level]?.toParagraphStyle()
                ?: this.textStyle.toParagraphStyle()
        }

        else -> {
            this.textStyle.toParagraphStyle()
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

fun Node.contentText(): String = chars.toString()

const val BULLET_POINT = "•"

fun ListItem.getMarkerText(): String =
    when (this.parent) {
        is BulletList -> {
            BULLET_POINT
        }

        is OrderedList -> {
            var max = 1
            var index = 1
            var node = this.parent?.firstChild
            while (node != null) {
                if (node == this) {
                    index = max
                }
                max++
                node = node.next
            }
            val maxLength = max.toString().length
            val indexString = index.toString()
            val indexLength = indexString.length
            "${StringExt.FIGURE_SPACE.repeat(maxLength - indexLength)}$indexString."
        }

        else -> {
            ""
        }
    }

fun ListItem.getIndentLevel(): Int {
    var level = 0
    var parent = this.parent
    while (parent != null) {
        if (parent is ListItem) {
            level++
        }
        parent = parent.parent
    }
    return level
}
