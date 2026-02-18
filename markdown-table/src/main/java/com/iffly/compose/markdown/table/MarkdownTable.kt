package com.iffly.compose.markdown.table

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.config.currentActionHandler
import com.iffly.compose.markdown.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.MarkdownText
import com.iffly.compose.markdown.table.widget.BodyScope
import com.iffly.compose.markdown.table.widget.RowScope
import com.iffly.compose.markdown.table.widget.Table
import com.iffly.compose.markdown.table.widget.TableBorder
import com.iffly.compose.markdown.table.widget.TableBorderMode
import com.iffly.compose.markdown.table.widget.TableScope
import com.iffly.compose.markdown.widget.DisableSelectionWrapper
import com.vladsch.flexmark.ext.tables.TableBlock
import com.vladsch.flexmark.ext.tables.TableBody
import com.vladsch.flexmark.ext.tables.TableCell
import com.vladsch.flexmark.ext.tables.TableHead
import com.vladsch.flexmark.ext.tables.TableRow
import com.vladsch.flexmark.util.ast.Node
import kotlinx.collections.immutable.toImmutableList

/**
 * Renderer table sub view. It is used to custom table render.
 * @param T The type of the node, must be a subclass of [Node].
 */
fun interface TableWidgetRenderer<T : Node> {
    /**
     * Composable function to render the given node.
     * @param node The node to render.
     * @param modifier Modifier to be applied to the rendered content.
     */
    @Suppress("ComposableNaming")
    @Composable
    operator fun invoke(
        node: T,
        modifier: Modifier,
    )
}

/**
 * Renderer for table title.
 * @param T The type of the node, must be a subclass of [TableBlock].
 * @see TableWidgetRenderer
 */
class TableTitleRenderer(
    private val tableTheme: TableTheme = TableTheme(),
) : TableWidgetRenderer<TableBlock> {
    @Suppress("ComposableNaming")
    @Composable
    override fun invoke(
        node: TableBlock,
        modifier: Modifier,
    ) {
        TableTitle(tableBlock = node, modifier = modifier, tableTheme = tableTheme)
    }
}

/**
 * Renderer for table cell.
 * @see TableWidgetRenderer
 */
class TableCellRenderer(
    private val tableTheme: TableTheme = TableTheme(),
) : TableWidgetRenderer<TableCell> {
    @Suppress("ComposableNaming")
    @Composable
    override fun invoke(
        node: TableCell,
        modifier: Modifier,
    ) {
        val isHeader = node.parent is TableRow && node.parent?.parent is TableHead
        SelectionContainer {
            MarkdownText(
                parent = node,
                modifier = Modifier,
                textAlign = node.alignment.toTextAlign(),
                textStyle = if (isHeader) tableTheme.headerTextStyle else tableTheme.cellTextStyle,
            )
        }
    }
}

/**
 * Renderer for TableBlock nodes.
 * @param tableTitleRenderer The renderer for the table title.
 * @param tableCellRenderer The renderer for the table cells.
 * @see TableWidgetRenderer
 * @see IBlockRenderer
 */
class TableRenderer(
    private val tableTheme: TableTheme = TableTheme(),
    tableTitleRenderer: TableWidgetRenderer<TableBlock>? = null,
    tableCellRenderer: TableWidgetRenderer<TableCell>? = null,
) : IBlockRenderer<TableBlock> {
    private val tableTitleRenderer: TableWidgetRenderer<TableBlock> =
        tableTitleRenderer ?: TableTitleRenderer(tableTheme)
    private val tableCellRenderer: TableWidgetRenderer<TableCell> =
        tableCellRenderer ?: TableCellRenderer(tableTheme)

    @Composable
    override fun Invoke(
        node: TableBlock,
        modifier: Modifier,
    ) {
        MarkdownTable(
            tableBlock = node,
            modifier = modifier,
            tableTitleRenderer = tableTitleRenderer,
            tableCellRenderer = tableCellRenderer,
            tableTheme = tableTheme,
        )
    }
}

/**
 * String builder for TableCell nodes.
 * @see CompositeChildNodeStringBuilder
 */
class TableCellNodeStringBuilder : CompositeChildNodeStringBuilder<Node>()

/**
 * Composable function to render a markdown table.
 * @param tableBlock The TableBlock node to render.
 * @param tableTitleRenderer The renderer for the table title.
 * @param tableCellRenderer The renderer for the table cells.
 * @param modifier Modifier to be applied to the rendered content.
 *  * @param tableTheme The theme to be applied to the table.
 */
@Composable
fun MarkdownTable(
    tableBlock: TableBlock,
    tableTitleRenderer: TableWidgetRenderer<TableBlock>,
    tableCellRenderer: TableWidgetRenderer<TableCell>,
    modifier: Modifier = Modifier,
    tableTheme: TableTheme = TableTheme(),
) {
    val cells = tableBlock.cells()
    val columnsCount = cells.firstOrNull()?.size ?: 0
    if (columnsCount == 0 || cells.isEmpty()) return
    val borderColor = tableTheme.borderColor
    val widthWeights = if (columnsCount <= 2) List(columnsCount) { 1f } else null

    Column(
        modifier =
            modifier
                .wrapContentSize()
                .clip(tableTheme.shape)
                .border(
                    tableTheme.borderThickness,
                    borderColor,
                    tableTheme.shape,
                ),
    ) {
        tableTitleRenderer(tableBlock, Modifier.fillMaxWidth())
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(tableTheme.borderThickness)
                .background(borderColor),
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cellMinWidth = this.minWidth / 3 - 1.dp
            val cellModifier = Modifier.cellModifier(columnsCount, cellMinWidth)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .boxModifier(columnsCount, rememberScrollState()),
            ) {
                Table(
                    modifier = Modifier.tableModifier(columnsCount),
                    widthWeights = widthWeights?.toImmutableList(),
                    cellPadding = tableTheme.cellPadding,
                    border =
                        TableBorder.solid(
                            mode = TableBorderMode.ALL,
                            color = borderColor,
                            width = tableTheme.borderThickness,
                        ),
                    cellAlignment = Alignment.TopStart,
                ) {
                    tableHeader(
                        headerCells = cells.first(),
                        modifier = cellModifier,
                        backgroundColor = tableTheme.tableHeaderBackgroundColor,
                        cellContent = tableCellRenderer,
                    )
                    val bodyCells = if (cells.size > 1) cells.subList(1, cells.size) else null
                    bodyCells?.let {
                        tableBody(
                            rows = it,
                            modifier = cellModifier,
                            backgroundColor = tableTheme.tableCellBackgroundColor,
                            cellContent = tableCellRenderer,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable function to render the table title with a copy button.
 * @param tableBlock The TableBlock node representing the table.
 * @param modifier Modifier to be applied to the rendered content.
 */
@Composable
private fun TableTitle(
    tableBlock: TableBlock,
    modifier: Modifier = Modifier,
    tableTheme: TableTheme = TableTheme(),
) {
    val actionHandler = currentActionHandler()
    DisableSelectionWrapper(disabled = true) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(tableTheme.titleBackgroundColor)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Copy button
            Text(
                text = "Copy table",
                style = tableTheme.copyTextStyle,
                modifier =
                    Modifier.clickable {
                        actionHandler?.handleCopyClick(tableBlock)
                    },
            )
        }
    }
}

private fun Modifier.cellModifier(
    columnsCount: Int,
    minWidth: Dp,
): Modifier =
    if (columnsCount <= 2) {
        fillMaxSize()
    } else {
        fillMaxHeight()
            .wrapContentWidth()
            .widthIn(max = 167.dp, min = minWidth)
    }

private fun Modifier.boxModifier(
    columnsCount: Int,
    state: ScrollState,
): Modifier =
    if (columnsCount <= 2) {
        this
    } else {
        this.horizontalScroll(state)
    }

private fun Modifier.tableModifier(columnsCount: Int): Modifier =
    if (columnsCount <= 2) {
        this.fillMaxWidth()
    } else {
        this
    }

private fun TableScope.tableHeader(
    headerCells: List<TableCell>,
    modifier: Modifier,
    backgroundColor: Color,
    cellContent: TableWidgetRenderer<TableCell>,
) {
    header(modifier = Modifier.background(backgroundColor)) {
        tableCell(
            nodes = headerCells,
            modifier = modifier,
            cellContent = cellContent,
        )
    }
}

private fun TableScope.tableBody(
    rows: List<List<TableCell>>,
    modifier: Modifier,
    backgroundColor: Color,
    cellContent: TableWidgetRenderer<TableCell>,
) {
    body {
        tableRow(
            cells = rows,
            modifier = modifier,
            backgroundColor = backgroundColor,
            cellContent = cellContent,
        )
    }
}

private fun BodyScope.tableRow(
    cells: List<List<TableCell>>,
    modifier: Modifier,
    backgroundColor: Color,
    cellContent: TableWidgetRenderer<TableCell>,
) {
    cells.forEach { rowCells ->
        row(Modifier.background(backgroundColor)) {
            tableCell(
                nodes = rowCells,
                modifier = modifier,
                cellContent = cellContent,
            )
        }
    }
}

private fun RowScope.tableCell(
    nodes: List<TableCell>,
    modifier: Modifier,
    cellContent: TableWidgetRenderer<TableCell>,
) {
    nodes.forEach { node ->
        cell(alignment = node.alignment.toTableAlignment(), modifier = modifier) {
            cellContent(node, Modifier)
        }
    }
}

private fun TableCell.Alignment?.toTextAlign(): TextAlign =
    when (this) {
        TableCell.Alignment.CENTER -> TextAlign.Center
        TableCell.Alignment.RIGHT -> TextAlign.End
        else -> TextAlign.Start
    }

private fun TableCell.Alignment?.toTableAlignment(): Alignment =
    when (this) {
        TableCell.Alignment.CENTER -> Alignment.TopCenter
        TableCell.Alignment.RIGHT -> Alignment.TopEnd
        else -> Alignment.TopStart
    }

private fun TableBlock.cells(): List<List<TableCell>> {
    var content = this.firstChild
    return buildList {
        while (content != null) {
            when (content) {
                is TableHead, is TableBody -> {
                    var row = content.firstChild
                    while (row != null) {
                        if (row is TableRow) {
                            add(row.cells())
                        }
                        row = row.next
                    }
                }
            }
            content = content.next
        }
    }
}

private fun TableRow.cells(): List<TableCell> {
    var cell = this.firstChild
    return buildList {
        while (cell != null) {
            if (cell is TableCell) {
                add(cell)
            }
            cell = cell.next
        }
    }
}
