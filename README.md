# Compose Markdown


A powerful and highly customizable Jetpack Compose Markdown rendering library that supports rich Markdown syntax and custom styling.

## Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Installation](#-installation)
- [Quick Start](#-quick-start)
- [Core Components](#-core-components)
- [Style Customization](#-style-customization)
- [Advanced Features](#-advanced-features)
- [API Reference](#-api-reference)
- [FAQ](#-faq)
- [Contributing](#-contributing)
- [License](#-license)

## 📋 Features

### Core Features
- ✅ **Standard Markdown Support** - Full support for CommonMark specification
- ✅ **Extended Syntax** - Support for GFM (GitHub Flavored Markdown) tables
- ✅ **Code Syntax Highlighting** - Multi-language code block syntax highlighting
- ✅ **Multimedia Support** - Rendering of images, links and other multimedia content
- ✅ **Responsive Design** - Perfect adaptation to different screen sizes

### Performance & Extensions
- ⚡ **Async Parsing** - Background thread parsing ensures UI fluidity
- 🎨 **Fully Customizable** - Support for custom styles, renderers and parsers
- 🔌 **Plugin System** - Flexible plugin architecture for feature extensions
- 🛡️ **Error Handling** - Graceful error state handling mechanism

## 🔧 Tech Stack

| Technology | Version | Purpose |
|------|------|------|
| **Jetpack Compose** | 2024.09.00+ | Modern UI framework |
| **CommonMark** | Latest | Markdown parsing engine |
| **Kotlin Coroutines** | 1.7+ | Asynchronous processing |
| **Material Design 3** | Latest | Design language specification |

## 📦 Installation

### System Requirements

- **Android API**: 24+ (Android 7.0)
- **Kotlin**: 2.0.21+
- **Compose BOM**: 2024.09.00+
- **Java**: 8+

### Add Dependency

Add the dependency to your project's `build.gradle.kts` (Module: app):

```kotlin
dependencies {
    implementation(project(":markdown"))
    
    // If installing from remote repository (future version)
    // implementation("com.iffly.compose:markdown:1.0.0")
}
```

## 🚀 Quick Start

### Basic Usage

The simplest way to use:

```kotlin
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig

@Composable
fun SimpleMarkdownExample() {
    val markdownContent = """
        # Welcome to Compose Markdown
        
        This is a powerful Markdown rendering library.
        
        ## Supported Features
        
        - **Bold text**
        - *Italic text*
        - `Inline code`
        - [Link](https://github.com)
        
        ### Code Block Example
        
        ```kotlin
        @Composable
        fun HelloWorld() {
            Text("Hello, Compose Markdown!")
        }
        ```
    """.trimIndent()
    
    MarkdownView(
        content = markdownContent,
        modifier = Modifier.fillMaxSize()
    )
}
```

### Usage with Configuration

```kotlin
@Composable
fun ConfiguredMarkdownExample() {
    val config = MarkdownRenderConfig.Builder()
        .typographyStyle(
            TypographyStyle(
                textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
                head = mapOf(
                    1 to SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    2 to SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                )
            )
        )
        .build()
    
    MarkdownView(
        content = "# Custom Style Title\n\nThis is Markdown content with custom styling.",
        markdownRenderConfig = config,
        linkInteractionListener = LinkInteractionListener { url ->
            // Handle link click events
            println("Link clicked: $url")
        },
        onError = { error ->
            // Custom error handling
            Text(
                text = "Content parsing failed: ${error.message}",
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}
```

## 🧩 Core Components

### MarkdownView

The main Composable component for rendering Markdown content.

```kotlin
@Composable
fun MarkdownView(
    content: String,
    modifier: Modifier = Modifier,
    markdownRenderConfig: MarkdownRenderConfig = MarkdownRenderConfig.Builder().build(),
    linkInteractionListener: LinkInteractionListener? = null,
    onError: @Composable (Throwable) -> Unit = { DefaultErrorContent(it) }
)
```

### MarkdownRenderConfig

Configuration class for customizing Markdown rendering behavior.

```kotlin
class MarkdownRenderConfig private constructor(
    val typographyStyle: TypographyStyle,
    val blockRenderers: Map<Class<*>, IBlockRenderer<*>>,
    val inlineNodeStringBuilders: Map<Class<*>, IInlineNodeStringBuilder<*>>,
    val plugins: List<Plugin>
) {
    class Builder {
        fun typographyStyle(style: TypographyStyle): Builder
        fun addBlockRenderer(nodeClass: Class<*>, renderer: IBlockRenderer<*>): Builder
        fun addInlineNodeStringBuilder(nodeClass: Class<*>, builder: IInlineNodeStringBuilder<*>): Builder
        fun addPlugin(plugin: Plugin): Builder
        fun build(): MarkdownRenderConfig
    }
}
```

## 🎨 Style Customization

### Basic Style Configuration

```kotlin
val customTypography = TypographyStyle(
    textStyle = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontFamily = FontFamily.Default
    ),
    body = SpanStyle(color = MaterialTheme.colorScheme.onSurface),
    strongEmphasis = SpanStyle(
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    ),
    emphasis = SpanStyle(
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    ),
    code = SpanStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.secondary,
        background = MaterialTheme.colorScheme.surfaceVariant
    )
)
```

### Heading Style Customization

```kotlin
val headingStyles = mapOf(
    1 to SpanStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    ),
    2 to SpanStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    ),
    3 to SpanStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    ),
    // ... other levels
)
```

### Link Style Configuration

```kotlin
val linkStyles = TextLinkStyles(
    style = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline
    ),
    hoveredStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        textDecoration = TextDecoration.Underline
    ),
    pressedStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        textDecoration = TextDecoration.Underline
    )
)
```

## 🔧 Advanced Features

### Custom Block Renderer

Create custom block-level element renderers:

```kotlin
class AlertBlockRenderer : IBlockRenderer<AlertBlock> {
    @Composable
    override fun Invoke(node: AlertBlock, modifier: Modifier) {
        val (backgroundColor, contentColor, icon) = when (node.alertType) {
            AlertBlock.TYPE_INFO -> Triple(
                Color(0xFFE3F2FD),
                Color(0xFF1976D2),
                Icons.Default.Info
            )
            AlertBlock.TYPE_WARNING -> Triple(
                Color(0xFFFFF3E0),
                Color(0xFFE65100),
                Icons.Default.Warning
            )
            AlertBlock.TYPE_SUCCESS -> Triple(
                Color(0xFFE8F5E8),
                Color(0xFF2E7D32),
                Icons.Default.CheckCircle
            )
            AlertBlock.TYPE_ERROR -> Triple(
                Color(0xFFFFEBEE),
                Color(0xFFC62828),
                Icons.Default.Error
            )
            else -> Triple(
                Color(0xFFF5F5F5),
                Color(0xFF424242),
                Icons.Default.Info
            )
        }

        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = node.alertType,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val nodeTitle = node.title
                    if (!nodeTitle.isNullOrBlank()) {
                        Text(
                            text = nodeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (node.content.isNotBlank()) {
                        Text(
                            text = node.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
```

### Custom Inline Node Builder

```kotlin
class MentionNodeStringBuilder : IInlineNodeStringBuilder<MentionNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: MentionNode,
        inlineContentMap: MutableMap<String, InlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int
    ) {
        withStyle(
            SpanStyle(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Medium,
                background = Color(0xFFE3F2FD)
            )
        ) {
            append("@${node.username}")
        }
    }
}
```

### Custom Node Definitions

```kotlin
/**
 * Alert block node
 * Syntax: :::type[title] content :::
 */
class AlertBlock : CustomBlock() {
    var alertType: String = TYPE_INFO
    var title: String? = null
    var content: String = ""

    companion object {
        const val TYPE_INFO = "info"
        const val TYPE_WARNING = "warning"
        const val TYPE_SUCCESS = "success"
        const val TYPE_ERROR = "error"
    }
}

/**
 * Mention node
 * Syntax: @username
 */
class MentionNode : Node() {
    var username: String = ""
    
    override fun accept(visitor: Visitor?) {
        visitor?.visit(this)
    }
}

/**
 * Hashtag node
 * Syntax: #hashtag
 */
class HashtagNode : Node() {
    var hashtag: String = ""
    
    override fun accept(visitor: Visitor?) {
        visitor?.visit(this)
    }
}

/**
 * Highlight text node
 * Syntax: ==highlight text==
 */
class HighlightNode : Node() {
    var highlightText: String = ""
    
    override fun accept(visitor: Visitor?) {
        visitor?.visit(this)
    }
}

/**
 * Badge node
 * Syntax: !!badge:text!!
 */
class BadgeNode : Node() {
    var badgeText: String = ""
    var badgeType: String = "default"
    
    override fun accept(visitor: Visitor?) {
        visitor?.visit(this)
    }
}
```

### Register Custom Components

```kotlin
val config = MarkdownRenderConfig.Builder()
    .typographyStyle(customTypography)
    .addBlockRenderer(AlertBlock::class.java, AlertBlockRenderer())
    .addInlineNodeStringBuilder(MentionNode::class.java, MentionNodeStringBuilder())
    .addInlineNodeStringBuilder(HashtagNode::class.java, HashtagNodeStringBuilder())
    .addInlineNodeStringBuilder(HighlightNode::class.java, HighlightNodeStringBuilder())
    .addInlineNodeStringBuilder(BadgeNode::class.java, BadgeNodeStringBuilder())
    .build()
```

## 📚 API Reference

### Main Interfaces

#### IBlockRenderer<T>
```kotlin
interface IBlockRenderer<T : Node> {
    @Composable
    fun Invoke(node: T, modifier: Modifier = Modifier)
}
```

#### IInlineNodeStringBuilder<T>
```kotlin
interface IInlineNodeStringBuilder<T : Node> {
    fun AnnotatedString.Builder.buildInlineNodeString(
        node: T,
        inlineContentMap: MutableMap<String, InlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int
    )
}
```

#### LinkInteractionListener
```kotlin
fun interface LinkInteractionListener {
    fun onLinkClicked(url: String)
}
```

### Style Classes

#### TypographyStyle
```kotlin
data class TypographyStyle(
    val textStyle: TextStyle = TextStyle.Default,
    val body: SpanStyle = SpanStyle(),
    val strongEmphasis: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
    val emphasis: SpanStyle = SpanStyle(fontStyle = FontStyle.Italic),
    val code: SpanStyle = SpanStyle(fontFamily = FontFamily.Monospace),
    val link: TextLinkStyles = TextLinkStyles(),
    val tableHeader: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
    val tableCell: SpanStyle = SpanStyle(),
    val head: Map<Int, SpanStyle> = emptyMap()
)
```

## ❓ FAQ

### Q: How to handle performance issues with large Markdown documents?
A: The library uses an asynchronous parsing mechanism that processes Markdown parsing in background threads without blocking the UI thread. For very large documents, pagination or lazy loading is recommended.

### Q: What Markdown extension syntaxes are supported?
A: Currently supports CommonMark standard syntax and GFM tables. More extension syntax support will be added in the future.

### Q: How to customize syntax highlighting for code blocks?
A: You can implement custom syntax highlighting by creating a custom `CodeBlockRenderer` and integrating third-party syntax highlighting libraries.

### Q: How to handle image loading failures?
A: You can configure default placeholder styles in `TypographyStyle` or implement a custom image renderer.

## 🤝 Contributing

We welcome community contributions! Please check [CONTRIBUTING.md](CONTRIBUTING.md) for detailed contribution guidelines.

### Development Process

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Create a Pull Request

### Code Standards

- Follow Kotlin official coding style
- Add appropriate documentation comments
- Write unit tests
- Ensure all tests pass

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [CommonMark](https://commonmark.org/) - Markdown parsing engine
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI toolkit
- All contributors and community members

---

<div align="center">

**[⬆ Back to top](#compose-markdown)**

Made with ❤️ by the Compose Markdown team

</div>
