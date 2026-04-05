package com.iffly.compose.markdown.render

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlatformParagraphStyle
import androidx.compose.ui.text.PlatformSpanStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.style.MarkdownTheme
import com.iffly.compose.markdown.util.StringExt
import com.iffly.compose.markdown.widget.richtext.RichTextInlineContent
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node

/**
 * An [IInlineNodeStringBuilder] that wraps an [IBlockRenderer] as inline content.
 *
 * When a block node (e.g. code block, blockquote, list) is encountered during
 * [markdownText] string building, this builder inserts an
 * [EmbeddedRichTextInlineContent][RichTextInlineContent.EmbeddedRichTextInlineContent]
 * with [adjustSizeByContent] set to `true`, and delegates the actual rendering to the
 * wrapped [blockRenderer].
 *
 * This enables the Text-based rendering path ([com.iffly.compose.markdown.MarkdownText])
 * to handle block nodes through the same [markdownText] pipeline without special-casing.
 *
 * @param T The type of block node this builder handles.
 * @param blockRenderer The block renderer to delegate rendering to.
 *
 * @see IBlockRenderer
 * @see IInlineNodeStringBuilder
 */
class BlockRendererInlineStringBuilder<T : Block>(
    private val blockRenderer: IBlockRenderer<T>,
) : IInlineNodeStringBuilder<T> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: T,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val blockId = "block_${System.identityHashCode(node)}"
        inlineContentMap[blockId] =
            MarkdownInlineView.MarkdownRichTextInlineContent(
                RichTextInlineContent.EmbeddedRichTextInlineContent(
                    placeholder =
                        Placeholder(
                            width = 1.sp,
                            height = 1.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                        ),
                    adjustSizeByContent = true,
                ) {
                    blockRenderer.Invoke(node, Modifier.fillMaxWidth())
                },
            )
        appendInlineContent(blockId, REPLACEMENT_CHAR)
    }

    private companion object {
        const val REPLACEMENT_CHAR = "\uFFFD"
    }
}

/**
 * An [IInlineNodeStringBuilder] for [Document] nodes that iterates the document's
 * children and delegates each to the registered inline string builder.
 *
 * Text blocks (those with an [IInlineNodeStringBuilder], like Paragraph and Heading) are
 * merged directly into the [AnnotatedString]. Other blocks are handled by
 * [BlockRendererInlineStringBuilder] which inserts standalone inline content placeholders.
 *
 * Newline characters are inserted between children to maintain paragraph separation.
 *
 * @see BlockRendererInlineStringBuilder
 */
class DocumentInlineStringBuilder : IInlineNodeStringBuilder<Document> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Document,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        var child: Node? = node.firstChild
        while (child != null) {
            buildChildInlineNodeString(
                child = child,
                inlineContentMap = inlineContentMap,
                markdownTheme = markdownTheme,
                actionHandler = actionHandler,
                indentLevel = indentLevel,
                isShowNotSupported = isShowNotSupported,
                renderRegistry = renderRegistry,
                nodeStringBuilderContext = nodeStringBuilderContext,
            )
            if (child.next != null) {
                appendSpacer(
                    markdownTheme = markdownTheme,
                    nodeStringBuilderContext = nodeStringBuilderContext,
                )
            }
            child = child.next
        }
    }
}

private fun AnnotatedString.Builder.appendSpacer(
    markdownTheme: MarkdownTheme,
    nodeStringBuilderContext: NodeStringBuilderContext,
) {
    val spacerHeightSp =
        with(nodeStringBuilderContext.layoutContext.density) {
            markdownTheme.spacerTheme.spacerHeight
                .toSp()
                .times(0.9)
        }
    withStyle(
        ParagraphStyle(
            lineHeight = spacerHeightSp,
            lineHeightStyle =
                LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            platformStyle =
                PlatformParagraphStyle(
                    includeFontPadding = false,
                ),
        ),
    ) {
        withStyle(
            SpanStyle(
                fontSize = spacerHeightSp,
                platformStyle = PlatformSpanStyle(),
                letterSpacing = 0.sp,
                baselineShift = BaselineShift(0f),
            ),
        ) {
            append(StringExt.FIGURE_SPACE)
        }
    }
}

private fun AnnotatedString.Builder.buildChildInlineNodeString(
    child: Node,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    markdownTheme: MarkdownTheme,
    actionHandler: ActionHandler?,
    indentLevel: Int,
    isShowNotSupported: Boolean,
    renderRegistry: RenderRegistry,
    nodeStringBuilderContext: NodeStringBuilderContext,
) {
    val builder = renderRegistry.getInlineNodeStringBuilder(child::class.java)
    if (builder != null) {
        builder.buildMarkdownInlineNodeString(
            child,
            inlineContentMap,
            markdownTheme,
            indentLevel,
            actionHandler,
            renderRegistry,
            isShowNotSupported,
            this,
            nodeStringBuilderContext,
        )
    } else {
        if (isShowNotSupported) {
            append("[Unsupported: ${child::class.java.simpleName}]")
        } else {
            append(child.chars.toString())
        }
    }
}
