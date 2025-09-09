package com.iffly.compose.markdown.render

import android.content.ClipData
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.config.currentHtmlRenderer
import com.iffly.compose.markdown.config.currentTypographyStyle
import com.iffly.compose.markdown.widget.table.BodyScope
import com.iffly.compose.markdown.widget.table.RowScope
import com.iffly.compose.markdown.widget.table.Table
import com.iffly.compose.markdown.widget.table.TableBorder
import com.iffly.compose.markdown.widget.table.TableBorderMode
import com.iffly.compose.markdown.widget.table.TableScope
import com.vladsch.flexmark.ext.tables.TableBlock
import com.vladsch.flexmark.ext.tables.TableBody
import com.vladsch.flexmark.ext.tables.TableCell
import com.vladsch.flexmark.ext.tables.TableHead
import com.vladsch.flexmark.ext.tables.TableRow

object TableRenderer : IBlockRenderer<TableBlock> {
    @Composable
    override fun Invoke(
        node: TableBlock,
        modifier: Modifier
    ) {
        MarkdownTable(tableBlock = node, modifier = modifier)
    }

}

@Composable
fun MarkdownTable(
    tableBlock: TableBlock,
    modifier: Modifier = Modifier
) {
    val cells = tableBlock.cells()
    val columnsCount = cells.firstOrNull()?.size ?: 0
    if (columnsCount == 0 || cells.isEmpty()) return
    val typographyStyle = currentTypographyStyle()
    val borderColor = typographyStyle.tableBorderColor
    val widthWeights = if (columnsCount <= 2) List(columnsCount) { 1f } else null
    val cellModifier = createCellModifier(columnsCount)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                borderColor,
                RoundedCornerShape(8.dp)
            )
    ) {

        TableTitle(tableBlock, cells, Modifier.fillMaxWidth())
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(borderColor)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .boxModifier(columnsCount, rememberScrollState())
        ) {
            Table(
                modifier = Modifier
                    .tableModifier(columnsCount),
                widthWeights = widthWeights,
                cellPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                border = TableBorder.solid(
                    mode = TableBorderMode.ALL, color = borderColor, width = 1.dp
                ),
                cellAlignment = Alignment.TopStart
            ) {
                Header(cells.first(), cellModifier, typographyStyle.tableHeaderBackgroundColor)
                val bodyCells = if (cells.size > 1) cells.subList(1, cells.size) else null
                bodyCells?.let {
                    Body(it, cellModifier, typographyStyle.tableRowHeaderBackgroundColor)
                }
            }
        }
    }
}


@Composable
private fun TableTitle(
    tableBlock: TableBlock,
    cells: List<List<TableCell>>,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val typographyStyle = currentTypographyStyle()
    val htmlRenderer = currentHtmlRenderer()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(typographyStyle.tableTitleBackgroundColor)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Copy button
        Text(
            text = "Copy table",
            style = typographyStyle.tableCopyStyle,
            modifier = Modifier
                .clickable {
                    val clipData =
                        ClipData.newHtmlText(
                            "",
                            buildTableText(cells),
                            htmlRenderer.render(tableBlock)
                        )
                    clipboardManager.setClip(clipData.toClipEntry())
                }
        )
    }
}

private fun createCellModifier(columnsCount: Int): Modifier = if (columnsCount <= 2) {
    Modifier.fillMaxSize()
} else {
    Modifier
        .fillMaxHeight()
        .wrapContentWidth()
        .widthIn(max = 167.dp)
}

private fun Modifier.boxModifier(columnsCount: Int, state: ScrollState): Modifier {
    return if (columnsCount <= 2) {
        this
    } else {
        this.horizontalScroll(state)
    }
}

private fun Modifier.tableModifier(columnsCount: Int): Modifier {
    return if (columnsCount <= 2) {
        this.fillMaxWidth()
    } else {
        this
    }
}

private fun TableScope.Header(
    headerCells: List<TableCell>,
    modifier: Modifier,
    backgroundColor: Color
) {
    header(modifier = Modifier.background(backgroundColor)) {
        Cells(headerCells, modifier)
    }
}

private fun TableScope.Body(
    rows: List<List<TableCell>>,
    modifier: Modifier,
    backgroundColor: Color
) {
    body {
        Rows(rows, modifier, backgroundColor)
    }
}

private fun BodyScope.Rows(
    cells: List<List<TableCell>>,
    modifier: Modifier,
    backgroundColor: Color
) {
    cells.forEach { rowCells ->
        row(Modifier.background(backgroundColor)) {
            Cells(rowCells, modifier)
        }
    }
}

private fun RowScope.Cells(nodes: List<TableCell>, modifier: Modifier) {
    nodes.forEach { node ->
        cell(alignment = node.alignment.toTableAlignment(), modifier = modifier) {
            MarkdownText(parent = node, modifier = Modifier)
        }
    }
}

private fun TableCell.Alignment?.toTableAlignment(): Alignment {
    return when (this) {
        TableCell.Alignment.CENTER -> Alignment.TopCenter
        TableCell.Alignment.RIGHT -> Alignment.TopEnd
        else -> Alignment.TopStart
    }
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

private fun buildTableText(cells: List<List<TableCell>>): String {
    val stringBuilder = StringBuilder()
    cells.forEach { rowCells ->
        val rowText = rowCells.joinToString("   ") { cell ->
            cell.childChars.toString().trim()
        }
        stringBuilder.append(rowText).append("\n")
    }
    return stringBuilder.toString().trim()
}
