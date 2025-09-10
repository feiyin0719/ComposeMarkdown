package com.iffly.compose.markdown.task

import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.config.AbstractMarkdownRenderPlugin
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.misc.Extension

class TaskMarkdownRenderPlugin(
    private val taskStyle: SpanStyle = SpanStyle()
) : AbstractMarkdownRenderPlugin() {
    override fun extensions(): List<Extension> {
        return listOf(TaskListExtension.create())
    }

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<out Node>> =
        mapOf(
            TaskListItem::class.java to
                    TaskInlineNodeStringBuilder(taskStyle)
        )

}