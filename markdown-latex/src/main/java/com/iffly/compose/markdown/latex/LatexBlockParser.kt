package com.iffly.compose.markdown.latex

import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.block.AbstractBlockParser
import com.vladsch.flexmark.parser.block.AbstractBlockParserFactory
import com.vladsch.flexmark.parser.block.BlockContinue
import com.vladsch.flexmark.parser.block.BlockParserFactory
import com.vladsch.flexmark.parser.block.BlockStart
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory
import com.vladsch.flexmark.parser.block.MatchedBlockParser
import com.vladsch.flexmark.parser.block.ParserState
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.data.MutableDataHolder
import com.vladsch.flexmark.util.sequence.BasedSequence

/**
 * Block node representing a display LaTeX math environment delimited by $$ ... $$
 */
class LatexBlock : Block() {
    var formula: String = ""
    override fun getSegments(): Array<BasedSequence> = emptyArray()
}

private class LatexBlockParser(private val block: LatexBlock) : AbstractBlockParser() {
    private val lines = mutableListOf<String>()
    private var finished = false

    override fun getBlock(): Block = block

    override fun tryContinue(state: ParserState): BlockContinue? {
        if (finished) return BlockContinue.none()
        val line = state.line
        val nextNonSpace = state.nextNonSpaceIndex
        if (nextNonSpace < line.length && line[nextNonSpace] == '$' && nextNonSpace + 1 < line.length && line[nextNonSpace + 1] == '$') {
            // Potential closing $$
            val after = line.subSequence(nextNonSpace + 2, line.length).toString()
            if (after.trim().isEmpty()) {
                finished = true
                return BlockContinue.atIndex(line.length)
            }
        }
        return BlockContinue.atIndex(state.index)
    }

    override fun addLine(state: ParserState, line: BasedSequence) {
        if (finished) return
        val text = line.toString()
        lines.add(text)
    }

    override fun closeBlock(state: ParserState) {
        if (lines.isEmpty()) return
        // Remove first and last potential delimiters if present
        var content = lines.joinToString("\n")
        // Trim starting $$ if first line starts with it
        content = content.trim()
        if (content.startsWith("$$")) {
            content = content.removePrefix("$$").trimStart()
        }
        if (content.endsWith("$$")) {
            content = content.removeSuffix("$$").trimEnd()
        }
        block.formula = content.trim()
    }
}

class LatexBlockParserFactory : CustomBlockParserFactory, Parser.ParserExtension {
    override fun apply(options: DataHolder): BlockParserFactory =
        object : AbstractBlockParserFactory(options) {
            override fun tryStart(
                state: ParserState,
                matchedBlockParser: MatchedBlockParser
            ): BlockStart? {
                val nextNonSpace = state.nextNonSpaceIndex
                val line = state.line
                if (state.indent >= 4) return BlockStart.none()
                if (nextNonSpace + 1 < line.length && line[nextNonSpace] == '$' && line[nextNonSpace + 1] == '$') {
                    val after = line.subSequence(nextNonSpace + 2, line.length).toString()
                    val block = LatexBlock()
                    val parser = LatexBlockParser(block)
                    // Single-line form: $$ formula $$
                    val closingIndex = after.indexOf("$$")
                    if (closingIndex >= 0) {
                        val body = after.substring(0, closingIndex).trim()
                        block.formula = body
                        return BlockStart.of(object : AbstractBlockParser() {
                            override fun getBlock(): Block = block
                            override fun tryContinue(state: ParserState): BlockContinue? =
                                BlockContinue.none()

                            override fun addLine(state: ParserState, line: BasedSequence) {}
                            override fun closeBlock(state: ParserState) {}
                        }).atIndex(line.length)
                    }
                    return BlockStart.of(parser).atIndex(line.length)
                }
                return BlockStart.none()
            }
        }

    override fun getAfterDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun getBeforeDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun affectsGlobalScope(): Boolean = false
    override fun parserOptions(options: MutableDataHolder) {}
    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customBlockParserFactory(this)
    }
}