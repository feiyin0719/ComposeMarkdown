package com.iffly.compose.markdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.iffly.compose.markdown.ui.theme.ComposeMarkdownTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeMarkdownTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )
                        MarkdownView(
                            content = """
                            # Hello World
                            This is a simple markdown example.
                            ## Subheading
                            
                            1. Item 1
                            2. Item 2
                            3. Item 3
                            
                            - Item A
                            - Item B
                            - Item C
                            
                            **Bold Text**
                            
                            *Italic Text*![Image](https://raw.githubusercontent.com/feiyin0719/AFreeSvg/dev/test.jpg)
                            
                            [Link to Google](https://www.google.com)
                                                        
                            | Name | Age | City |
                            |------|-----|------|
                            | John | 25 | New York |
                            | Jane | 30 | **San Francisco** |
                            | Bob | *22* | [Chicago](https://chicago.com) |
                            
                            
                        """.trimIndent(),
                            modifier = Modifier.padding(innerPadding),
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeMarkdownTheme {
        Greeting("Android")
    }
}