package com.iffly.compose.markdown

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.ui.theme.ComposeMarkdownTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeMarkdownTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState()),

                        ) {
                        MarkdownView(
                            linkInteractionListener = {
                                Log.i("MainActivity", "Clicked link: $it")
                            },
                            markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
                            content = """
                            # Hello World
                            This is a simple markdown example.```val greeting = "Hello, World!"```
                            ## Subheading
                            
                            1. Item 1
                               1. Item 1.1
                               2. Item 1.2
                            2. Item 2
                            3. Item 3
                            
                            - Item A
                            - Item B
                            - Item C
                            
                            **Bold Text**
                            
                            *Italic Text*![Image](https://raw.githubusercontent.com/feiyin0719/AFreeSvg/dev/test.jpg)
                            
                            ![Image](https://raw.githubusercontent.com/feiyin0719/AFreeSvg/dev/test.jpg)
                            
                            [Link to Google](https://www.google.com)
                                                        
                            | :Name | Age | City |
                            :---    |   :---:   |   ---:|---|
                            | John czczxcxzcxzcxzcxzcxzczxczxcxzcxzcxzcxzcxzcxzcxz | 30jkhjkhbjkbnkjbjkbj,bmjbjkbkjbjknjkbjkbjkjbkj  | New York huanghou jkljklnknjkn,mdsadasdasdasdasfasfdsfdsfdsfdsfdsfsdfdsfdsfdsfsdcsczcxzcxzcxzcz |
                            | Jane  | 25  | San Francisco |
                            
                            new two columns table
                            | Name | Age |
                            |------|-----|
                            | John | 25  |
                            
                             ```kotlin
                            fun main() {
                                println("Hello, World!")
                                val list = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
                                list.forEach { println(it) }
                            }
                            ```
                            
                            ```javascript
                            function greet(name) {
                                console.log(`Hello, ${'$'}{name}!`);
                            }
                            
                            greet('World');
                            ```
                            This is an indented code block:
        
                                // This is an indented code block
                                val x = 10
                                val y = 20
                                println("Sum: ${'$'}{x + y}")
                            
                        """.trimIndent(),
                            modifier = Modifier.padding(PaddingValues(horizontal = 12.dp)),
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