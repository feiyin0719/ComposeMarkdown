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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.plugin.CustomMarkdownPlugin
import com.vladsch.flexmark.util.ast.Node

@Composable
fun CustomPluginExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // Create configuration with custom plugins
    val customConfig =
        MarkdownRenderConfig
            .Builder()
            .addPlugin(CustomMarkdownPlugin())
            .build()

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
            colors = CardDefaults.cardColors(containerColor = Color.Blue.copy(alpha = 0.1f)),
        ) {
            Text(
                text =
                    "💡 Custom Plugin Features Demo\n" +
                        "• Alert blocks: :::info, :::warning, :::success, :::error\n" +
                        "• User mentions: @username\n" +
                        "• Hashtags: #hashtag\n" +
                        "• Highlight text: ==text==\n" +
                        "• Badges: !!type:text!!",
                modifier = Modifier.padding(16.dp),
                color = Color.Blue,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        MarkdownView(
            content =
                """
                # Custom Plugin Example
                
                This example demonstrates various custom Markdown plugin features.
                
                ## 1. Alert Blocks
                
                ### Information Notice
                :::info
                Important Information
                This is an info-type alert block for displaying general information.
                :::
                
                ### Warning Notice
                :::warning
                Notice
                This is a warning-type alert block to remind users of certain matters.
                :::
                
                ### Success Notice
                :::success
                Operation Successful
                This is a success-type alert block for displaying successful operation messages.
                :::
                
                ### Error Notice
                :::error
                Operation Failed
                This is an error-type alert block for displaying error messages.
                :::
                
                ## 2. User Mentions
                
                Welcome @developer and @designer to join our team!
                
                Thanks to @admin for the help, and @user123 for the feedback.
                
                ## 3. Hashtags
                
                This project uses #Android #Compose #Kotlin technology stack.
                
                Related topics: #MobileDevelopment #UIFramework #OpenSource
                
                ## 4. Highlight Text
                
                This text contains ==important highlighted content== that needs special attention.
                
                Please note that ==this feature== has been improved in the new version.
                
                ## 5. Badges
                
                Project status: !!success:Completed!! !!primary:v1.0.0!! !!warning:Testing!!
                
                Tech stack: !!info:Kotlin!! !!primary:Compose!! !!success:Stable!!
                
                Important reminder: !!error:Deprecated!! !!danger:High Risk!! !!warning:Update Required!!
                
                ## 6. Mixed Usage Example
                
                :::info
                Project Update Notification
                
                Thanks to @team_lead for releasing !!primary:v2.0.0!! version!
                
                Main improvements include:
                - ==Performance optimization== #Performance
                - ==UI improvements== #Interface
                - New features !!success:Completed!!
                
                Related personnel: @developer @designer @tester
                Topic tags: #Update #Release #TeamCollaboration
                :::
                
                ## 7. Technical Implementation
                
                ### Block Parser
                ```kotlin
                class AlertBlockParser : AbstractBlockParser() {
                    // Parse :::type content ::: format alert blocks
                }
                ```
                
                ### Inline Parser
                ```kotlin
                class MentionInlineParser : InlineContentParser {
                    // Parse @username format user mentions
                }
                ```
                
                ### Custom Renderer
                ```kotlin
                class AlertBlockRenderer : IBlockRenderer<AlertBlock> {
                    @Composable
                    override fun Invoke(node: AlertBlock, modifier: Modifier) {
                        // Custom Compose UI rendering logic
                    }
                }
                ```
                
                These plugins demonstrate how to extend Markdown syntax and add application-specific features.
                """.trimIndent(),
            markdownRenderConfig = customConfig,
            modifier = Modifier.padding(16.dp),
            actionHandler =
                object : ActionHandler {
                    override fun handleUrlClick(
                        url: String,
                        node: Node,
                    ) {
                        Log.d("BasicSyntax", "Clicked link: $url")
                    }
                },
        )
    }
}
