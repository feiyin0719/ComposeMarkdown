package com.iffly.compose.markdown.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import kotlinx.coroutines.Dispatchers

@Composable
fun AsyncLoadingExample(paddingValues: PaddingValues) {
    var content by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        content = """
            # Async Loading Complete!
            
            This content was obtained through async loading, demonstrating how to handle:
            
            ## Loading States
            - ✅ Show loading indicator
            - ✅ Async Markdown parsing
            - ✅ Update UI state
            
            ## Use Cases
            - Fetch Markdown content from network
            - Read large local files
            - Content that requires preprocessing
            
            **Tip**: Async parsing doesn't block the UI thread, providing better user experience.
        """.trimIndent()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(state = rememberScrollState())
    ) {
        MarkdownView(
            content = content,
            markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
            parseDispatcher = Dispatchers.Default,
            modifier = Modifier
                .padding(16.dp),
            onLoading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        )
    }

}
