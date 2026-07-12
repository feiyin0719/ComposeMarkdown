package com.iffly.compose.markdown.chunkloader

import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.parser.Parser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownChunkLoaderTest {
    private val parser = Parser.builder().build()

    @Test
    fun `trailing block is held until the next range confirms its boundary`() =
        runBlocking {
            val source =
                StringMarkdownLineSource(
                    """
                    # Heading

                    ```kotlin
                    val value = 1
                    ```

                    After
                    """.trimIndent(),
                )
            val loader = MarkdownChunkLoader(source, parser)

            val first = loader.load(3)
            assertEquals(1, first.nodes.size)
            assertTrue(first.nodes.single().node is Heading)
            assertEquals(0..0, first.nodes.single().startLine..first.nodes.single().endLine)
            assertFalse(first.endOfSource)

            val second = loader.load(3)
            assertEquals(1, second.nodes.size)
            assertTrue(second.nodes.single().node is FencedCodeBlock)
            assertEquals(2..4, second.nodes.single().startLine..second.nodes.single().endLine)
            assertFalse(second.endOfSource)

            val third = loader.load(3)
            assertEquals(1, third.nodes.size)
            assertEquals(6..6, third.nodes.single().startLine..third.nodes.single().endLine)
            assertTrue(third.endOfSource)
        }

    @Test
    fun `node window recycles behind the viewport and reloads it`() =
        runBlocking {
            val text = (0 until 12).joinToString("\n\n") { index -> "# Heading $index" }
            val config =
                ChunkLoaderConfig(
                    initialLineCount = 8,
                    incrementalLineCount = 8,
                    minNodesAhead = 1,
                    minNodesBehind = 0,
                    maxCachedNodes = 4,
                    maxCachedSourceLines = 100,
                    minRecycleNodeCount = 2,
                )
            val loader = MarkdownChunkLoader(StringMarkdownLineSource(text), parser)
            val window = MarkdownNodeWindow(loader, config)

            val initial = window.loadInitial()
            val firstKey = initial.nodes.first().key
            val after = window.loadAfter(initial.nodes.last().key)

            assertTrue(after.canLoadBefore)
            assertFalse(after.nodes.any { it.key == firstKey })

            val before = window.loadBefore(after.nodes.first().key)
            assertTrue(before.nodes.any { it.key == firstKey })
        }

    @Test
    fun `read batch may exceed source limit when retained blocks do not`() =
        runBlocking {
            val loader =
                MarkdownChunkLoader(
                    source = StringMarkdownLineSource("# One\n\n# Two\n\n# Three\n\n# Four"),
                    parser = parser,
                    maxCachedSourceLines = 2,
                )

            val first = loader.load(6)
            assertEquals(3, first.nodes.size)
            assertFalse(first.endOfSource)

            val second = loader.load(6)
            assertEquals(1, second.nodes.size)
            assertTrue(second.endOfSource)
        }

    @Test
    fun `source pressure recycles fewer nodes than the normal minimum`() =
        runBlocking {
            val config =
                ChunkLoaderConfig(
                    initialLineCount = 6,
                    incrementalLineCount = 2,
                    minNodesAhead = 1,
                    minNodesBehind = 0,
                    maxCachedNodes = 10,
                    maxCachedSourceLines = 3,
                    minRecycleNodeCount = 5,
                )
            val loader =
                MarkdownChunkLoader(
                    source = StringMarkdownLineSource("# One\n\n# Two\n\n# Three\n\n# Four"),
                    parser = parser,
                    maxCachedSourceLines = config.maxCachedSourceLines,
                )
            val window = MarkdownNodeWindow(loader, config)

            val initial = window.loadInitial()
            assertTrue(initial.needsRecycle)

            val recycled = window.recycle(initial.nodes[1].key, initial.nodes.last().key)
            assertEquals(2, recycled.nodes.size)
            assertEquals(3, recycled.cachedSourceLineCount)
            assertTrue(recycled.canLoadBefore)
        }

    @Test
    fun `single unconfirmed block cannot exceed source limit`() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                val loader =
                    MarkdownChunkLoader(
                        source = StringMarkdownLineSource("```\none\ntwo\n```\n\n# Next"),
                        parser = parser,
                        maxCachedSourceLines = 3,
                    )
                loader.load(4)
            }
        }
    }
}
