package com.iffly.compose.markdown.task

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import com.iffly.compose.markdown.core.renders.BaseListItemRenderer
import com.iffly.compose.markdown.core.renders.ListItemMarkerRenderer
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem

/**
 * The default marker renderer for task list items.
 * Draws a checkbox marker (☑ or ☐) using [drawText].
 * @see ListItemMarkerRenderer
 */
class TaskListMarkerRenderer : ListItemMarkerRenderer<TaskListItem> {
    override fun measureMarker(
        textMeasurer: TextMeasurer,
        node: TaskListItem,
        style: TextStyle,
    ): TextLayoutResult {
        val marker = if (node.isItemDoneMarker) "☑" else "☐"
        return textMeasurer.measure(marker, style)
    }

    override fun DrawScope.drawMarker(textLayoutResult: TextLayoutResult) {
        drawText(textLayoutResult, topLeft = Offset(0f, 0f))
    }
}

/**
 * The renderer for task list items.
 * @see BaseListItemRenderer
 */
class TaskListItemRenderer(
    markerRenderer: TaskListMarkerRenderer = TaskListMarkerRenderer(),
) : BaseListItemRenderer<TaskListItem>(markerRenderer)
