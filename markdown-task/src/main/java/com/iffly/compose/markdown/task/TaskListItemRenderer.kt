package com.iffly.compose.markdown.task

import com.iffly.compose.markdown.core.renders.ListItemRenderer
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem

class TaskListItemRenderer : ListItemRenderer<TaskListItem>() {
    override fun getMarker(node: TaskListItem): String {
        return if (node.isItemDoneMarker) "☑" else "☐"
    }
}