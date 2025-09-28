package com.iffly.compose.markdown.latex

import com.vladsch.flexmark.parser.InlineParser
import com.vladsch.flexmark.parser.InlineParserExtension
import com.vladsch.flexmark.parser.InlineParserExtensionFactory
import com.vladsch.flexmark.parser.LightInlineParser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence

/**
 * Custom inline LaTeX node representing a `$ ... $` math expression.
 */
class InlineLatexNode(private val seq: BasedSequence, val formula: String) : Node() {
    override fun getSegments(): Array<BasedSequence> = arrayOf(seq)
}

private abstract class BaseInlineLatexExt : InlineParserExtension {
    override fun finalizeDocument(inlineParser: InlineParser) {}
    override fun finalizeBlock(inlineParser: InlineParser) {}
}

/**
 * Inline parser extension for `$...$` math (single dollar). It purposely ignores `$$` which is
 * handled by the display math block parser.
 */
private class InlineLatexParserExtension : BaseInlineLatexExt() {
    override fun parse(inlineParser: LightInlineParser): Boolean {
        if (inlineParser.peek() != '$') return false
        val input = inlineParser.input
        val start = inlineParser.index
        // Don't treat double $$ as inline math (likely a block start)
        if (start + 1 < input.length && input[start + 1] == '$') return false

        var i = start + 1
        var found = false
        while (i < input.length) {
            val ch = input[i]
            if (ch == '$' && input[i - 1] != '\\') { // unescaped closing $
                found = true
                break
            }
            i++
        }
        if (!found) return false
        if (i == start + 1) return false // empty content

        val seq = input.subSequence(start, i + 1)
        val body = seq.subSequence(1, seq.length - 1).toString().trim()
        if (body.isEmpty()) return false
        inlineParser.appendNode(InlineLatexNode(seq, body))
        inlineParser.index = i + 1
        return true
    }
}

class InlineLatexParserFactory : InlineParserExtensionFactory {
    override fun getCharacters(): CharSequence = "$" // Trigger on dollar sign
    override fun apply(inlineParser: LightInlineParser): InlineParserExtension =
        InlineLatexParserExtension()

    override fun affectsGlobalScope(): Boolean = false
    override fun getAfterDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun getBeforeDependents(): MutableSet<Class<*>> = mutableSetOf()
}

