package com.iffly.compose.markdown.samples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig

@Composable
fun TableAndCodeExample(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        MarkdownView(
            content = """
                # Table and Code Examples
                
                ## Complex Table
                
                | Feature | Android | iOS | Web | Description |
                |---------|:-------:|:---:|:---:|-------------|
                | Basic Rendering | ✅ | ✅ | ✅ | Supports standard Markdown syntax |
                | Table Support | ✅ | ✅ | ✅ | GFM table extension |
                | Code Highlighting | ✅ | ❌ | ✅ | Syntax highlighting display |
                | Image Rendering | ✅ | ✅ | ✅ | Local and network images |
                | Custom Styling | ✅ | ✅ | ✅ | Fully customizable |
                
                ## Multiple Programming Languages
                
                ### Kotlin Code
                ```kotlin
                class MarkdownRenderer {
                    fun render(content: String): ComposableNode {
                        val parser = Parser.builder().build()
                        val document = parser.parse(content)
                        return convert(document)
                    }
                    
                    private fun convert(node: Node): ComposableNode {
                        return when (node) {
                            is Heading -> HeadingNode(node.level, node.children)
                            is Paragraph -> ParagraphNode(node.children)
                            else -> TextNode(node.literal)
                        }
                    }
                }
                ```
                
                ### JavaScript Code
                ```javascript
                const markdownRenderer = {
                    render(content) {
                        const tokens = this.tokenize(content);
                        return this.parse(tokens);
                    },
                    
                    tokenize(content) {
                        return content.split('\n').map(line => ({
                            type: this.getType(line),
                            content: line
                        }));
                    }
                };
                ```
                
                ### Python Code
                ```python
                class MarkdownProcessor:
                    def __init__(self):
                        self.parser = CommonMark.Parser()
                        
                    def process(self, content: str) -> Dict:
                        ast = self.parser.parse(content)
                        return {
                            'ast': ast,
                            'html': self.to_html(ast),
                            'metadata': self.extract_metadata(ast)
                        }
                        
                    def extract_metadata(self, ast) -> Dict:
                        metadata = {}
                        for node in ast.children:
                            if node.type == 'heading' and node.level == 1:
                                metadata['title'] = node.text
                        return metadata
                ```
                
                ## Simple Table
                
                | Name | Age |
                |------|-----|
                | John | 25 |
                | Jane | 30 |
                | Bob | 28 |
                
                ## Quoted Code Block
                > ```python
                > def hello_world():
                >     print("Hello, World!")
                >
                
                ## Quoted Table
                > | Product | Price |
                > |---------|-------|
                > | Apple   | $1    |
                > | Banana  | $0.5  |
                > | Cherry  | $2    |
                
                ## Quoted Mixed Content
                > Here is a table:
                >
                > | Feature | Android | iOS | Web | Description |
                > |---------|:-------:|:---:|:---:|-------------|
                > | Basic Rendering | ✅ | ✅ | ✅ | Supports standard Markdown syntax |
                > | Table Support | ✅ | ✅ | ✅ | GFM table extension |
                > | Code Highlighting | ✅ | ❌ | ✅ | Syntax highlighting display |
                > | Image Rendering | ✅ | ✅ | ✅ | Local and network images |
                > | Custom Styling | ✅ | ✅ | ✅ | Fully customizable |
                >> And here is some code:
                >> ```javascript
                >> console.log("Hello from quoted code!");
                >> ```
                
                ## List with Table
                1. First item
                
                   | Task       | Status   |
                   |------------|----------|
                   | Task 1     | Completed|
                   | Task 2     | Pending  |
                 
                2. | Feature | Android | iOS | Web | Description |
                   |---------|:-------:|:---:|:---:|-------------|
                   | Basic Rendering | ✅ | ✅ | ✅ | Supports standard Markdown syntax |
                   | Table Support | ✅ | ✅ | ✅ | GFM table extension |
                   | Code Highlighting | ✅ | ❌ | ✅ | Syntax highlighting display |
                   | Image Rendering | ✅ | ✅ | ✅ | Local and network images |
                   | Custom Styling | ✅ | ✅ | ✅ | Fully customizable |
            """.trimIndent(),
            markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
            modifier = Modifier.padding(16.dp)
        )
    }
}
