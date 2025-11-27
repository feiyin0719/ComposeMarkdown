package com.iffly.compose.markdown.samples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
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
fun DarkThemeExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val darkTypography =
        MarkdownTheme(
            textStyle =
                TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color.White,
                ),
            strongEmphasis =
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64B5F6),
                ),
            emphasis =
                SpanStyle(
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFFBA68C8),
                ),
            code =
                TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Color(0xFF81C784),
                    background = Color(0xFF2E2E2E),
                ),
            headStyle =
                mapOf(
                    1 to
                        TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64B5F6),
                        ),
                    2 to
                        TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81C784),
                        ),
                    3 to
                        TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBA68C8),
                        ),
                ),
        )

    val config =
        MarkdownRenderConfig
            .Builder()
            .markdownTheme(darkTypography)
            .build()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF121212),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
        ) {
            MarkdownView(
                content =
                    """
                    # Dark Theme Example
                    
                    This example demonstrates Markdown styles specifically designed for dark mode.
                    
                    ## Key Features
                    
                    **Blue bold text** is easier to read on dark backgrounds
                    
                    *Purple italic text* provides elegant emphasis effects
                    
                    `Green code text` is clearly visible on dark backgrounds
                    
                    ### Level 3 headings use purple
                    
                    Dark theme is not just color inversion, but a color scheme specifically optimized for nighttime reading.
                    
                    ## Code Block Example
                    
                    ```kotlin
                    // Code display under dark theme
                    val darkTheme = TypographyStyle(
                        body = SpanStyle(color = Color.White),
                        code = SpanStyle(
                            color = Color(0xFF81C784),
                            background = Color(0xFF2E2E2E)
                        )
                    )
                    ```
                    
                    ## Advantages
                    
                    - 🌙 Reduces eye strain
                    - 🔋 Saves battery life (OLED screens)
                    - 🎨 Modern design style
                    - 📱 Matches system theme
                    """.trimIndent(),
                markdownRenderConfig = config,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
