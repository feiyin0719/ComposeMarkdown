package com.iffly.compose.markdown.util

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.render.NodeContentHashProvider
import com.iffly.compose.markdown.style.MarkdownTheme
import com.vladsch.flexmark.ast.BlockQuote
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.HardLineBreak
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.HtmlBlock
import com.vladsch.flexmark.ast.HtmlInline
import com.vladsch.flexmark.ast.IndentedCodeBlock
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.SoftLineBreak
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.ast.ThematicBreak
import com.vladsch.flexmark.util.ast.Document
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

fun Node.contentText(): String = chars.toString()

/**
 * Calculates a stable hash from the render-relevant content of this node and its descendants.
 * Traversal is iterative and bounded to avoid expensive work for unusually large subtrees.
 */
fun Node.contentHash(): Int {
    val stack = ArrayDeque<Node>()
    stack.addLast(this)
    var hash = 0
    var count = 0
    while (stack.isNotEmpty()) {
        val node = stack.removeLast()
        if (count >= CONTENT_HASH_MAX_NODES) {
            return HASH_MULTIPLIER * hash + System.identityHashCode(this)
        }
        hash = HASH_MULTIPLIER * hash + node.selfContentHash()
        count++

        var child = node.lastChild
        while (child != null) {
            stack.addLast(child)
            child = child.previous
        }
    }
    return hash
}

private fun Node.selfContentHash(): Int =
    when (this) {
        is NodeContentHashProvider -> contentHash()
        is Code, is HtmlInline, is HtmlBlock, is Text, is Link -> chars.toString().hashCode()
        is FencedCodeBlock -> HASH_MULTIPLIER * info.toString().hashCode() + contentChars.toString().hashCode()
        is IndentedCodeBlock -> contentChars.toString().hashCode()
        is Heading -> level
        is OrderedList -> HASH_MULTIPLIER * isTight.hashCode() + startNumber
        is BulletList -> isTight.hashCode()
        is Paragraph -> javaClass.name.hashCode()
        is BlockQuote -> javaClass.name.hashCode()
        is ListItem -> javaClass.name.hashCode()
        is Document -> javaClass.name.hashCode()
        is HardLineBreak -> javaClass.name.hashCode()
        is SoftLineBreak -> javaClass.name.hashCode()
        is ThematicBreak -> javaClass.name.hashCode()
        is Emphasis -> javaClass.name.hashCode()
        is StrongEmphasis -> javaClass.name.hashCode()
        else -> chars.toString().takeIf(String::isNotEmpty)?.hashCode() ?: toString().hashCode()
    }

private const val HASH_MULTIPLIER = 31
private const val CONTENT_HASH_MAX_NODES = 1024

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

fun Node.isInQuoteBlock(): Boolean {
    var parent = this.parent
    while (parent != null) {
        if (parent is BlockQuote) {
            return true
        }
        parent = parent.parent
    }
    return false
}
