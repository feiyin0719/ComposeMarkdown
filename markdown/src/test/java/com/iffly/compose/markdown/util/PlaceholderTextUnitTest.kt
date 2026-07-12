package com.iffly.compose.markdown.util

import androidx.compose.ui.unit.Density
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceholderTextUnitTest {
    @Test
    fun `converts measured pixels with linear density and font scale`() {
        val density = Density(density = 2f, fontScale = 2f)

        val width = density.toPlaceholderSp(px = 40)
        val height = density.toPlaceholderSp(px = 20)

        assertEquals(10f, width.value, 0f)
        assertEquals(5f, height.value, 0f)
        assertEquals(2f, width.value / height.value, 0f)
    }
}
