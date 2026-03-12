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
                    "HTML Rendering Demo\n" +
                        "HtmlBlock: HTML-to-Markdown conversion for unified styling\n" +
                        "HtmlInline: push/pop style stack with CSS parsing",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        MarkdownView(
            content =
                """
                # HTML Block Example

                ## HTML Block Rendering

                HTML blocks are converted to Markdown AST for unified styling.

                <p>This is a <b>bold</b> paragraph rendered from HTML block.</p>

                <div>
                  <p>Paragraph inside a div with <i>italic</i> and <b>bold</b> text.</p>
                </div>

                <ul>
                  <li>HTML List Item 1</li>
                  <li>HTML List Item 2</li>
                </ul>

                ---

                # HTML Inline Example

                ## Basic Inline Styles

                Text with <b>bold</b> and <i>italic</i> and <u>underline</u> and <del>strikethrough</del> formatting.

                Nested: <b><i>bold italic</i></b> and <b><u>bold underline</u></b>.

                Alternative tags: <strong>strong</strong> and <em>emphasis</em> and <s>strike</s>.

                ## Line Breaks

                Line one<br>Line two<br/>Line three<br />Line four.

                ## Links

                Visit <a href="https://github.com">GitHub</a> or <a href="https://google.com">Google</a> for more info.

                Mixed: **Markdown bold** with <a href="https://example.com">HTML link</a> inside.

                ## Span with Inline CSS

                <span style="color: red">Red text</span> and <span style="color: blue">blue text</span> and <span style="color: green">green text</span>.

                <span style="color: #FF6600">Orange hex color</span> and <span style="color: rgb(128, 0, 255)">Purple RGB color</span>.

                <span style="background-color: yellow; color: black">Highlighted text</span> with background.

                <span style="font-weight: bold; color: darkred">Bold dark red</span> and <span style="font-style: italic; color: darkblue">Italic dark blue</span>.

                <span style="font-size: 24px; color: purple">Large purple text</span> and <span style="font-size: 12px">small text</span>.

                <span style="text-decoration: underline; color: #1976D2">Underlined styled text</span>.

                ## Mixed Markdown and HTML Inline

                **Bold markdown** with <span style="color: red">red span</span> and *italic markdown* together.

                A paragraph with <b>HTML bold</b>, **Markdown bold**, <i>HTML italic</i>, and *Markdown italic* all mixed.

                ## Back to Markdown

                This is regular **Markdown** content after the HTML sections.

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
