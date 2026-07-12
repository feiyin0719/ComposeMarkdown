package com.iffly.compose.markdown.util

import com.iffly.compose.markdown.render.NodeContentHashProvider
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NodeContentHashTest {
    private val parser = Parser.builder().build()

    @Test
    fun `equal content parsed separately has equal hash`() {
        val markdown = "# Heading\n\nA [link](https://example.com).\n\n```kotlin\nval value = 1\n```"

        assertEquals(parser.parse(markdown).contentHash(), parser.parse(markdown).contentHash())
    }

    @Test
    fun `rendered field and child order changes affect hash`() {
        val original = parser.parse("[first](https://one.example) then second")
        val changedDestination = parser.parse("[first](https://two.example) then second")
        val changedOrder = parser.parse("second then [first](https://one.example)")

        assertNotEquals(original.contentHash(), changedDestination.contentHash())
        assertNotEquals(original.contentHash(), changedOrder.contentHash())
    }

    @Test
    fun `custom node controls its render content hash`() {
        val first = Paragraph().apply { appendChild(CustomHashNode("first")) }
        val second = Paragraph().apply { appendChild(CustomHashNode("second")) }

        assertNotEquals(first.contentHash(), second.contentHash())
    }

    @Test
    fun `large trees use stable root identity fallback`() {
        val first = largeParagraph()
        val second = largeParagraph()

        assertEquals(first.contentHash(), first.contentHash())
        assertNotEquals(first.contentHash(), second.contentHash())
    }

    private fun largeParagraph(): Paragraph =
        Paragraph().apply {
            repeat(1024) { appendChild(Text("item")) }
        }

    private class CustomHashNode(
        private val value: String,
    ) : Node(),
        NodeContentHashProvider {
        override fun contentHash(): Int = value.hashCode()

        override fun getSegments(): Array<BasedSequence> = emptyArray()
    }
}
