/*
 * Copyright (c) 2025.
 * Microsoft Corporation. All rights reserved.
 */

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
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.MarkdownContent
import com.iffly.compose.markdown.util.StringExt.FIGURE_SPACE
import com.iffly.compose.markdown.util.getIndentLevel
import com.iffly.compose.markdown.util.getMarkerText
import com.iffly.compose.markdown.widget.SelectionFormatText
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.BulletListItem
import com.vladsch.flexmark.ast.ListBlock
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.OrderedListItem

/**
 * The renderer for ListBlock nodes.
 */
open class ListRenderer<T : ListBlock> : IBlockRenderer<T> {
    @Composable
    override fun Invoke(
        node: T,
        modifier: Modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = modifier,
        ) {
            val theme = currentTheme()
            val spacerHeight =
                if (node.isLoose) theme.spacerTheme.spacerHeight else theme.listTheme.tightListSpacerHeight
            var child = node.firstChild
            while (child != null) {
                MarkdownContent(
                    node = child,
                    modifier =
                        Modifier
                            .wrapContentHeight()
                            .fillMaxWidth(),
                )
                if (child.next != null && theme.spacerTheme.showSpacer) {
                    Spacer(Modifier.height(spacerHeight))
                }
                child = child.next
            }
        }
    }
}

/**
 * The renderer for OrderedList nodes.
 */
class OrderedListRenderer : ListRenderer<OrderedList>()

/**
 * The renderer for BulletList nodes.
 */
class BulletListRenderer : ListRenderer<BulletList>()

/**
 * A functional interface for rendering ListItem markers.
 * @param T The type of ListItem.
 */
fun interface ListItemMarkerRenderer<in T : ListItem> {
    @Composable
    operator fun invoke(
        node: T,
        modifier: Modifier,
    )
}

class ListItemMarkerRendererImpl<T : ListItem> : ListItemMarkerRenderer<T> {
    @Composable
    override fun invoke(
        node: T,
        modifier: Modifier,
    ) {
        val marker = node.getMarkerText()
        val theme = currentTheme()
        Text(
            text = marker,
            style = theme.listTheme.markerTextStyle ?: theme.textStyle,
            modifier = modifier,
        )
    }
}

/**
 * The base renderer for ListItem nodes.
 * @param T The type of ListItem.
 * @param markerRenderer The renderer for the list item marker.
 * You can provide a custom implementation.
 */
open class BaseListItemRenderer<T : ListItem>(
    private val markerRenderer: ListItemMarkerRenderer<T> = ListItemMarkerRendererImpl(),
) : IBlockRenderer<T> {
    @Composable
    override fun Invoke(
        node: T,
        modifier: Modifier,
    ) {
        val theme = currentTheme()
        val listTheme = theme.listTheme
        val intentLevel = node.getIndentLevel()
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
        ) {
            if (node.parent?.firstChild != node && intentLevel > 0) {
                // add invisible space to align with above item marker for selection-copy purpose
                SelectionFormatText(FIGURE_SPACE.repeat(intentLevel))
            }
            // Render the marker
            markerRenderer(node = node, modifier = Modifier.wrapContentHeight())
            Spacer(modifier = Modifier.width(listTheme.markerSpacerWidth))
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.wrapContentSize(),
            ) {
                val spacerHeight =
                    if (node.isLoose) theme.spacerTheme.spacerHeight else theme.listTheme.tightListSpacerHeight
                var child = node.firstChild
                while (child != null) {
                    if (child != node.firstChild) {
                        // add invisible space to align with marker for selection-copy purpose
                        SelectionFormatText(FIGURE_SPACE.repeat(intentLevel + 1))
                    }
                    MarkdownContent(
                        node = child,
                        modifier =
                            Modifier
                                .wrapContentHeight()
                                .fillMaxWidth(),
                    )
                    if (child.next != null && theme.spacerTheme.showSpacer) {
                        Spacer(Modifier.height(spacerHeight))
                    }
                    child = child.next
                }
            }
        }
    }
}

/**
 * The renderer for OrderedListItem nodes.
 */
class OrderedListItemRenderer(
    markerRenderer: ListItemMarkerRenderer<OrderedListItem> = ListItemMarkerRendererImpl(),
) : BaseListItemRenderer<OrderedListItem>(markerRenderer = markerRenderer)

/**
 * The renderer for BulletListItem nodes.
 */
class BulletListItemRenderer(
    markerRenderer: ListItemMarkerRenderer<BulletListItem> = ListItemMarkerRendererImpl(),
) : BaseListItemRenderer<BulletListItem>(markerRenderer = markerRenderer)
