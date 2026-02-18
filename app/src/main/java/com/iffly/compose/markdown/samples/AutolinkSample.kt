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
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.autolink.AutolinkMarkdownRenderPlugin
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.vladsch.flexmark.util.ast.Node

@Composable
fun AutolinkExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // Create configuration with Autolink plugin
    val autolinkConfig =
        MarkdownRenderConfig
            .Builder()
            .addPlugin(
                AutolinkMarkdownRenderPlugin(),
            ).build()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
    ) {
        // Description card
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Text(
                text =
                    "This example uses the Autolink plugin to automatically convert URLs and email " +
                        "addresses into clickable links without Markdown syntax.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        MarkdownView(
            content =
                """
                # Autolink Plugin Example
                
                The Autolink plugin automatically detects URLs and email addresses in the text and converts them into links.
                
                ## URLs
                
                Here are some URLs:
                - https://www.google.com
                - http://example.com
                - www.github.com
                
                ## Email Addresses
                
                Here are some email addresses:
                - user@example.com
                - contact@support.com
                
                ## Mixed Content
                
                You can visit https://kotlinlang.org to learn more about Kotlin.
                Or send an email to info@jetbrains.com.
                
                Note that standard Markdown links still work:
                [Android Developers](https://developer.android.com)
                """.trimIndent(),
            markdownRenderConfig = autolinkConfig,
            actionHandler =
                object : ActionHandler {
                    override fun handleUrlClick(
                        url: String,
                        node: Node,
                    ) {
                        Log.d("AutolinkExample", "Clicked URL: $url")
                    }
                },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
    }
}
