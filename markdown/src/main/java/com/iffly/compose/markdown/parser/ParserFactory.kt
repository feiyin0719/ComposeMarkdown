package com.iffly.compose.markdown.parser

import org.commonmark.parser.Parser

class ParserFactory {
    fun build(): Parser {
        return Parser.builder().build()
    }
}