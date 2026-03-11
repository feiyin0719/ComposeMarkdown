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
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.html.HtmlMarkdownPlugin
import com.vladsch.flexmark.util.ast.Node

@Composable
fun HtmlBlockExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val htmlConfig =
        MarkdownRenderConfig
            .Builder()
            .addPlugin(HtmlMarkdownPlugin())
            .build()

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
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.3f,
                        ),
                ),
        ) {
            Text(
                text =
                    "HTML Block Rendering Demo\n" +
                        "Demonstrates rendering of raw HTML blocks in Markdown\n" +
                        "HTML is converted to Markdown AST for unified styling",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        MarkdownView(
            content =
                """
                # HTML Block Example

                This example demonstrates HTML block rendering using the HtmlMarkdownPlugin.

                ## Inline HTML in Markdown

                Regular markdown text with **bold** and *italic*.

                <p>This is a <b>bold</b> paragraph rendered from HTML.</p>

                <div>
                  <p>This is a paragraph inside a div.</p>
                  <p>With <i>italic</i> and <b>bold</b> text.</p>
                </div>

                ## HTML Line Breaks

                <p>Line one<br>Line two<br>Line three</p>

                ## HTML Lists

                <ul>
                  <li>HTML List Item 1</li>
                  <li>HTML List Item 2</li>
                  <li>HTML List Item 3</li>
                </ul>

                ## Back to Markdown

                This is regular **Markdown** content after the HTML blocks.

                - Markdown list item 1
                - Markdown list item 2
                """.trimIndent(),
            markdownRenderConfig = htmlConfig,
            modifier = Modifier.padding(16.dp),
            actionHandler =
                object : ActionHandler {
                    override fun handleUrlClick(
                        url: String,
                        node: Node,
                    ) {
                        Log.d("HtmlBlock", "Clicked link: $url")
                    }
                },
        )
    }
}
