package com.iffly.compose.markdown.samples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig

@Composable
fun ErrorHandlingExample(paddingValues: PaddingValues) {
    // 故意创建一个可能出错的场景
    val invalidContent = """
        # 错误处理示例
        
        这个示例展示正常内容和错误处理：
        
        ## 正常内容
        这部分内容应该正常显示。
        
        **粗体** 和 *斜体* 文本。
        
        ## 潜在问题
        在实际应用中，可能遇到：
        - 网络请求失败
        - 文件读取错误  
        - 解析异常
        
        库提供了优雅的错误处理机制。
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Blue.copy(alpha = 0.1f))
        ) {
            Text(
                text = "💡 提示：这个示例展示了错误处理机制",
                modifier = Modifier.padding(16.dp),
                color = Color.Blue
            )
        }

        MarkdownView(
            content = invalidContent,
            markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
            modifier = Modifier.padding(16.dp),
            onError = { throwable ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ Markdown 解析失败",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "错误信息: ${throwable.message}",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        )
    }
}
