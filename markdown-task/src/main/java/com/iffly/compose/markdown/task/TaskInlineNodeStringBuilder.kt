package com.iffly.compose.markdown.task

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.InlineNodeStringBuilders
import com.iffly.compose.markdown.render.MarkdownInlineTextContent
import com.iffly.compose.markdown.render.buildMarkdownAnnotatedString
import com.iffly.compose.markdown.style.TypographyStyle
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem

class TaskInlineNodeStringBuilder(
    private val taskStyle: SpanStyle,
    private val taskParagraphStyle: ParagraphStyle? = null
) :
    IInlineNodeStringBuilder<TaskListItem> {

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: TaskListItem,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders,
    ) {
        withStyle(taskParagraphStyle ?: typographyStyle.bulletListParagraphStyle) {
            // Add indentation for nested lists
            repeat(indentLevel) {
                append("  ")
            }

            // Render the checkbox based on whether the task is done
            withStyle(taskStyle) {
                if (node.isItemDoneMarker) {
                    append("☑ ") // Checked checkbox
                } else {
                    append("☐ ") // Unchecked checkbox
                }
                buildMarkdownAnnotatedString(
                    node,
                    indentLevel,
                    inlineContentMap,
                    typographyStyle,
                    linkInteractionListener = linkInteractionListener,
                    inlineNodeStringBuilders = inlineNodeStringBuilders,
                    isShowNotSupported = isShowNotSupported
                )
            }
        }

    }
}