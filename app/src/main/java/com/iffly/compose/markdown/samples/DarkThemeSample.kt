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
import com.iffly.compose.markdown.style.TypographyStyle

@Composable
fun DarkThemeExample(paddingValues: PaddingValues) {
    val darkTypography = TypographyStyle(
        textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
        body = SpanStyle(color = Color.White),
        strongEmphasis = SpanStyle(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64B5F6)
        ),
        emphasis = SpanStyle(
            fontStyle = FontStyle.Italic,
            color = Color(0xFFBA68C8)
        ),
        code = SpanStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = Color(0xFF81C784),
            background = Color(0xFF2E2E2E)
        ),
        head = mapOf(
            1 to SpanStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64B5F6)
            ),
            2 to SpanStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF81C784)
            ),
            3 to SpanStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBA68C8)
            )
        )
    )

    val config = MarkdownRenderConfig.Builder()
        .typographyStyle(darkTypography)
        .build()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            MarkdownView(
                content = """
                    # 暗黑主题示例
                    
                    这个示例展示了专为暗黑模式设计的Markdown样式。
                    
                    ## 特色功能
                    
                    **蓝色粗体文本** 在暗背景下更易阅读
                    
                    *紫色斜体文本* 提供了优雅的强调效果
                    
                    `绿色代码文本` 在深色背景上清晰可见
                    
                    ### 三级标题使用紫色
                    
                    暗黑主题不仅仅是颜色的反转，而是为夜间阅读专门优化的配色方案。
                    
                    ## 代码块示例
                    
                    ```kotlin
                    // 暗黑主题下的代码显示
                    val darkTheme = TypographyStyle(
                        body = SpanStyle(color = Color.White),
                        code = SpanStyle(
                            color = Color(0xFF81C784),
                            background = Color(0xFF2E2E2E)
                        )
                    )
                    ```
                    
                    ## 优势
                    
                    - 🌙 减少眼部疲劳
                    - 🔋 节省电池电量（OLED屏幕）
                    - 🎨 现代化设计风格
                    - 📱 符合系统主题
                """.trimIndent(),
                markdownRenderConfig = config,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
