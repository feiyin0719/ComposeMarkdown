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
import androidx.compose.runtime.Immutable
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
import com.iffly.compose.markdown.config.LocalNodeDataMap
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.MarkdownChildren
import com.iffly.compose.markdown.style.MarkerAlignment
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
 * Layout metrics of the first line of text inside a ListItem content area.
 * Used to vertically align the list marker with the first line of text.
 *
 * All coordinates are relative to the ListItem's content area origin.
 */
@Immutable
data class FirstLineMetrics(
    /** Top of the first line, in pixels. */
    val top: Float,
    /** Bottom of the first line, in pixels. */
    val bottom: Float,
    /** Baseline of the first line, in pixels. */
    val baseline: Float,
) {
    companion object {
        val Unspecified = FirstLineMetrics(0f, 0f, 0f)

        /**
         * Extract [FirstLineMetrics] from a [TextLayoutResult].
         * Returns [Unspecified] if the result has no lines.
         */
        fun fromTextLayoutResult(textLayoutResult: TextLayoutResult): FirstLineMetrics =
            if (textLayoutResult.lineCount > 0) {
                FirstLineMetrics(
                    top = textLayoutResult.getLineTop(0),
                    bottom = textLayoutResult.getLineBottom(0),
                    baseline = textLayoutResult.getLineBaseline(0),
                )
            } else {
                Unspecified
            }
    }

    /**
     * Compute the vertical offset for the marker based on the given [MarkerAlignment].
     * @param markerTextLayoutResult The layout result of the marker text.
     * @param alignment The desired alignment mode.
     * @return The y-offset to apply when drawing the marker.
     */
    fun computeMarkerYOffset(
        markerTextLayoutResult: TextLayoutResult,
        alignment: MarkerAlignment,
    ): Float {
        if (markerTextLayoutResult.lineCount == 0) return 0f
        return when (alignment) {
            MarkerAlignment.Top -> {
                top - markerTextLayoutResult.getLineTop(0)
            }

            MarkerAlignment.Bottom -> {
                bottom - markerTextLayoutResult.getLineBottom(0)
            }

            MarkerAlignment.Center -> {
                val contentCenter = (top + bottom) / 2f
                val markerCenter =
                    (markerTextLayoutResult.getLineTop(0) + markerTextLayoutResult.getLineBottom(0)) / 2f
                contentCenter - markerCenter
            }

            MarkerAlignment.Baseline -> {
                baseline - markerTextLayoutResult.getLineBaseline(0)
            }
        }
    }
}

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
     * @param firstLineMetrics The first line metrics of the ListItem content text,
     * used to vertically align the marker with the first line.
     * @param alignment The vertical alignment mode for the marker.
     */
    fun DrawScope.drawMarker(
        textLayoutResult: TextLayoutResult,
        firstLineMetrics: FirstLineMetrics,
        alignment: MarkerAlignment = MarkerAlignment.Baseline,
    )
}

/**
 * The default implementation of [ListItemMarkerRenderer].
 * Draws the bullet point or ordered number marker using [drawText],
 * aligned to the first line of text based on the given [MarkerAlignment].
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

    override fun DrawScope.drawMarker(
        textLayoutResult: TextLayoutResult,
        firstLineMetrics: FirstLineMetrics,
        alignment: MarkerAlignment,
    ) {
        val yOffset = firstLineMetrics.computeMarkerYOffset(textLayoutResult, alignment)
        drawText(textLayoutResult, topLeft = Offset(0f, yOffset))
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

        val nodeDataMap = LocalNodeDataMap.current

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
                        val metrics = nodeDataMap[node] as? FirstLineMetrics
                        val alignment = if (metrics != null) listTheme.markerAlignment else MarkerAlignment.Top
                        with(markerRenderer) {
                            drawMarker(markerTextLayoutResult, metrics ?: FirstLineMetrics.Unspecified, alignment)
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
