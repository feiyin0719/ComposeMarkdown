package com.iffly.compose.markdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.iffly.compose.markdown.samples.MarkdownExample
import com.iffly.compose.markdown.samples.markdownExamples
import com.iffly.compose.markdown.ui.theme.ComposeMarkdownTheme
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val okHttpClient = OkHttpClient.Builder()
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
//            .build()
//
//        val imageLoader = ImageLoader.Builder(this)
//            .crossfade(true)
//            .components {
//                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
//            }
//            .build()
//        SingletonImageLoader.setUnsafe(imageLoader)
        enableEdgeToEdge()
        setContent {
            ComposeMarkdownTheme {
                MarkdownSampleApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownSampleApp() {
    var currentExample by remember { mutableStateOf<MarkdownExample?>(null) }

    if (currentExample == null) {
        ExampleListScreen(onExampleSelected = { currentExample = it })
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentExample!!.title) },
                    navigationIcon = {
                        IconButton(onClick = { currentExample = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            currentExample!!.content(paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleListScreen(onExampleSelected: (MarkdownExample) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Compose Markdown Examples") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(markdownExamples) { example ->
                Card(
                    onClick = { onExampleSelected(example) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = example.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = example.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
