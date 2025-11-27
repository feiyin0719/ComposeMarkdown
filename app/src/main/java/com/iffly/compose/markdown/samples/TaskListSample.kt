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
import com.iffly.compose.markdown.task.TaskMarkdownRenderPlugin
import com.vladsch.flexmark.util.ast.Node

@Composable
fun TaskListExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // Create configuration with TaskListItem plugin
    val taskConfig =
        MarkdownRenderConfig
            .Builder()
            .addPlugin(
                TaskMarkdownRenderPlugin(),
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
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.3f,
                        ),
                ),
        ) {
            Text(
                text =
                    "📋 Task List Features Demo\n" +
                        "• Checked tasks: [x] Completed task\n" +
                        "• Unchecked tasks: [ ] Pending task\n" +
                        "• Nested task lists supported\n" +
                        "• Custom styling for task items",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        MarkdownView(
            content =
                """
                # Task List Example
                
                This example demonstrates GitHub-style task lists using the TaskMarkdownRenderPlugin.
                
                ## Simple Task List
                
                - [x] Completed task
                - [ ] Pending task
                - [x] Another completed task
                - [ ] Another pending task
                
                ## Project Planning Example
                
                ### Frontend Development
                - [x] Setup project structure
                - [x] Install dependencies
                - [ ] Create components
                  - [x] Header component
                  - [x] Navigation component
                  - [ ] Content area
                  - [ ] Footer component
                - [ ] Add styling
                - [ ] Testing
                
                ### Backend Development
                - [x] Database design
                - [x] API endpoints
                  - [x] User authentication
                  - [x] Data CRUD operations
                  - [ ] File upload
                - [ ] Security implementation
                - [ ] Performance optimization
                
                ## Daily Tasks
                
                ### Today's Goals
                - [x] ☕ Morning coffee
                - [x] 📧 Check emails
                - [x] 💻 Code review
                - [ ] 🏃‍♂️ Exercise
                - [ ] 📚 Read documentation
                - [ ] 🛒 Grocery shopping
                
                ### This Week
                - [x] Monday: Team meeting
                - [x] Tuesday: Sprint planning
                - [ ] Wednesday: Code refactoring
                - [ ] Thursday: Testing
                - [ ] Friday: Release preparation
                
                ## Mixed Content with Tasks
                
                You can also combine task lists with other Markdown elements:
                
                > **Important Note**: Task lists support nested items and can be mixed with regular list items.
                
                1. First, complete these tasks:
                   - [x] Review requirements
                   - [ ] Design mockups
                   - [ ] Get approval
                2. Then proceed with:
                   - [ ] Implementation
                   - [ ] Testing
                   - [ ] Deployment
                
                ```kotlin
                // Task completion tracking
                data class Task(
                    val id: String,
                    val title: String,
                    val isCompleted: Boolean = false
                )
                ```
                
                ---
                
                **Tips**: 
                - Use `[x]` for completed tasks
                - Use `[ ]` for pending tasks  
                - Tasks can be nested within other list items
                - Custom styling can be applied via the TaskMarkdownRenderPlugin
                """.trimIndent(),
            markdownRenderConfig = taskConfig,
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
