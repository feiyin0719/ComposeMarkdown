package com.iffly.compose.markdown.table

import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataHolder

/**
 * A safe replacement for Flexmark's `TablesExtension` that uses per-cell inline parsing.
 *
 * The original `TablesExtension` registers `TableParagraphPreProcessor` which passes the
 * entire table row to `inlineParser.parseCustom()`, allowing inline parser extensions
 * (e.g., LaTeX `$` delimiters) to match content across cell boundaries.
 *
 * This extension registers [SafeTableParagraphPreProcessor] instead, which splits each
 * row into individual cells first, then runs inline parsing on each cell independently.
 * This prevents cross-cell interference between inline parser extensions.
 */
class SafeTablesExtension private constructor() : Parser.ParserExtension {
    companion object {
        fun create(): SafeTablesExtension = SafeTablesExtension()
    }

    override fun parserOptions(options: MutableDataHolder?) {}

    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.paragraphPreProcessorFactory(SafeTableParagraphPreProcessor.factory())
    }
}
