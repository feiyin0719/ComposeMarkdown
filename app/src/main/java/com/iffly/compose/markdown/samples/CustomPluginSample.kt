package com.iffly.compose.markdown.samples

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.plugin.CustomMarkdownPlugin
import com.iffly.compose.markdown.plugin.HighlightDelimiterProcessor

@Composable
fun CustomPluginExample(paddingValues: PaddingValues) {
    // 创建包含自定义插件的配置
    val customConfig = MarkdownRenderConfig.Builder()
        .addPlugin(CustomMarkdownPlugin())
        .addDelimiterProcessor(HighlightDelimiterProcessor())
        .build()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        // 说明卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Blue.copy(alpha = 0.1f))
        ) {
            Text(
                text = "💡 自定义插件功能展示\n" +
                        "• 告警块: :::info, :::warning, :::success, :::error\n" +
                        "• 提及用户: @username\n" +
                        "• 标签: #hashtag\n" +
                        "• 高亮文本: ==文本==\n" +
                        "• 徽章: !!type:text!!",
                modifier = Modifier.padding(16.dp),
                color = Color.Blue,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        MarkdownView(
            content = """
                # 自定义插件示例
                
                这个示例展示了各种自定义Markdown插件的功能。
                
                ## 1. 告警块 (Alert Blocks)
                
                ### 信息提示
                :::info
                重要信息
                这是一个信息类型的告警块，用于显示一般性信息。
                :::
                
                ### 警告提示
                :::warning
                注意事项
                这是一个警告类型的告警块，用于提醒用户注意某些事项。
                :::
                
                ### 成功提示
                :::success
                操作成功
                这是一个成功类型的告警块，用于显示操作成功的消息。
                :::
                
                ### 错误提示
                :::error
                操作失败
                这是一个错误类型的告警块，用于显示错误信息。
                :::
                
                ## 2. 用户提及 (Mentions)
                
                欢迎 @developer 和 @designer 加入我们的团队！
                
                感谢 @admin 的帮助，以及 @user123 的反馈。
                
                ## 3. 话题标签 (Hashtags)
                
                这个项目使用了 #Android #Compose #Kotlin 技术栈。
                
                相关话题：#移动开发 #UI框架 #开源项目
                
                ## 4. 高亮文本 (Highlight)
                
                这段文本包含 ==重要的高亮内容== 需要特别注意。
                
                请注意 ==这个功能== 在新版本中有所改进。
                
                ## 5. 徽章 (Badges)
                
                项目状态：!!success:已完成!! !!primary:v1.0.0!! !!warning:测试中!!
                
                技术栈：!!info:Kotlin!! !!primary:Compose!! !!success:稳定版!!
                
                重要提醒：!!error:已废弃!! !!danger:高风险!! !!warning:需更新!!
                
                ## 6. 混合使用示例
                
                :::info
                项目更新通知
                
                感谢 @team_lead 发布了 !!primary:v2.0.0!! 版本！
                
                主要改进包括：
                - ==性能优化== #性能
                - ==UI改进== #界面
                - 新增功能 !!success:已完成!!
                
                相关人员：@developer @designer @tester
                话题标签：#更新 #版本发布 #团队协作
                :::
                
                ## 7. 技术实现说明
                
                ### Block 解析器
                ```kotlin
                class AlertBlockParser : AbstractBlockParser() {
                    // 解析 :::type 内容 ::: 格式的告警块
                }
                ```
                
                ### Inline 解析器
                ```kotlin
                class MentionInlineParser : InlineContentParser {
                    // 解析 @username 格式的用户提及
                }
                ```
                
                ### 自定义渲染器
                ```kotlin
                class AlertBlockRenderer : IBlockRenderer<AlertBlock> {
                    @Composable
                    override fun Invoke(node: AlertBlock, modifier: Modifier) {
                        // 自定义Compose UI渲染逻辑
                    }
                }
                ```
                
                这些插件展示了如何扩展Markdown语法，添加应用专属的功能。
            """.trimIndent(),
            markdownRenderConfig = customConfig,
            modifier = Modifier.padding(16.dp),
            linkInteractionListener = { linkAnnotation ->
                Log.d("CustomPlugin", "点击链接: $linkAnnotation")
            }
        )
    }
}
