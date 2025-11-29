package com.iffly.compose.markdown.samples

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.latex.MarkdownMathPlugin
import kotlinx.coroutines.delay

@Composable
fun DynamicMarkdownSample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val mathConfig =
        MarkdownRenderConfig
            .Builder()
            .addPlugin(
                MarkdownMathPlugin(
                    mathStyle =
                        TextStyle(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Start,
                        ),
                ),
            ).build()

    val chunks =
        remember {
            mutableStateListOf(
                // Chunk 1: title and simple inline math
                """
                # 动态 Markdown 示例

                每隔 2 秒追加内容，包含多个数学表达式：${'$'}a^2 + b^2 = c^2${'$'} and ${'$'}a^2 + b^2 = c^2${'$'}。更多的文本来换行测试重叠问题。
                这里有一些额外的文本，以确保我们有足够的内容来测试换行和布局问题。让我们添加更多的句子，看看效果如何。
                """.trimIndent(),
                // Chunk 2: physics
                """
                物理公式：${'$'}E = mc^2${'$'}，速度：${'$'}v = \frac{d}{t}${'$'}，力：${'$'}F = ma${'$'}。更多的文本来换行测试重叠问题。
                这里有一些额外的文本，以确保我们有足够的内容来测试换行和布局问题。让我们添加更多的句子，看看效果如何。
                """.trimIndent(),
                // Chunk 3: fractions and roots
                """
                分数与根式：${'$'}\frac{1}{2}${'$'}，${'$'}\sqrt{2}${'$'}，${'$'}\sqrt{a^2 + b^2}${'$'}。更多的文本来换行测试重叠问题。
                这里有一些额外的文本，以确保我们有足够的内容来测试换行和布局问题。让我们添加更多的句子，看看效果如何。
                """.trimIndent(),
                // Chunk 4: Greek symbols and combined
                """
                希腊字母：${'$'}\alpha + \beta = \gamma${'$'}。

                二次公式：${'$'}x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}${'$'}，适用于 ${'$'}ax^2 + bx + c = 0${'$'}。 更多的文本来测试
                
                """.trimIndent(),
                // Chunk 5: block math
                """
                ---

                ${"$$"}
                \int_0^{\pi} \sin(x)\,dx = 2
                ${"$$"}
                """.trimIndent(),
                // Chunk 6: matrices and summations
                """
                矩阵与求和：

                ${"$$"}
                \begin{bmatrix}
                1 & 2\\
                3 & 4
                \end{bmatrix}
                ${"$$"}

                ${"$$"}
                \sum_{i=1}^{n} i = \frac{n(n+1)}{2}
                ${"$$"}
                """.trimIndent(),
            )
        }

    var appended by remember { mutableStateOf<List<String>>(listOf(chunks[0])) }

    LaunchedEffect(Unit) {
        var index = 0
        while (true) {
            appended = appended + chunks[index % chunks.size]
            index++
            if (index % 4 == 1) {
                appended = listOf(chunks[3]) + appended.subList(1, appended.size)
            } else if (index % 4 == 3) {
                appended = listOf(chunks[0]) + appended.subList(1, appended.size)
            }
            delay(500)
        }
    }

    val content =
        remember(appended) {
            appended.joinToString(separator = "\n\n")
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                ),
        ) {
            Text(
                text = "动态追加 Markdown 内容（每 2 秒一段），包含多段数学表达式。",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        MarkdownView(
            content = content,
            markdownRenderConfig = mathConfig,
            modifier = Modifier.padding(16.dp),
        )
    }
}
