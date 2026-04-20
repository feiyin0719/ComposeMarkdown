/*
 * Copyright (c) 2025.
 * Microsoft Corporation. All rights reserved.
 */

package com.iffly.compose.markdown.core.renders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.MarkdownChildren
import com.iffly.compose.markdown.util.StringExt.FIGURE_SPACE
import com.iffly.compose.markdown.util.getIndentLevel
import com.iffly.compose.markdown.util.getMarkerText
import com.iffly.compose.markdown.util.isInQuoteBlock
import com.iffly.compose.markdown.widget.SelectionFormatText
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.BulletListItem
import com.vladsch.flexmark.ast.ListBlock
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.OrderedListItem

/**
 * The renderer for ListBlock nodes.
 * @see IBlockRenderer
 */
open class ListRenderer<T : ListBlock> : IBlockRenderer<T> {
    @Composable
    override fun Invoke(
        node: T,
        modifier: Modifier,
    ) {
        val theme = currentTheme()
        val spacerHeight =
            if (node.isLoose) theme.spacerTheme.spacerHeight else theme.listTheme.tightListSpacerHeight
        MarkdownChildren(
            parent = node,
            modifier = modifier,
            verticalArrangement = Arrangement.Top,
            spacerHeight = spacerHeight,
        )
    }
}

/**
 * The renderer for OrderedList nodes.
 * @see ListRenderer
 */
class OrderedListRenderer : ListRenderer<OrderedList>()

/**
 * The renderer for BulletList nodes.
 * @see ListRenderer
 */
class BulletListRenderer : ListRenderer<BulletList>()

/**
 * Interface for rendering ListItem markers via [DrawScope].
 * The marker is drawn on canvas instead of being a separate composable,
 * so it does not participate in text selection and avoids extra `\n` when copying.
 *
 * The workflow is: [measureMarker] → use [TextLayoutResult] for offset calculation → [DrawScope.drawMarker].
 * @param T The type of ListItem.
 */
interface ListItemMarkerRenderer<in T : ListItem> {
    /**
     * Measure the marker text and return the [TextLayoutResult].
     * The caller uses [TextLayoutResult.size] to compute the content offset,
     * then passes the same result to [drawMarker] for rendering.
     * @param textMeasurer The text measurer for measuring text.
     * @param node The list item node.
     * @param style The merged text style for the marker.
     * @return The [TextLayoutResult] of the measured marker text.
     */
    fun measureMarker(
        textMeasurer: TextMeasurer,
        node: @UnsafeVariance T,
        style: TextStyle,
    ): TextLayoutResult

    /**
     * Draw the marker in the given [DrawScope] using a pre-measured [TextLayoutResult].
     * @param textLayoutResult The result returned by [measureMarker].
     */
    fun DrawScope.drawMarker(textLayoutResult: TextLayoutResult)
}

/**
 * The default implementation of [ListItemMarkerRenderer].
 * Draws the bullet point or ordered number marker using [drawText].
 * @param T The type of ListItem.
 */
class ListItemMarkerRendererImpl<T : ListItem> : ListItemMarkerRenderer<T> {
    override fun measureMarker(
        textMeasurer: TextMeasurer,
        node: T,
        style: TextStyle,
    ): TextLayoutResult {
        val marker = node.getMarkerText()
        return textMeasurer.measure(marker, style)
    }

    override fun DrawScope.drawMarker(textLayoutResult: TextLayoutResult) {
        drawText(textLayoutResult, topLeft = Offset(0f, 0f))
    }
}

/**
 * The base renderer for ListItem nodes.
 * Uses a custom [Layout] with [drawBehind] to draw the marker on canvas,
 * avoiding the `\n` issue caused by [Row] with multiple [Text] composables.
 * @param T The type of ListItem.
 * @param markerRenderer The renderer for the list item marker.
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
        val spacerHeight =
            if (node.isLoose) theme.spacerTheme.spacerHeight else theme.listTheme.tightListSpacerHeight

        val isInQuoteBlock = node.isInQuoteBlock()
        val mergedTextStyle =
            (listTheme.markerTextStyle ?: theme.textStyle)
                .merge(
                    theme.blockQuoteTheme.textStyle.takeIf { isInQuoteBlock },
                )

        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val spacerWidthPx = with(density) { listTheme.markerSpacerWidth.toPx() }

        val markerTextLayoutResult =
            markerRenderer.measureMarker(
                textMeasurer = textMeasurer,
                node = node,
                style = mergedTextStyle,
            )
        val markerOffset = markerTextLayoutResult.size.width + spacerWidthPx.toInt()

        Layout(
            content = {
                if (node.parent?.firstChild != node && intentLevel > 0) {
                    // add invisible space to align with above item marker for selection-copy purpose
                    SelectionFormatText(FIGURE_SPACE.repeat(intentLevel))
                }
                MarkdownChildren(
                    parent = node,
                    modifier = Modifier.wrapContentSize(),
                    verticalArrangement = Arrangement.Top,
                    spacerHeight = spacerHeight,
                    onBeforeChild = { child, parent ->
                        if (child != parent.firstChild) {
                            // add invisible space to align with marker for selection-copy purpose
                            SelectionFormatText(FIGURE_SPACE.repeat(intentLevel + 1))
                        }
                    },
                )
            },
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .drawBehind {
                        with(markerRenderer) {
                            drawMarker(markerTextLayoutResult)
                        }
                    },
        ) { measurables, constraints ->
            val contentConstraints =
                constraints.copy(
                    maxWidth = (constraints.maxWidth - markerOffset).coerceAtLeast(0),
                    minWidth = 0,
                )
            val placeables = measurables.map { it.measure(contentConstraints) }
            val height = placeables.sumOf { it.height }

            layout(constraints.maxWidth, height) {
                var yOffset = 0
                placeables.forEach { placeable ->
                    placeable.place(markerOffset, yOffset)
                    yOffset += placeable.height
                }
            }
        }
    }
}

/**
 * The renderer for OrderedListItem nodes.
 * @see BaseListItemRenderer
 */
class OrderedListItemRenderer(
    markerRenderer: ListItemMarkerRenderer<OrderedListItem> = ListItemMarkerRendererImpl(),
) : BaseListItemRenderer<OrderedListItem>(markerRenderer = markerRenderer)

/**
 * The renderer for BulletListItem nodes.
 * @see BaseListItemRenderer
 */
class BulletListItemRenderer(
    markerRenderer: ListItemMarkerRenderer<BulletListItem> = ListItemMarkerRendererImpl(),
) : BaseListItemRenderer<BulletListItem>(markerRenderer = markerRenderer)
