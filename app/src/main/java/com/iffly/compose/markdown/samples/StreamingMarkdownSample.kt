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
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.streaming.DefaultStreamingMarkdownParser
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
                StreamingChunk("# Streaming", 120),
                StreamingChunk(" Markdown\n\n", 260),
                StreamingChunk("This sample appends ", 90),
                StreamingChunk("**short tokens**", 180),
                StreamingChunk(
                    ", medium fragments, and a much longer paragraph so the final block changes " +
                        "shape several times before its boundary becomes stable.",
                    620,
                ),
                StreamingChunk("\n\n## Uneven cadence\n\n", 850),
                StreamingChunk("- Fast item", 80),
                StreamingChunk(
                    "\n- A second item arrives after a longer pause and contains enough text to wrap across lines.",
                    720,
                ),
                StreamingChunk("\n- Tiny", 110),
                StreamingChunk("\n\n> Completed blocks stay in the document", 430),
                StreamingChunk("\n> while only the active tail is reparsed.", 170),
                StreamingChunk("\n\n## Incremental code fence\n\n```kotlin\n", 760),
                StreamingChunk("val message", 130),
                StreamingChunk(" = \"Hello", 70),
                StreamingChunk(", streaming Markdown!\"", 480),
                StreamingChunk("\nrepeat(3) { index ->\n", 240),
                StreamingChunk("    println(\"${'$'}index: ${'$'}message\")\n", 360),
                StreamingChunk("}\n```\n\n", 140),
                StreamingChunk(
                    "The completed stream receives one final full parse, restoring document-wide " +
                        "parser semantics after all partial updates have arrived.",
                    880,
                ),
                StreamingChunk("\n\n---\n\n", 220),
                StreamingChunk("_Streaming complete._", 540),
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
            delay(chunk.delayMillis)
            content += chunk.content
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

private data class StreamingChunk(
    val content: String,
    val delayMillis: Long,
)
