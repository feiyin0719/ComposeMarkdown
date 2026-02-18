/*
 * Copyright (c) 2025.
 * Microsoft Corporation. All rights reserved.
 */

package com.iffly.compose.markdown.table.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

/**
 * Border drawing modes for table
 */
enum class TableBorderMode {
    NONE,
    HORIZONTAL,
    VERTICAL,
    ALL,
}

/**
 * Table border configuration
 */
data class TableBorder(
    val mode: TableBorderMode,
    val brush: Brush,
    val width: Dp,
) {
    companion object {
        // Default solid color border constructor
        fun solid(
            mode: TableBorderMode = TableBorderMode.NONE,
            color: Color = Color.Gray,
            width: Dp = 1.dp,
        ) = TableBorder(mode, SolidColor(color), width)

        // Brush-based border constructor
        fun brush(
            mode: TableBorderMode = TableBorderMode.NONE,
            brush: Brush,
            width: Dp = 1.dp,
        ) = TableBorder(mode, brush, width)
    }
}

/**
 * Draws the table borders on a Canvas
 */
@Composable
internal fun TableBorderCanvas(
    border: TableBorder,
    columnWidths: ImmutableList<Int>,
    rowHeights: ImmutableList<Int>,
    density: Density,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        when (border.mode) {
            TableBorderMode.NONE -> {
                // Do nothing
            }

            TableBorderMode.HORIZONTAL -> {
                drawHorizontalBorders(border, rowHeights, density, size.width)
            }

            TableBorderMode.VERTICAL -> {
                drawVerticalBorders(border, columnWidths, density, size.height)
            }

            TableBorderMode.ALL -> {
                drawAllBorders(
                    border,
                    columnWidths,
                    rowHeights,
                    density,
                    size.width,
                    size.height,
                )
            }
        }
    }
}

/**
 * Draw horizontal borders only (internal borders only, no outer borders)
 */
private fun DrawScope.drawHorizontalBorders(
    border: TableBorder,
    rowHeights: List<Int>,
    density: Density,
    totalWidth: Float,
) {
    val strokeWidth = with(density) { border.width.toPx() }
    var yOffset = 0f

    // Only draw borders between rows, skip top and bottom borders
    for (i in 0 until rowHeights.size - 1) {
        yOffset += rowHeights[i] + strokeWidth / 2
        drawLine(
            brush = border.brush,
            start = Offset(0f, yOffset),
            end = Offset(totalWidth, yOffset),
            strokeWidth = strokeWidth,
        )
    }
}

/**
 * Draw vertical borders only (internal borders only, no outer borders)
 */
private fun DrawScope.drawVerticalBorders(
    border: TableBorder,
    columnWidths: List<Int>,
    density: Density,
    totalHeight: Float,
) {
    val strokeWidth = with(density) { border.width.toPx() }
    var xOffset = 0f

    // Only draw borders between columns, skip left and right borders
    for (i in 0 until columnWidths.size - 1) {
        xOffset += columnWidths[i] + strokeWidth / 2
        drawLine(
            brush = border.brush,
            start = Offset(xOffset, 0f),
            end = Offset(xOffset, totalHeight),
            strokeWidth = strokeWidth,
        )
    }
}

/**
 * Draw all borders (horizontal and vertical)
 */
private fun DrawScope.drawAllBorders(
    border: TableBorder,
    columnWidths: List<Int>,
    rowHeights: List<Int>,
    density: Density,
    totalWidth: Float,
    totalHeight: Float,
) {
    drawHorizontalBorders(border, rowHeights, density, totalWidth)
    drawVerticalBorders(border, columnWidths, density, totalHeight)
}

/**
 * Calculate the X offset for border drawing based on the column index
 */
internal fun calculateBorderOffsetX(
    border: TableBorder,
    density: Density,
): Int =
    when (border.mode) {
        TableBorderMode.NONE -> {
            0
        }

        TableBorderMode.HORIZONTAL -> {
            0
        }

        TableBorderMode.VERTICAL -> {
            with(density) {
                border.width.toPx().toInt()
            }
        }

        TableBorderMode.ALL -> {
            with(density) {
                border.width.toPx().toInt()
            }
        }
    }

/**
 * Calculate the Y offset for border drawing based on the row index
 */
internal fun calculateBorderOffsetY(
    border: TableBorder,
    density: Density,
): Int =
    when (border.mode) {
        TableBorderMode.NONE -> {
            0
        }

        TableBorderMode.VERTICAL -> {
            0
        }

        TableBorderMode.HORIZONTAL -> {
            with(density) {
                border.width.toPx().toInt()
            }
        }

        TableBorderMode.ALL -> {
            with(density) {
                border.width.toPx().toInt()
            }
        }
    }
