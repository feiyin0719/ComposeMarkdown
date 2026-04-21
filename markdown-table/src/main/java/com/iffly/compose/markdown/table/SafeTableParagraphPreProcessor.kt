package com.iffly.compose.markdown.table

import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ext.tables.TableBlock
import com.vladsch.flexmark.ext.tables.TableBody
import com.vladsch.flexmark.ext.tables.TableCaption
import com.vladsch.flexmark.ext.tables.TableCell
import com.vladsch.flexmark.ext.tables.TableHead
import com.vladsch.flexmark.ext.tables.TableRow
import com.vladsch.flexmark.ext.tables.TableSeparator
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.parser.block.ParagraphPreProcessor
import com.vladsch.flexmark.parser.block.ParagraphPreProcessorFactory
import com.vladsch.flexmark.parser.block.ParserState
import com.vladsch.flexmark.parser.core.ReferencePreProcessorFactory
import com.vladsch.flexmark.util.ast.DoNotDecorate
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.sequence.BasedSequence
import java.util.Locale
import java.util.regex.Pattern

/**
 * A safe replacement for Flexmark's TableParagraphPreProcessor that splits table rows
 * into individual cells BEFORE running inline parsing on each cell. This prevents inline
 * parser extensions (like LaTeX $ delimiters) from scanning across cell boundaries.
 *
 * The original Flexmark implementation calls `inlineParser.parseCustom(fullRowLine, ...)`
 * which passes the entire row text to the inline parser, allowing inline extensions to
 * incorrectly match content spanning multiple cells (e.g., `$15,000` in one cell matching
 * with `$28,500` in another cell as a LaTeX formula).
 */
internal class SafeTableParagraphPreProcessor(
    options: DataHolder,
) : ParagraphPreProcessor {
    // Read options directly from DataKeys since TableParserOptions is package-private
    private val minSeparatorDashes: Int = TablesExtension.MIN_SEPARATOR_DASHES[options]
    private val maxHeaderRows: Int = TablesExtension.MAX_HEADER_ROWS[options]
    private val minHeaderRows: Int = TablesExtension.MIN_HEADER_ROWS[options]
    private val withCaption: Boolean = TablesExtension.WITH_CAPTION[options]
    private val discardExtraColumns: Boolean = TablesExtension.DISCARD_EXTRA_COLUMNS[options]
    private val headerSeparatorColumnMatch: Boolean = TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH[options]
    private val appendMissingColumns: Boolean = TablesExtension.APPEND_MISSING_COLUMNS[options]
    private val tableHeaderSeparator = getTableHeaderSeparator(minSeparatorDashes)

    private class TableSeparatorRow(
        chars: BasedSequence,
    ) : TableRow(chars),
        DoNotDecorate

    override fun preProcessBlock(
        block: Paragraph,
        state: ParserState,
    ): Int {
        val inlineParser = state.inlineParser

        val tableLines = mutableListOf<BasedSequence>()
        var separatorLineNumber = -1
        var separatorLine: BasedSequence? = null
        val blockIndent = block.getLineIndent(0)
        var captionLine: BasedSequence? = null

        for (rowLine in block.contentLines) {
            val rowNumber = tableLines.size
            if (separatorLineNumber == -1 && rowNumber > maxHeaderRows) return 0

            if (rowLine.indexOf('|') < 0) {
                if (separatorLineNumber == -1) return 0
                if (withCaption) {
                    val trimmed = rowLine.trim()
                    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                        captionLine = trimmed
                    }
                }
                break
            }

            val trimmedRowLine = rowLine.subSequence(block.getLineIndent(rowNumber))

            if (separatorLineNumber == -1) {
                if (rowNumber >= minHeaderRows &&
                    tableHeaderSeparator.matcher(trimmedRowLine).matches()
                ) {
                    if ((rowLine[0] != ' ' && rowLine[0] != '\t') || rowLine[0] != '|') {
                        separatorLineNumber = rowNumber
                        separatorLine = trimmedRowLine
                    } else if (rowLine[0] == ' ' || rowLine[0] == '\t') {
                        block.setHasTableSeparator(true)
                    }
                }
            }

            tableLines.add(trimmedRowLine)
        }

        if (separatorLineNumber == -1) return 0

        val alignments = parseAlignment(separatorLine!!)
        val separatorColumns = alignments.size

        val tableBlock = TableBlock(tableLines)
        var section: Node =
            TableHead(tableLines[0].subSequence(0, 0))
        tableBlock.appendChild(section)

        for (rowNumber in tableLines.indices) {
            if (rowNumber >= tableLines.size) break

            val rowLine = tableLines[rowNumber]
            val fullRowLine =
                if (block.getLineIndent(rowNumber) <= blockIndent) {
                    rowLine.trimEOL()
                } else {
                    rowLine.baseSubSequence(
                        rowLine.startOffset - (block.getLineIndent(rowNumber) - blockIndent),
                        rowLine.endOffset - rowLine.eolEndLength(),
                    )
                }

            val isSeparator = rowNumber == separatorLineNumber

            if (rowNumber == separatorLineNumber) {
                section.setCharsFromContent()
                section = TableSeparator()
                tableBlock.appendChild(section)
            } else if (rowNumber == separatorLineNumber + 1) {
                section.setCharsFromContent()
                section = TableBody()
                tableBlock.appendChild(section)
            }

            val tableRow = TableRow(fullRowLine)
            val tableRowNumber =
                when {
                    isSeparator -> 0
                    rowNumber < separatorLineNumber -> rowNumber + 1
                    else -> rowNumber - separatorLineNumber
                }
            tableRow.setRowNumber(tableRowNumber)

            if (isSeparator) {
                // Separator row: create cells with separator content, no inline parsing needed
                val parts = splitRow(fullRowLine)
                for ((cellIndex, cellSeq) in parts.withIndex()) {
                    if (cellIndex >= separatorColumns && discardExtraColumns) break
                    val tableCell = TableCell()
                    tableCell.isHeader = false
                    if (cellIndex < separatorColumns) {
                        tableCell.setAlignment(alignments[cellIndex])
                    }
                    tableCell.setChars(cellSeq)
                    tableRow.appendChild(tableCell)
                }
            } else {
                // Content row: split into cells, then parse inline content per-cell
                val parts = splitRow(fullRowLine)
                if (parts.isEmpty()) {
                    if (rowNumber <= separatorLineNumber) return 0
                    break
                }

                var cellCount = 0
                for ((cellIndex, cellSeq) in parts.withIndex()) {
                    if (cellCount >= separatorColumns && discardExtraColumns) {
                        if (headerSeparatorColumnMatch && rowNumber < separatorLineNumber) {
                            return 0
                        }
                        break
                    }

                    val tableCell = TableCell()
                    tableCell.isHeader = rowNumber < separatorLineNumber
                    if (cellCount < separatorColumns) {
                        tableCell.setAlignment(alignments[cellCount])
                    }

                    // Parse inline content for this cell only
                    val trimmedCell = cellSeq.trim()
                    if (trimmedCell.isNotEmpty) {
                        inlineParser.parse(trimmedCell, tableCell)
                    }

                    tableCell.setText(tableCell.childChars)
                    tableCell.setCharsFromContent()
                    tableRow.appendChild(tableCell)
                    cellCount++
                }

                if (headerSeparatorColumnMatch &&
                    rowNumber < separatorLineNumber &&
                    cellCount < separatorColumns
                ) {
                    return 0
                }

                while (appendMissingColumns && cellCount < separatorColumns) {
                    val tableCell = TableCell()
                    tableCell.isHeader = rowNumber < separatorLineNumber
                    tableCell.setAlignment(alignments[cellCount])
                    tableRow.appendChild(tableCell)
                    cellCount++
                }
            }

            tableRow.setCharsFromContent()
            section.appendChild(tableRow)
        }

        section.setCharsFromContent()

        if (section is TableSeparator) {
            val tableBody = TableBody(section.chars.subSequence(section.chars.length))
            tableBlock.appendChild(tableBody)
        }

        // Add caption if the option is enabled
        if (captionLine != null) {
            val caption =
                TableCaption(
                    captionLine.subSequence(0, 1),
                    captionLine.subSequence(1, captionLine.length - 1),
                    captionLine.subSequence(captionLine.length - 1),
                )
            inlineParser.parse(caption.text, caption)
            caption.setCharsFromContent()
            tableBlock.appendChild(caption)
        }

        tableBlock.setCharsFromContent()

        block.insertBefore(tableBlock)
        state.blockAdded(tableBlock)
        return tableBlock.chars.length
    }

    companion object {
        fun factory(): ParagraphPreProcessorFactory =
            object : ParagraphPreProcessorFactory {
                override fun affectsGlobalScope(): Boolean = false

                override fun getAfterDependents(): MutableSet<Class<*>> = mutableSetOf<Class<*>>(ReferencePreProcessorFactory::class.java)

                override fun getBeforeDependents(): MutableSet<Class<*>>? = null

                override fun apply(state: ParserState): ParagraphPreProcessor = SafeTableParagraphPreProcessor(state.properties)
            }

        internal fun getTableHeaderSeparator(minColumnDashes: Int): Pattern {
            val minCol = if (minColumnDashes >= 1) minColumnDashes else 1
            val minColDash = if (minColumnDashes >= 2) minColumnDashes - 1 else 1
            val minColDashes = if (minColumnDashes >= 3) minColumnDashes - 2 else 1
            val col =
                String.format(
                    Locale.US,
                    "(?:\\s*-{%d,}\\s*|\\s*:-{%d,}\\s*|\\s*-{%d,}:\\s*|\\s*:-{%d,}:\\s*)",
                    minCol,
                    minColDash,
                    minColDash,
                    minColDashes,
                )
            val regex = "\\|${col}\\|?\\s*|${col}\\|\\s*|\\|?(?:${col}\\|)+${col}\\|?\\s*"
            return Pattern.compile(regex)
        }

        /**
         * Split a table row into cell segments at unescaped `|` characters.
         * Leading and trailing pipes are excluded from the result.
         */
        private fun splitRow(line: BasedSequence): List<BasedSequence> {
            val trimmed = line.trim()
            val lineLength = trimmed.length
            if (lineLength == 0) return emptyList()

            var start = 0
            var end = lineLength

            // Skip leading pipe
            if (trimmed[0] == '|') {
                start = 1
            }
            // Skip trailing pipe (but don't miss empty cells like "||")
            if (lineLength > 1 && trimmed[lineLength - 1] == '|') {
                end = lineLength - 1
            }

            val segments = mutableListOf<BasedSequence>()
            var escape = false
            var lastPos = start
            for (i in start until end) {
                val c = trimmed[i]
                if (escape) {
                    escape = false
                } else {
                    when (c) {
                        '\\' -> {
                            escape = true
                        }

                        '|' -> {
                            segments.add(trimmed.subSequence(lastPos, i))
                            lastPos = i + 1
                        }
                    }
                }
            }
            if (lastPos < end) {
                segments.add(trimmed.subSequence(lastPos, end))
            } else if (lastPos == end && segments.isNotEmpty()) {
                // trailing empty cell
                segments.add(trimmed.subSequence(end, end))
            }

            return segments
        }

        private fun parseAlignment(separatorLine: BasedSequence): List<TableCell.Alignment?> {
            val parts = splitRow(separatorLine)
            return parts.map { part ->
                val trimmed = part.trim()
                val left = trimmed.startsWith(":")
                val right = trimmed.endsWith(":")
                getAlignment(left, right)
            }
        }

        private fun getAlignment(
            left: Boolean,
            right: Boolean,
        ): TableCell.Alignment? =
            when {
                left && right -> TableCell.Alignment.CENTER
                left -> TableCell.Alignment.LEFT
                right -> TableCell.Alignment.RIGHT
                else -> null
            }
    }
}
