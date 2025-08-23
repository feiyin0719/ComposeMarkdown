package com.iffly.compose.markdown.render

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.widget.table.Table
import com.iffly.compose.markdown.widget.table.TableBorder
import com.iffly.compose.markdown.widget.table.TableBorderMode
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.node.Node

@Composable
fun MarkdownTable(
    tableBlock: TableBlock,
    modifier: Modifier = Modifier
) {
    val cells = tableBlock.cells()
    val columnsCount = cells.firstOrNull()?.size ?: 0
    if (columnsCount == 0 || cells.isEmpty()) return
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val widthWeights = if (columnsCount <= 2) List(columnsCount) { 1f } else null
    val cellModifier = if (columnsCount <= 2) {
        Modifier.wrapContentSize()
    } else {
        Modifier
            .fillMaxHeight()
            .wrapContentWidth()
            .widthIn(max = 167.dp)
    }
    val tableModifier = if (columnsCount <= 2) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
    }
    val boxModifier = if (columnsCount <= 2) {
        Modifier
    } else {
        Modifier.horizontalScroll(rememberScrollState())
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(boxModifier)
    ) {
        Table(
            modifier = modifier
                .then(tableModifier)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
            widthWeights = widthWeights,
            cellPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            border = TableBorder.solid(
                mode = TableBorderMode.ALL, color = borderColor, width = 1.dp
            ),
            cellAlignment = Alignment.TopStart
        ) {
            cells.firstOrNull()?.let { headers ->
                header(modifier = Modifier.background(Color.LightGray)) {
                    headers.forEach {
                        cell(modifier = cellModifier) {
                            MarkdownText(
                                parent = it,
                            )
                        }
                    }
                }
            }

            val dataRows = if (cells.size > 1) cells.subList(1, cells.size) else null
            dataRows?.let {
                body {
                    it.forEach { row ->
                        row {
                            row.forEach { cell ->
                                cell(modifier = cellModifier) {
                                    MarkdownText(
                                        parent = cell,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

private fun TableBlock.cells(): List<List<Node>> {
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

private fun TableRow.cells(): List<Node> {
    var cell = this.firstChild
    return buildList {
        while (cell != null) {
            add(cell)
            cell = cell.next
        }
    }
}