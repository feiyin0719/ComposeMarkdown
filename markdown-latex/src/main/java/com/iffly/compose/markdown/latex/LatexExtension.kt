package com.iffly.compose.markdown.latex

import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataHolder

class LatexExtension private constructor() : Parser.ParserExtension {

    companion object {
        fun create(): LatexExtension {
            return LatexExtension()
        }
    }

    override fun parserOptions(options: MutableDataHolder?) {
    }

    override fun extend(parserBuilder: Parser.Builder?) {
        parserBuilder?.let {
            it.customInlineParserExtensionFactory(InlineLatexParserFactory())
            it.customBlockParserFactory(LatexBlockParserFactory())
        }
    }
}