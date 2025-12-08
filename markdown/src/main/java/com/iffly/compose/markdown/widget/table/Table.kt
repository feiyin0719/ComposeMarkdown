/*
 * Copyright (c) 2025.
 * Microsoft Corporation. All rights reserved.
 */

package com.iffly.compose.markdown.widget.table

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.max
import kotlin.math.min

/**
 * A table component that uses SubComposeLayout
 * This is a custom implementation to provide a flexible table layout
 * with support for headers, rows, cell alignment, padding, borders, and shapes.
 * It supports both weight-based and content-based column widths.
 * - For weight-based widths, provide a list of weights for each column. And please ensure the table width is not infinite.
 * - For content-based widths, omit the weights and the table will size columns based on content.
 * For improve performance and requirement, the cells are measured in a single pass, so the content width will not scaled to fit the table width.
 * Usage example:
 * weight-based widths
 * ```kotlin
 * Table(
 *    modifier = Modifier.fillMaxWidth().padding(16.dp)
 *                     .clip(RoundedCornerShape(8.dp))
 *                     .border(1.dp, CopilotTheme.fluentColors.stroke.stroke2, RoundedCornerShape(8.dp)),
 *    cellPadding = PaddingValues(8.dp),
 *    cellAlignment = Alignment.CenterStart,
 *    border = TableBorder.solid(color = Color.Gray, width = 1.dp),
 *    widthWeights = listOf(1f, 2f, 1f),
 *   ) {
 *      header( modifier = Modifier.background(Color.LightGray) ) {
 *          cell { Text("Header 1", fontWeight = FontWeight.Bold) }
 *          cell { Text("Header 2", fontWeight = FontWeight.Bold) }
 *          cell { Text("Header 3", fontWeight = FontWeight.Bold) }
 *      }
 *      body {
 *          row {
 *              cell { Text("Row 1 Col 1") }
 *              cell { Text("Row 1 Col 2") }
 *              cell { Text("Row 1 Col 3") }
 *          }
 *          row( modifier = Modifier.background(Color(0xFFF0F0F0)){
 *              cell { Text("Row 2 Col 1") }
 *              cell { Text("Row 2 Col 2") }
 *              cell { Text("Row 2 Col 3") }
 *          }
 *      }
 *   }
 *   ```
 *  content-based widths
 *  ```kotlin
 * Table(
 *    modifier = Modifier.wrapContentSize().padding(16.dp)
 *                     .clip(RoundedCornerShape(8.dp))
 *                     .border(1.dp, CopilotTheme.fluentColors.stroke.stroke2, RoundedCornerShape(8.dp)),
 *    cellPadding = PaddingValues(8.dp),
 *    cellAlignment = Alignment.CenterStart,
 *    border = TableBorder.solid(color = Color.Gray, width = 1.dp),
 *   ) {
 *      header( modifier = Modifier.background(Color.LightGray) ) {
 *          cell { Text("Header 1", fontWeight = FontWeight.Bold) }
 *          cell { Text("Header 2", fontWeight = FontWeight.Bold) }
 *          cell { Text("Header 3", fontWeight = FontWeight.Bold) }
 *      }
 *      body {
 *          row {
 *              cell { Text("Row 1 Col 1") }
 *              cell { Text("Row 1 Col 2") }
 *              cell { Text("Row 1 Col 3") }
 *          }
 *          row( modifier = Modifier.background(Color(0xFFF0F0F0)){
 *              cell { Text("Row 2 Col 1") }
 *              cell { Text("Row 2 Col 2") }
 *              cell { Text("Row 2 Col 3") }
 *          }
 *      }
 *   }
 *  ```
 *
 * @param modifier Modifier to be applied to the table
 * @param cellPadding Padding to be applied to each cell
 * @param cellAlignment Default alignment for cells in the table
 * @param border Border style for the table
 * @param shape Optional shape to clip the table
 * @param widthWeights Optional list of weights for each column to determine their widths, if null or empty, content-based widths are used
 * When use the weight-based widths, the cell width will be exactly the calculated width,
 * it will ignore the cell content width config, and please set the `fillMaxSize` for cell content.
 * @param content Lambda to define the table structure using TableScope
 * @see TableScope
 * @see BodyScope
 * @see RowScope
 * @see TableBorder
 */
@Composable
fun Table(
    modifier: Modifier = Modifier,
    cellPadding: PaddingValues = PaddingValues(),
    cellAlignment: Alignment = Alignment.CenterStart,
    border: TableBorder = TableBorder.solid(mode = TableBorderMode.NONE),
    shape: Shape? = null,
    widthWeights: ImmutableList<Float>? = null,
    content: TableScope.() -> Unit,
) {
    val tableBuilder = TableScopeImpl(cellAlignment).apply(content)
    val density = LocalDensity.current
    val appliedModifier = if (shape != null) modifier.clip(shape) else modifier
    SubcomposeLayout(modifier = appliedModifier) { constraints ->
        measureAndLayoutTable(
            this,
            tableBuilder,
            cellPadding,
            density,
            constraints,
            border,
            widthWeights,
        )
    }
}

private fun measureAndLayoutTable(
    measureScope: SubcomposeMeasureScope,
    tableBuilder: TableScopeImpl,
    cellPadding: PaddingValues,
    density: Density,
    constraints: Constraints,
    border: TableBorder,
    widthWeights: List<Float>? = null,
) = with(measureScope) {
    val measureResult =
        measureTable(
            measureScope = this,
            tableBuilder = tableBuilder,
            cellPadding = cellPadding,
            density = density,
            constraints = constraints,
            border = border,
            widthWeights = widthWeights,
        )

    layoutTable(
        measureScope = this,
        measureResult = measureResult,
        border = border,
        density = density,
    )
}

/**
 * Data class to hold the results of table measurement
 */
private data class TableMeasureResult(
    val allRows: List<RowScopeImpl>,
    val columnCount: Int,
    val columnWidths: List<Int>,
    val rowHeights: List<Int>,
    val cellPlaceableMap: List<List<Placeable?>>,
    val cellData: List<List<CellImpl?>>,
    val totalWidth: Int,
    val totalHeight: Int,
    val borderWidth: Int = 0,
    val borderHeight: Int = 0,
)

/**
 * Measure all table components and calculate dimensions
 */
private fun measureTable(
    measureScope: SubcomposeMeasureScope,
    tableBuilder: TableScopeImpl,
    cellPadding: PaddingValues,
    density: Density,
    constraints: Constraints,
    border: TableBorder,
    widthWeights: List<Float>?,
): TableMeasureResult? {
    // Combine header and body rows into a single list
    val allRows = tableBuilder.rows()
    val columnCount = allRows.maxOfOrNull { it.cells.size } ?: 0

    if (columnCount == 0 || allRows.isEmpty()) {
        return null
    }

    // Calculate column widths based on whether weights are provided
    val columnWeightWidths =
        getColumnWeightWidths(widthWeights, columnCount, constraints, border, density)

    // Single pass: measure all cells with calculated column widths
    val columnWidths = MutableList(columnCount) { 0 }
    val rowHeights = MutableList(allRows.size) { 0 }
    val cellPlaceableMap = List(allRows.size) { MutableList<Placeable?>(columnCount) { null } }
    val cellData = List(allRows.size) { MutableList<CellImpl?>(columnCount) { null } }
    val borderWith = calculateBorderOffsetX(border, density)
    val borderHeight = calculateBorderOffsetY(border, density)
    // Measure all cells and update dimensions
    measureAllCells(
        measureScope = measureScope,
        allRows = allRows,
        columnCount = columnCount,
        columnWeightWidths = columnWeightWidths,
        cellPadding = cellPadding,
        constraints = constraints,
        columnWidths = columnWidths,
        rowHeights = rowHeights,
        cellPlaceableMap = cellPlaceableMap,
        cellData = cellData,
    )

    // Calculate layout dimensions
    val totalWidth = columnWidths.sum() + borderWith * (columnCount - 1)
    val totalHeight = rowHeights.sum() + borderHeight * (allRows.size - 1)
    // adjust size based on constraints
    val (adjustedTotalWidth, adjustedTotalHeight) =
        adjustedSize(
            constraints,
            totalWidth,
            totalHeight,
        )

    return TableMeasureResult(
        allRows = allRows,
        columnCount = columnCount,
        columnWidths = columnWidths,
        rowHeights = rowHeights,
        cellPlaceableMap = cellPlaceableMap,
        cellData = cellData,
        totalWidth = adjustedTotalWidth,
        totalHeight = adjustedTotalHeight,
        borderWidth = borderWith,
        borderHeight = borderHeight,
    )
}

private fun adjustedSize(
    constraints: Constraints,
    totalWidth: Int,
    totalHeight: Int,
): Pair<Int, Int> {
    val adjustedTotalWidth =
        if (constraints.hasBoundedWidth) {
            min(constraints.maxWidth, totalWidth)
        } else {
            totalWidth
        }
    val adjustedTotalHeight =
        if (constraints.hasBoundedHeight) {
            min(constraints.maxHeight, totalHeight)
        } else {
            totalHeight
        }
    return Pair(adjustedTotalWidth, adjustedTotalHeight)
}

private fun getColumnWeightWidths(
    widthWeights: List<Float>?,
    columnCount: Int,
    constraints: Constraints,
    border: TableBorder,
    density: Density,
): List<Int> {
    val columnWeightWidths =
        if (!widthWeights.isNullOrEmpty()) {
            // Weight mode: distribute available width according to weights
            calculateWeightBasedColumnWidths(
                widthWeights,
                columnCount,
                constraints,
                border,
                density,
            )
        } else {
            // Content mode: measure cells to determine widths
            List(columnCount) { -1 }
        }
    return columnWeightWidths
}

/**
 * Measure all cells in the table and update dimension arrays
 */
private fun measureAllCells(
    measureScope: SubcomposeMeasureScope,
    allRows: List<RowScopeImpl>,
    columnCount: Int,
    columnWeightWidths: List<Int>,
    cellPadding: PaddingValues,
    constraints: Constraints,
    columnWidths: MutableList<Int>,
    rowHeights: MutableList<Int>,
    cellPlaceableMap: List<MutableList<Placeable?>>,
    cellData: List<MutableList<CellImpl?>>,
) = allRows.fastForEachIndexed { rowIndex, row ->
    row.cells.fastForEachIndexed { columnIndex, cell ->
        if (columnIndex < columnCount) {
            measureSingleCell(
                measureScope = measureScope,
                rowIndex = rowIndex,
                columnIndex = columnIndex,
                cell = cell,
                columnWeightWidths = columnWeightWidths,
                cellPadding = cellPadding,
                constraints = constraints,
                columnWidths = columnWidths,
                rowHeights = rowHeights,
                cellPlaceableMap = cellPlaceableMap,
                cellData = cellData,
            )
        }
    }
}

/**
 * Measure a single cell and update the corresponding data structures
 */
private fun measureSingleCell(
    measureScope: SubcomposeMeasureScope,
    rowIndex: Int,
    columnIndex: Int,
    cell: CellImpl,
    columnWeightWidths: List<Int>,
    cellPadding: PaddingValues,
    constraints: Constraints,
    columnWidths: MutableList<Int>,
    rowHeights: MutableList<Int>,
    cellPlaceableMap: List<MutableList<Placeable?>>,
    cellData: List<MutableList<CellImpl?>>,
) = with(measureScope) {
    // Create constraints for this specific column width
    val cellConstraints =
        if (columnWeightWidths[columnIndex] != -1) {
            Constraints.fixedWidth(columnWeightWidths[columnIndex])
        } else {
            constraints
        }

    // Only apply padding and content-related modifiers, not background modifiers
    val placeable =
        subcompose("cell_${rowIndex}_$columnIndex") {
            CellBox(cellPadding, cell.modifier, contentAlignment = cell.alignment, cell.content)
        }[0].measure(cellConstraints)

    // Update data structures
    cellPlaceableMap[rowIndex][columnIndex] = placeable
    cellData[rowIndex][columnIndex] = cell
    rowHeights[rowIndex] = max(rowHeights[rowIndex], placeable.height)
    columnWidths[columnIndex] = max(columnWidths[columnIndex], placeable.width)
}

/**
 * Layout all measured components in their final positions
 */
private fun layoutTable(
    measureScope: SubcomposeMeasureScope,
    measureResult: TableMeasureResult?,
    border: TableBorder,
    density: Density,
) = with(measureScope) {
    if (measureResult == null) {
        return@with layout(0, 0) {}
    }

    val borderWith = measureResult.borderWidth
    val borderHeight = measureResult.borderHeight
    layout(measureResult.totalWidth, measureResult.totalHeight) {
        var yOffset = 0

        measureResult.allRows.fastForEachIndexed { rowIndex, row ->
            if (yOffset >= measureResult.totalHeight) {
                return@fastForEachIndexed
            }
            val rowHeight = measureResult.rowHeights[rowIndex]
            // Place row background if exists
            placeRowBackground(
                measureScope = measureScope,
                rowIndex = rowIndex,
                row = row,
                totalWidth = measureResult.totalWidth,
                rowHeight = rowHeight,
                yOffset = yOffset,
                density = density,
            )

            // Place cells with alignment calculation
            placeCellsInRow(
                measureScope = measureScope,
                measureResult = measureResult,
                rowIndex = rowIndex,
                yOffset = yOffset,
                borderWith = borderWith,
                density = density,
            )

            yOffset += rowHeight + borderHeight
        }

        // Draw borders if needed
        placeTableBorders(
            measureScope = measureScope,
            border = border,
            measureResult = measureResult,
            density = density,
        )
    }
}

/**
 * Place row background if it has a custom modifier
 */
private fun Placeable.PlacementScope.placeRowBackground(
    measureScope: SubcomposeMeasureScope,
    rowIndex: Int,
    row: RowScopeImpl,
    totalWidth: Int,
    rowHeight: Int,
    yOffset: Int,
    density: Density,
) {
    if (row.modifier != Modifier) {
        val rowBackground =
            measureScope
                .subcompose("bg_$rowIndex") {
                    Box(
                        modifier =
                            row.modifier.size(
                                width = totalWidth.toDp(density),
                                height = rowHeight.toDp(density),
                            ),
                    )
                }[0]
                .measure(Constraints.fixed(totalWidth, rowHeight))
        rowBackground.place(0, yOffset)
    }
}

/**
 * Place all cells in a row with proper alignment
 */
private fun Placeable.PlacementScope.placeCellsInRow(
    measureScope: SubcomposeMeasureScope,
    measureResult: TableMeasureResult,
    rowIndex: Int,
    yOffset: Int,
    borderWith: Int,
    density: Density,
) {
    var xOffset = 0
    for (columnIndex in 0 until measureResult.columnCount) {
        if (xOffset >= measureResult.totalWidth) {
            break
        }
        val placeable = measureResult.cellPlaceableMap[rowIndex][columnIndex]
        val cell = measureResult.cellData[rowIndex][columnIndex]

        if (placeable != null && cell != null) {
            val columnWidth = measureResult.columnWidths[columnIndex]
            val rowHeight = measureResult.rowHeights[rowIndex]

            // Place cell background first if cell has custom modifier
            placeCellBackground(
                measureScope = measureScope,
                rowIndex = rowIndex,
                columnIndex = columnIndex,
                cell = cell,
                cellWidth = columnWidth,
                cellHeight = rowHeight,
                xOffset = xOffset,
                yOffset = yOffset,
                density = density,
            )

            // Calculate position based on cell alignment
            val (cellX, cellY) =
                calculateCellPosition(
                    alignment = cell.alignment,
                    cellWidth = placeable.width,
                    cellHeight = placeable.height,
                    availableWidth = columnWidth,
                    availableHeight = rowHeight,
                    baseX = xOffset,
                    baseY = yOffset,
                )

            placeable.place(cellX, cellY)
        }

        xOffset += measureResult.columnWidths[columnIndex] + borderWith
    }
}

private const val BORDER_COMPOSE = "border"

/**
 * Place table borders if needed
 */
private fun Placeable.PlacementScope.placeTableBorders(
    measureScope: SubcomposeMeasureScope,
    border: TableBorder,
    measureResult: TableMeasureResult,
    density: Density,
) {
    if (border.mode != TableBorderMode.NONE) {
        val borderCanvas =
            measureScope
                .subcompose(BORDER_COMPOSE) {
                    TableBorderCanvas(
                        border = border,
                        columnWidths = measureResult.columnWidths.toImmutableList(),
                        rowHeights = measureResult.rowHeights.toImmutableList(),
                        density = density,
                    )
                }[0]
                .measure(Constraints.fixed(measureResult.totalWidth, measureResult.totalHeight))
        borderCanvas.place(0, 0)
    }
}

/**
 * Place cell background if it has a custom modifier
 */
private fun Placeable.PlacementScope.placeCellBackground(
    measureScope: SubcomposeMeasureScope,
    rowIndex: Int,
    columnIndex: Int,
    cell: CellImpl,
    cellWidth: Int,
    cellHeight: Int,
    xOffset: Int,
    yOffset: Int,
    density: Density,
) {
    if (cell.cellBackground != null && cell.cellBackground != Modifier) {
        val cellBackground =
            measureScope
                .subcompose("cell_bg_${rowIndex}_$columnIndex") {
                    Box(
                        modifier =
                            cell.cellBackground.size(
                                width = cellWidth.toDp(density),
                                height = cellHeight.toDp(density),
                            ),
                    )
                }[0]
                .measure(Constraints.fixed(cellWidth, cellHeight))
        cellBackground.place(xOffset, yOffset)
    }
}

@Composable
private fun CellBox(
    cellPadding: PaddingValues,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.CenterStart,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.padding(cellPadding),
        contentAlignment = contentAlignment,
    ) {
        content()
    }
}

/**
 * Calculate the position for a cell within its allocated space based on alignment
 */
private fun calculateCellPosition(
    alignment: Alignment,
    cellWidth: Int,
    cellHeight: Int,
    availableWidth: Int,
    availableHeight: Int,
    baseX: Int,
    baseY: Int,
): Pair<Int, Int> {
    // Calculate X position based on horizontal alignment
    val x: Int =
        when (alignment) {
            Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> baseX
            Alignment.TopCenter, Alignment.Center, Alignment.BottomCenter -> baseX + (availableWidth - cellWidth) / 2
            Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> baseX + availableWidth - cellWidth
            else -> baseX // Fallback to start if undefined
        }

    // Calculate Y position based on vertical alignment
    val y: Int =
        when (alignment) {
            Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> baseY
            Alignment.CenterStart, Alignment.Center, Alignment.CenterEnd -> baseY + (availableHeight - cellHeight) / 2
            Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd -> baseY + availableHeight - cellHeight
            else -> baseY // Fallback to top if undefined
        }

    return Pair(x, y)
}

/**
 * Calculate column widths based on weight distribution
 */
private fun calculateWeightBasedColumnWidths(
    widthWeights: List<Float>,
    columnCount: Int,
    constraints: Constraints,
    border: TableBorder,
    density: Density,
): List<Int> {
    // Process weights according to column count
    val processedWeights =
        when {
            widthWeights.size > columnCount -> {
                // If weights are more than columns, take only the first columnCount weights
                widthWeights.take(columnCount)
            }

            widthWeights.size < columnCount -> {
                // If weights are less than columns, pad with 1.0f for remaining columns
                widthWeights + List(columnCount - widthWeights.size) { 1.0f }
            }

            else -> {
                // If weights match column count exactly, use as is
                widthWeights
            }
        }

    // Calculate total weight
    val totalWeight = processedWeights.sum()

    // Calculate available width for content (subtracting border space)
    val availableWidth =
        constraints.maxWidth - calculateBorderOffsetX(border, density) * (columnCount - 1)

    // Calculate base width for each column
    val columnWidths = MutableList(columnCount) { 0 }

    // Special case: if total weight is zero, fallback to equal distribution
    if (totalWeight == 0f) {
        val equalWidth = availableWidth / columnCount
        columnWidths.fill(equalWidth)
    } else {
        // Calculate width for each column based on weight
        for (i in 0 until columnCount) {
            columnWidths[i] = (availableWidth * (processedWeights[i] / totalWeight)).toInt()
        }
    }

    return columnWidths
}

private fun Int.toDp(density: Density): Dp =
    with(density) {
        this@toDp.toDp()
    }

// Implementation classes
internal class TableScopeImpl(
    private val cellAlignment: Alignment = Alignment.CenterStart,
) : TableScope {
    var header: RowScopeImpl? = null
        private set
    var body: BodyScopeImpl? = null
        private set

    override fun header(
        modifier: Modifier,
        cellAlignment: Alignment?,
        content: RowScope.() -> Unit,
    ) {
        header =
            RowScopeImpl(
                modifier = modifier,
                rowDefaultAlignment = cellAlignment,
                tableDefaultAlignment = this@TableScopeImpl.cellAlignment,
            ).apply(content)
    }

    override fun body(content: BodyScope.() -> Unit) {
        body = BodyScopeImpl(cellAlignment).apply(content)
    }
}

private fun TableScopeImpl.rows(): List<RowScopeImpl> =
    buildList {
        header?.let { add(it) }
        body?.let { addAll(it.rows) }
    }

internal class BodyScopeImpl(
    private val tableDefaultAlignment: Alignment,
) : BodyScope {
    val rows = mutableListOf<RowScopeImpl>()

    override fun row(
        modifier: Modifier,
        cellAlignment: Alignment?,
        content: RowScope.() -> Unit,
    ) {
        rows.add(
            RowScopeImpl(
                modifier = modifier,
                rowDefaultAlignment = cellAlignment,
                tableDefaultAlignment = tableDefaultAlignment,
            ).apply(content),
        )
    }
}

internal class RowScopeImpl(
    val modifier: Modifier = Modifier,
    private val rowDefaultAlignment: Alignment? = null,
    private val tableDefaultAlignment: Alignment = Alignment.CenterStart,
) : RowScope {
    val cells = mutableListOf<CellImpl>()

    override fun cell(
        modifier: Modifier,
        cellBackground: Modifier,
        alignment: Alignment?,
        content: @Composable () -> Unit,
    ) {
        // Priority: cell alignment > row alignment > table alignment
        val finalAlignment = alignment ?: rowDefaultAlignment ?: tableDefaultAlignment
        cells.add(CellImpl(modifier, cellBackground, finalAlignment, content))
    }
}

internal class CellImpl(
    val modifier: Modifier = Modifier,
    val cellBackground: Modifier? = null,
    val alignment: Alignment = Alignment.CenterStart,
    val content: @Composable () -> Unit,
)
