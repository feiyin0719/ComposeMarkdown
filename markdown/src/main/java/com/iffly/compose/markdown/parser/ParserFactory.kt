package com.iffly.compose.markdown.parser

import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser

class ParserFactory {
    fun build(): Parser {
        val extensions = listOf(TablesExtension.create())
        return Parser.builder()
            .extensions(extensions)
            .build()
    }
}