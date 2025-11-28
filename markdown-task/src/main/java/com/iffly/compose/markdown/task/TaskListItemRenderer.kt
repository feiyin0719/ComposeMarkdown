package com.iffly.compose.markdown.task

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.core.renders.BaseListItemRenderer
import com.iffly.compose.markdown.core.renders.ListItemMarkerRenderer
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem

class TaskListMarkerRenderer : ListItemMarkerRenderer<TaskListItem> {
    @Composable
    override fun invoke(
        node: TaskListItem,
        modifier: Modifier,
    ) {
        BasicText(
            text = if (node.isItemDoneMarker) "☑" else "☐",
            modifier = modifier,
        )
    }
}

class TaskListItemRenderer(
    markerRenderer: TaskListMarkerRenderer = TaskListMarkerRenderer(),
) : BaseListItemRenderer<TaskListItem>(markerRenderer)
