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
                # 表格和代码示例
                
                ## 复杂表格
                
                | 功能 | Android | iOS | Web | 描述 |
                |------|:-------:|:---:|:---:|------|
                | 基础渲染 | ✅ | ✅ | ✅ | 支持标准Markdown语法 |
                | 表格支持 | ✅ | ✅ | ✅ | GFM表格扩展 |
                | 代码高亮 | ✅ | ❌ | ✅ | 语法高亮显示 |
                | 图片渲染 | ✅ | ✅ | ✅ | 本地和网络图片 |
                | 自定义样式 | ✅ | ✅ | ✅ | 完全可定制 |
                
                ## 多种编程语言
                
                ### Kotlin 代码
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
                
                ### JavaScript 代码
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
                
                ### Python 代码
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
                
                ## 简单表格
                
                | 名称 | 年龄 |
                |------|------|
                | 张三 | 25 |
                | 李四 | 30 |
                | 王五 | 28 |
            """.trimIndent(),
            markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
            modifier = Modifier.padding(16.dp)
        )
    }
}
