package com.iffly.compose.markdown.samples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.DefaultStreamingMarkdownParser
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

@Composable
fun StreamingMarkdownExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val chunks =
        remember {
            listOf(
                "# Streaming Markdown\n\n",
                "Completed blocks remain stable while content is appended.",
                "\n\n## Incremental tail\n\n",
                "```kotlin\n",
                "val message = \"Hello",
                ", streaming Markdown!\"\n",
                "println(message)\n",
                "```\n\n",
                "The completed stream receives one final full parse.",
            )
        }
    val markdownRenderConfig =
        remember {
            MarkdownRenderConfig
                .Builder()
                .streamingMarkdownParserFactory(::DefaultStreamingMarkdownParser)
                .build()
        }
    var content by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(true) }

    LaunchedEffect(chunks) {
        chunks.forEach { chunk ->
            delay(350)
            content += chunk
        }
        isStreaming = false
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
    ) {
        if (isStreaming) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        MarkdownView(
            content = content,
            markdownRenderConfig = markdownRenderConfig,
            modifier = Modifier.padding(16.dp),
            parseDispatcher = Dispatchers.Default,
            isStreaming = isStreaming,
        )
    }
}
