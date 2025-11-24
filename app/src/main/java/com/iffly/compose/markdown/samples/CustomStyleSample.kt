package com.iffly.compose.markdown.samples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.style.MarkdownTheme

@Composable
fun CustomStyleExample(paddingValues: PaddingValues) {
    val customTypography = MarkdownTheme(
        textStyle = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = Color(0xFF2E7D32)
        ),
        strongEmphasis = SpanStyle(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        ),
        emphasis = SpanStyle(
            fontStyle = FontStyle.Italic,
            color = Color(0xFF9C27B0)
        ),
        code = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = Color(0xFF37474F),
            background = Color(0xFFE8F5E8)
        ),
        headStyle = mapOf(
            1 to TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            ),
            2 to TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            ),
            3 to TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )
        )
    )

    val config = MarkdownRenderConfig.Builder()
        .markdownTheme(customTypography)
        .build()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        MarkdownView(
            content = """
                # Custom Style Example
                
                This example demonstrates how to customize Markdown typography styles.
                
                ## Colored Headers
                
                ### Third-level headers also have different colors
                
                **Bold text is now blue**
                
                *Italic text is now purple*
                
                `Inline code has green background`
                
                Body text is now dark green, showcasing a fully customizable style system.
                
                ```kotlin
                // Code blocks maintain original style
                val customStyle = "beautiful"
                println("Custom style: ${'$'}customStyle")
                ```
            """.trimIndent(),
            markdownRenderConfig = config,
            modifier = Modifier.padding(16.dp)
        )
    }
}
