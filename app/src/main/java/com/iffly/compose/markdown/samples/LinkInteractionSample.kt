package com.iffly.compose.markdown.samples

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.vladsch.flexmark.util.ast.Node

@Composable
fun LinkInteractionExample(paddingValues: PaddingValues) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        MarkdownView(
            content = """
                # Link Interaction Example
                
                This example demonstrates different types of link handling:
                
                ## External Links
                [GitHub](https://github.com) - Will open in browser
                
                [Google](https://google.com) - Another external link
                
                ## Internal Links
                [Internal Page A](/internal/page-a) - In-app navigation
                
                [Internal Page B](/internal/page-b) - Another internal page
                
                ## Special Links
                [Email Contact](mailto:example@example.com) - Email link
                
                [Phone Contact](tel:+1234567890) - Phone link
                
                ## Custom Protocol
                [Open App](myapp://custom/action) - Custom protocol
                
                Different links will be handled in different ways!
            """.trimIndent(),
            markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
            modifier = Modifier.padding(16.dp),
            actionHandler = object : ActionHandler {
                override fun handleUrlClick(url: String, node: Node) {

                    Log.d("LinkInteraction", "Clicked link: $url")

                    // Extract URL string from LinkAnnotation
                    val urlString = url

                    when {
                        urlString.startsWith("http") -> {
                            // Open external link
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, urlString.toUri())
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("LinkInteraction", "Cannot open link: $urlString", e)
                            }
                        }

                        urlString.startsWith("/internal") -> {
                            // Handle internal navigation
                            Log.i("LinkInteraction", "Navigate to internal page: $urlString")
                            // Navigation component integration can go here
                        }

                        urlString.startsWith("mailto:") -> {
                            // Handle email link
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO, urlString.toUri())
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("LinkInteraction", "Cannot open email: $urlString", e)
                            }
                        }

                        urlString.startsWith("tel:") -> {
                            // Handle phone link
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, urlString.toUri())
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("LinkInteraction", "Cannot dial phone: $urlString", e)
                            }
                        }

                        else -> {
                            Log.i("LinkInteraction", "Other type of link: $urlString")
                        }
                    }
                }

            }
        )
    }
}
