package com.iffly.compose.markdown.render

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow

@Composable
fun MarkdownTable(
    tableBlock: TableBlock,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor)
    ) {
        var node = tableBlock.firstChild
        while (node != null) {
            when (node) {
                is TableHead -> {
                    TableHeadRow(node, borderColor)
                }
                is TableBody -> {
                    var bodyNode = node.firstChild
                    while (bodyNode != null) {
                        if (bodyNode is TableRow) {
                            TableDataRow(bodyNode, borderColor)
                        }
                        bodyNode = bodyNode.next
                    }
                }
            }
            node = node.next
        }
    }
}

@Composable
private fun TableHeadRow(
    tableHead: TableHead,
    borderColor: Color
) {
    var node = tableHead.firstChild
    while (node != null) {
        if (node is TableRow) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                var cellNode = node.firstChild
                while (cellNode != null) {
                    if (cellNode is TableCell) {
                        TableHeaderCell(
                            cell = cellNode,
                            borderColor = borderColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    cellNode = cellNode.next
                }
            }
        }
        node = node.next
    }
}

@Composable
private fun TableDataRow(
    tableRow: TableRow,
    borderColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        var cellNode = tableRow.firstChild
        while (cellNode != null) {
            if (cellNode is TableCell) {
                TableDataCell(
                    cell = cellNode,
                    borderColor = borderColor,
                    modifier = Modifier.weight(1f)
                )
            }
            cellNode = cellNode.next
        }
    }
}

@Composable
private fun TableHeaderCell(
    cell: TableCell,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = borderColor
            )
            .padding(8.dp)
    ) {
        MarkdownText(
            parent = cell,
            modifier = Modifier
        )
    }
}

@Composable
private fun TableDataCell(
    cell: TableCell,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = borderColor
            )
            .padding(8.dp)
    ) {
        MarkdownText(
            parent = cell,
            modifier = Modifier
        )
    }
}
