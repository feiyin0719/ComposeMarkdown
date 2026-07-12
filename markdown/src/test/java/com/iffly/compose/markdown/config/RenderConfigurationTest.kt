package com.iffly.compose.markdown.config

import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class RenderConfigurationTest {
    @Test
    fun `builder creates independently scoped configurations`() {
        assertNotSame(MarkdownRenderConfig.Builder().build(), MarkdownRenderConfig.Builder().build())
    }

    @Test
    fun `text mode registry is cached`() {
        val registry = MarkdownRenderConfig.Builder().build().renderRegistry

        assertSame(registry.textModeRegistry(), registry.textModeRegistry())
    }
}
