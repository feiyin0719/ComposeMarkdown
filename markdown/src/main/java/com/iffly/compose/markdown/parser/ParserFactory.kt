package com.iffly.compose.markdown.parser

import org.commonmark.parser.Parser
import org.commonmark.ext.gfm.tables.TablesExtension

class ParserFactory {
    fun build(): Parser {
        val extensions = listOf(TablesExtension.create())
        return Parser.builder()
            .extensions(extensions)
            .build()
    }
}