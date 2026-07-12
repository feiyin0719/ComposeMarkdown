package com.iffly.compose.markdown.render

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.widget.richtext.RichTextInlineContent
import com.iffly.compose.markdown.widget.richtext.getStandaloneInlineTextContentAnnotations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class InlineContentIdTest {
    @Test
    fun `duplicate content ids preserve every inline renderer`() {
        val builder = AnnotatedString.Builder()
        val inlineContent = mutableMapOf<String, MarkdownInlineView>()
        val first = embeddedContent()
        val second = embeddedContent()

        val firstId =
            builder.appendMarkdownInlineContent("CodeBlock_42", first, inlineContent, "first")
        val secondId =
            builder.appendMarkdownInlineContent("CodeBlock_42", second, inlineContent, "second")
        val thirdId =
            builder.appendMarkdownInlineContent("CodeBlock_42", embeddedContent(), inlineContent, "third")

        assertEquals("CodeBlock_42", firstId)
        assertEquals("CodeBlock_42_1", secondId)
        assertEquals("CodeBlock_42_2", thirdId)
        assertEquals(setOf(firstId, secondId, thirdId), inlineContent.keys)
        assertEquals("firstsecondthird", builder.toAnnotatedString().text)
    }

    @Test
    fun `overwrite reuses requested id and replaces content`() {
        val builder = AnnotatedString.Builder()
        val inlineContent = mutableMapOf<String, MarkdownInlineView>()
        val first = embeddedContent()
        val second = embeddedContent()

        builder.appendMarkdownInlineContent("shared", first, inlineContent)
        val actualId =
            builder.appendMarkdownInlineContent(
                id = "shared",
                inlineContent = second,
                inlineContentMap = inlineContent,
                overwrite = true,
            )

        assertEquals("shared", actualId)
        assertEquals(1, inlineContent.size)
        assertSame(
            second,
            (inlineContent.getValue("shared") as MarkdownInlineView.MarkdownRichTextInlineContent).inlineContent,
        )
    }

    @Test
    fun `standalone content uses standalone annotation`() {
        val builder = AnnotatedString.Builder()
        val inlineContent = mutableMapOf<String, MarkdownInlineView>()

        val actualId =
            builder.appendMarkdownInlineContent(
                id = "standalone",
                inlineContent = RichTextInlineContent.StandaloneInlineContent {},
                inlineContentMap = inlineContent,
                alternateText = "fallback",
            )

        val annotation = builder.toAnnotatedString().getStandaloneInlineTextContentAnnotations().single()
        assertEquals(actualId, annotation.item)
        assertEquals("fallback", builder.toAnnotatedString().text)
    }

    @Test
    fun `overwrite rejects a different annotation type`() {
        val builder = AnnotatedString.Builder()
        val inlineContent = mutableMapOf<String, MarkdownInlineView>()
        builder.appendMarkdownInlineContent("shared", embeddedContent(), inlineContent)

        assertThrows(IllegalArgumentException::class.java) {
            builder.appendMarkdownInlineContent(
                id = "shared",
                inlineContent = RichTextInlineContent.StandaloneInlineContent {},
                inlineContentMap = inlineContent,
                overwrite = true,
            )
        }
    }

    private fun embeddedContent(): RichTextInlineContent =
        RichTextInlineContent.EmbeddedRichTextInlineContent(
            placeholder = Placeholder(1.sp, 1.sp, PlaceholderVerticalAlign.Center),
        ) {}
}
