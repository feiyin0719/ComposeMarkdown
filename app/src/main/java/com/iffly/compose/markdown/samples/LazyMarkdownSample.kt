package com.iffly.compose.markdown.samples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.LazyMarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import java.io.File
import java.io.FileOutputStream

@Composable
fun LazyMarkdownExample(paddingValues: PaddingValues) {
    val context = LocalContext.current
    var markdownFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load markdown file from assets and copy to temporary file
    LaunchedEffect(Unit) {
        try {
            val inputStream = context.assets.open("large_document.md")
            val tempFile = File(context.cacheDir, "large_document.md")

            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            markdownFile = tempFile
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = "Failed to load document: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        errorMessage?.let { error ->
            Text(text = error)
        } ?: markdownFile?.let { file ->
            val config = MarkdownRenderConfig.Builder().build()

            LazyMarkdownView(
                file = file,
                markdownRenderConfig = config,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}
