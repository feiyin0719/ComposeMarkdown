package com.iffly.compose.markdown.core.renders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.config.currentActionHandler
import com.iffly.compose.markdown.config.currentTheme
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.widget.DisableSelectionWrapper
import com.iffly.compose.markdown.widget.LineNumberText
import com.iffly.compose.markdown.widget.SelectionFormatText
import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.IndentedCodeBlock
import com.vladsch.flexmark.util.ast.Block

/**
 * A widget for code blocks, including fenced and indented code blocks.
 * You can override these methods to customize the rendering of code blocks.
 * @param T The type of the code block.
 */
fun interface CodeWidgetRenderer<T : Block> {
    @Composable
    operator fun invoke(block: T, modifier: Modifier)
}

/**
 * Default implementation of the copy button renderer for code blocks.
 * You can override this to provide a custom copy button.
 * @param T The type of the code block.
 * @see CodeWidgetRenderer
 */
class CopyRenderer<T : Block> : CodeWidgetRenderer<T> {
    @Composable
    override fun invoke(block: T, modifier: Modifier) {
        val actionHandler = currentActionHandler()
        val codeBlockTheme = currentTheme().codeBlockTheme
        Text(
            "Copy",
            style = codeBlockTheme.codeCopyTextStyle,
            modifier = modifier.clickable {
                actionHandler?.handleCopyClick(block)
            },
        )
    }
}

/**
 * Default implementation of the code block header renderer.
 * Displays the language info and a copy button.
 * You can override this to provide a custom header.
 * @param T The type of the code block.
 * @param renderCopy The renderer for the copy button.
 * @param disableSelection Whether to disable text selection in the header.
 * @see CodeWidgetRenderer
 */
class CodeHeaderRenderer<T : Block>(
    private val renderCopy: CodeWidgetRenderer<T>,
    private val disableSelection: Boolean = true,
) : CodeWidgetRenderer<T> {
    @Composable
    override fun invoke(block: T, modifier: Modifier) {
        if (block !is FencedCodeBlock && block !is IndentedCodeBlock) {
            return
        }
        val language = if (block is FencedCodeBlock) {
            block.info.toString()
        } else {
            "Text"
        }
        val codeBlockTheme = currentTheme().codeBlockTheme
        DisableSelectionWrapper(disabled = disableSelection) {
            Column(modifier = modifier) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .then(codeBlockTheme.headerModifier),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = language,
                        style = codeBlockTheme.codeTitleTextStyle,
                        modifier = Modifier.wrapContentSize(),
                    )
                    if (codeBlockTheme.showCopyButton) {
                        renderCopy(block = block, modifier = Modifier.wrapContentSize())
                    }
                }
                // divider
                HorizontalDivider(
                    color = codeBlockTheme.borderColor,
                    thickness = codeBlockTheme.borderWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 9.dp),
                )
                // add invisible \n in header for selection-copy purpose
                SelectionFormatText("\n")
            }
        }
    }
}

/**
 * Default implementation of the code content renderer for code blocks.
 * Displays the code content with line numbers.
 * You can override this to provide a custom content renderer.
 * @param T The type of the code block.
 * @see CodeWidgetRenderer
 */
class CodeContentRenderer<T : Block> : CodeWidgetRenderer<T> {
    @Composable
    override fun invoke(block: T, modifier: Modifier) {
        if (block !is FencedCodeBlock && block !is IndentedCodeBlock) {
            return
        }
        val contentTheme = currentTheme().codeBlockTheme.contentTheme
        val codeText = when (block) {
            is FencedCodeBlock -> block.contentChars.toString()
            is IndentedCodeBlock -> block.contentChars.toString()
            else -> return
        }
        val scrollModifier = if (contentTheme.height != null) {
            val scrollState = rememberScrollState()
            Modifier
                .height(contentTheme.height)
                .verticalScroll(scrollState)
        } else {
            Modifier
        }
        LineNumberText(
            text = codeText,
            textStyle = contentTheme.codeTextStyle,
            lineNumberStyle = contentTheme.lineNumberTextStyle,
            contentPadding = contentTheme.contentPadding,
            lineNumberPadding = contentTheme.lineNumberPadding,
            showLineNumber = contentTheme.showLineNumber,
            softWrap = contentTheme.softWrap,
            maxLines = contentTheme.maxLines,
            minLines = contentTheme.minLines,
            modifier = modifier
                .then(contentTheme.modifier)
                .then(scrollModifier),
        )
        // add invisible \n in code content for selection-copy purpose
        SelectionFormatText("\n")
    }
}

/**
 * Base implementation of [CodeRenderer] that provides default rendering for code blocks.
 * The implementation is column includes a header with language info and a copy button,
 * as well as the code content with line numbers.
 * You can override specific parts by providing custom renderers for copy, content, and header.
 * @param T The type of the code block.
 * @param renderCopyOverride Optional custom renderer for the copy button.
 * @param renderContentOverride Optional custom renderer for the code content.
 * @param renderHeaderOverride Optional custom renderer for the code block header.
 * @see IBlockRenderer
 * @see CodeWidgetRenderer
 */
class CodeRenderer<T : Block>(
    renderCopyOverride: CodeWidgetRenderer<T>? = null,
    renderContentOverride: CodeWidgetRenderer<T>? = null,
    renderHeaderOverride: CodeWidgetRenderer<T>? = null,
) : IBlockRenderer<T> {
    private val renderCopy: CodeWidgetRenderer<T> = renderCopyOverride ?: CopyRenderer()
    private val renderContent: CodeWidgetRenderer<T> =
        renderContentOverride ?: CodeContentRenderer()

    private val renderHeader: CodeWidgetRenderer<T> =
        renderHeaderOverride ?: CodeHeaderRenderer(renderCopy = renderCopy)

    @Composable
    override fun Invoke(block: T, modifier: Modifier) {
        val codeBlockTheme = currentTheme().codeBlockTheme
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(codeBlockTheme.backgroundColor, shape = codeBlockTheme.shape)
                .border(
                    width = codeBlockTheme.borderWidth,
                    color = codeBlockTheme.borderColor,
                    shape = codeBlockTheme.shape,
                )
                .then(
                    codeBlockTheme.blockModifier,
                ),
        ) {
            if (codeBlockTheme.showHeader) {
                renderHeader(
                    block = block,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                )
            }
            renderContent(
                block = block,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            )
        }
    }
}

/**
 * Renderer for fenced code blocks.
 * @param renderCopyOverride Optional custom renderer for the copy button.
 * @param renderContentOverride Optional custom renderer for the code content.
 * @param renderHeaderOverride Optional custom renderer for the code block header.
 * @see IBlockRenderer
 * @see CodeRenderer
 * @see CodeWidgetRenderer
 */
class FencedCodeBlockRenderer(
    renderCopyOverride: CodeWidgetRenderer<FencedCodeBlock>? = null,
    renderContentOverride: CodeWidgetRenderer<FencedCodeBlock>? = null,
    renderHeaderOverride: CodeWidgetRenderer<FencedCodeBlock>? = null,
) : IBlockRenderer<FencedCodeBlock> by CodeRenderer(
    renderCopyOverride,
    renderContentOverride,
    renderHeaderOverride,
)

/**
 * Renderer for indented code blocks.
 * @param renderCopyOverride Optional custom renderer for the copy button.
 * @param renderContentOverride Optional custom renderer for the code content.
 * @param renderHeaderOverride Optional custom renderer for the code block header.
 * @see IBlockRenderer
 * @see CodeRenderer
 * @see CodeWidgetRenderer
 */
class IndentedCodeBlockRenderer(
    renderCopyOverride: CodeWidgetRenderer<IndentedCodeBlock>? = null,
    renderContentOverride: CodeWidgetRenderer<IndentedCodeBlock>? = null,
    renderHeaderOverride: CodeWidgetRenderer<IndentedCodeBlock>? = null,
) : IBlockRenderer<IndentedCodeBlock> by CodeRenderer(
    renderCopyOverride,
    renderContentOverride,
    renderHeaderOverride,
)

