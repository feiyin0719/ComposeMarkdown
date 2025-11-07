package com.iffly.compose.markdown.task

import com.iffly.compose.markdown.config.AbstractMarkdownRenderPlugin
import com.iffly.compose.markdown.render.IBlockRenderer
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.misc.Extension

class TaskMarkdownRenderPlugin() : AbstractMarkdownRenderPlugin() {
    override fun extensions(): List<Extension> {
        return listOf(TaskListExtension.create())
    }

    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<*>> {
        return mapOf(
            TaskListItem::class.java to TaskListItemRenderer()
        )
    }

}