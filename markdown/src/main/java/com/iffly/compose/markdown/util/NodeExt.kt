package com.iffly.compose.markdown.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.style.currentTypographyStyle
import org.commonmark.node.BulletList
import org.commonmark.node.Emphasis
import org.commonmark.node.Heading
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.StrongEmphasis

@Composable
fun Node.getSpanStyle(): SpanStyle {
    val typographyStyle = currentTypographyStyle()
    return when (this) {
        is Heading -> this.headStyle()
        is Emphasis -> typographyStyle.emphasis
        is StrongEmphasis -> typographyStyle.strongEmphasis
        else -> typographyStyle.body
    }
}

@Composable
fun Heading.headStyle(): SpanStyle {
    val typographyStyle = currentTypographyStyle()
    return typographyStyle.head[this.level] ?: typographyStyle.body
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