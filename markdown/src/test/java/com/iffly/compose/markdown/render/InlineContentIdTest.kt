package com.iffly.compose.markdown.render

import org.junit.Assert.assertEquals
import org.junit.Test

class InlineContentIdTest {
    @Test
    fun `duplicate content ids preserve every inline renderer`() {
        val inlineContent = mutableMapOf<String, String>()

        val firstId = inlineContent.putUniqueInlineContent("CodeBlock_42", "first")
        val secondId = inlineContent.putUniqueInlineContent("CodeBlock_42", "second")

        assertEquals("CodeBlock_42", firstId)
        assertEquals("CodeBlock_42_1", secondId)
        assertEquals(mapOf(firstId to "first", secondId to "second"), inlineContent)
    }
}
