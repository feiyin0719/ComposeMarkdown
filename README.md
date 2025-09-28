# Compose Markdown

[English](README.md) | [简体中文](README_zh-CN.md)

A powerful and highly customizable Jetpack Compose Markdown rendering library that supports rich
Markdown syntax and custom styling.

## Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Installation](#-installation)
- [Quick Start](#-quick-start)
- [Core Components](#-core-components)
- [Style Customization](#-style-customization)
- [Advanced Features](#-advanced-features)
- [Plugins](#-plugins)
- [API Reference](#-api-reference)
- [FAQ](#-faq)
- [Contributing](#-contributing)
- [License](#-license)

## 📋 Features

### 📸 Sample Screenshots

| Custom Styles | Tables and Code Blocks |             Custom Plugins (Alerts)             |
| :---: | :---: |:-----------------------------------------------:|
| *Custom typography styles* | *Complex tables and code highlighting* |                 *Custom block*                  |
| ![Custom Style](_posts/images/custom_style.png) | ![Table and Code](_posts/images/table_and_code.png) | ![Custom Alert](_posts/images/custom_alert.png) |

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

| Technology            | Version     | Purpose                       |
|-----------------------|-------------|-------------------------------|
| **Jetpack Compose**   | 2024.09.00+ | Modern UI framework           |
| **Flexmark**          | 0.64.8      | Markdown parsing engine       |
| **Kotlin Coroutines** | 1.7+        | Asynchronous processing       |
| **Material Design 3** | Latest      | Design language specification |

## 📦 Installation

### System Requirements

- **Android API**: 24+ (Android 7.0)
- **Kotlin**: 2.0.21+
- **Compose BOM**: 2024.09.00+
- **Java**: 8+

### Add Dependency

1. Add `jitpack.io` repository to your project:

```kotlin
repositories {
    ...
    maven(url = "https://jitpack.io")
}
```

2. Add the dependency to your project's `build.gradle.kts` :
   define library module in your `./gradle/libs.versions.toml` file:

```toml
[versions]
compose-markdown = "0.0.1"
composeBom = "2024.09.00"
coil = "2.5.0"
[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
compose-markdown = { group = "com.github.feiyin0719", name = "ComposeMarkdown", version.ref = "compose-markdown" }
```

add the dependency in your module `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.compose.markdown)
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
        fun addInlineNodeStringBuilder(
            nodeClass: Class<*>,
            builder: IInlineNodeStringBuilder<*>
        ): Builder
        fun addPlugin(plugin: Plugin): Builder
        fun addExtension(extension: Extension): Builder
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

### MarkdownView Usage Modes

MarkdownView provides three different usage modes to adapt to different use cases:

#### 1. Synchronous Parsing Version (Instant Parsing)

Suitable for small content that can be parsed instantly without blocking the UI.

```kotlin
@Composable
fun MarkdownView(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    linkInteractionListener: LinkInteractionListener? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
)
```

**Usage Example:**

```kotlin
@Composable
fun SyncMarkdownExample() {
    val shortContent = """
        # Quick Notes
        This is short markdown content that can be parsed instantly.
        - Item 1
        - Item 2
        
        **Bold text** and *italic text*
    """.trimIndent()

    MarkdownView(
        content = shortContent,
        markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
        modifier = Modifier.padding(16.dp),
        linkInteractionListener = LinkInteractionListener { url ->
            // Handle link clicks
            Log.d("MarkdownView", "Link clicked: $url")
        },
        onError = { error ->
            Text(
                text = "Parsing failed: ${error.message}",
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}
```

#### 2. Asynchronous Parsing Version (Background Parsing)

Recommended for large content or scenarios requiring loading/error state display.

```kotlin
@Composable
fun MarkdownView(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    linkInteractionListener: LinkInteractionListener? = null,
    parseDispatcher: CoroutineDispatcher? = null,
    onLoading: (@Composable () -> Unit)? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
)
```

**Usage Example:**

```kotlin
@Composable
fun AsyncMarkdownExample() {
    val largeContent = """
        # Large Document
        This is a large markdown document that may take time to parse.
        
        ## Features
        
        ${generateLargeMarkdownContent()}
    """.trimIndent()

    MarkdownView(
        content = largeContent,
        markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
        modifier = Modifier.fillMaxSize(),
        linkInteractionListener = LinkInteractionListener { url ->
            when {
                url.startsWith("mailto:") -> {
                    // Handle email links
                    val email = url.removePrefix("mailto:")
                    openEmailClient(email)
                }
                url.startsWith("tel:") -> {
                    // Handle phone links
                    val phone = url.removePrefix("tel:")
                    openDialer(phone)
                }
                else -> {
                    // Handle web links
                    openWebBrowser(url)
                }
            }
        },
        parseDispatcher = Dispatchers.IO,
        onLoading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Parsing markdown content...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        onError = { error ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Parse Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error.message ?: "Unknown error occurred",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    )
}
```

#### 3. Pre-parsed Node Version

Suitable for cases where you already have parsed Nodes.

```kotlin
@Composable
fun MarkdownView(
    node: Node,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    linkInteractionListener: LinkInteractionListener? = null,
)
```

**Usage Example:**

```kotlin
@Composable
fun PreParsedMarkdownExample() {
    val parser = MarkdownRenderConfig.Builder().build().parser
    val preParseNode = remember {
        parser.parse("# Pre-parsed Content\n\nThis content was parsed outside the composable.")
    }

    MarkdownView(
        node = preParseNode,
        markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
        modifier = Modifier.padding(16.dp),
        linkInteractionListener = LinkInteractionListener { url ->
            // Handle link clicks
        }
    )
}
```

### Performance Optimization Recommendations

#### 1. Choose the Right Version

- **Small content** (< 1KB): Use synchronous version to avoid unnecessary loading states
- **Large content**: Use asynchronous version to ensure UI fluidity

#### 2. Custom Dispatcher

```kotlin
val customDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

MarkdownView(
    content = largeContent,
    parseDispatcher = customDispatcher,
    // ... other parameters
)
```

#### 3. Node Caching

```kotlin
val nodeCache = remember { mutableMapOf<String, Node>() }
val cachedNode = nodeCache.getOrPut(contentKey) {
    parser.parse(content)
}

MarkdownView(node = cachedNode, ...)
```

#### 4. Memory Management

For very large documents, consider implementing pagination or virtual scrolling.

### MarkdownPlugin Usage

MarkdownPlugin is a powerful plugin system that allows you to extend Markdown parsing and rendering
functionality. By implementing the `IMarkdownRenderPlugin` interface, you can add custom block-level
elements, inline elements, and renderers.

#### Creating Custom Plugins

```kotlin
class CustomMarkdownPlugin : AbstractMarkdownRenderPlugin() {

    // Register custom block parser factories
    override fun blockParserFactories(): List<CustomBlockParserFactory> =
        listOf(AlertBlockParserFactory())

    // Register custom inline content parser factories
    override fun inlineContentParserFactories(): List<InlineParserExtensionFactory> = listOf(
        MentionInlineParserFactory(),
        HashtagInlineParserFactory(),
        BadgeInlineParserFactory(),
        HighlightInlineParserFactory()
    )

    // Register custom block renderers
    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<out Block>> =
        mapOf(AlertBlock::class.java to AlertBlockRenderer())

    // Register custom inline node string builders
    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<out Node>> =
        mapOf(
            MentionNode::class.java to MentionNodeStringBuilder(),
            HashtagNode::class.java to HashtagNodeStringBuilder(),
            HighlightNode::class.java to HighlightNodeStringBuilder(),
            BadgeNode::class.java to BadgeNodeStringBuilder()
        )
}
```

#### Using Custom Plugins

```kotlin
@Composable
fun PluginMarkdownExample() {
    val markdownContent = """
        :::info Tips
        This is an information alert box
        :::
        
        @username mentioned you!
        
        #hashtag makes content easier to categorize
        
        ==highlighted text== emphasizes key content
        
        !!success:Success!! status badge
    """.trimIndent()

    val config = MarkdownRenderConfig.Builder()
        .plugin(CustomMarkdownPlugin())
        .build()

    MarkdownView(
        content = markdownContent,
        markdownRenderConfig = config,
        modifier = Modifier.padding(16.dp)
    )
}
```

#### Supported Extended Syntax

Through `CustomMarkdownPlugin`, you can use the following extended syntax:

- **Alert Blocks**: `:::info Title` `Content` `:::`
- **User Mentions**: `@username`
- **Hashtags**: `#hashtag`
- **Highlighted Text**: `==highlighted content==`
- **Badges**: `!!type:text!!`

### Custom Block Renderer

Create custom block-level element renderers:

```kotlin
class AlertBlockRenderer : IBlockRenderer<AlertBlock> {
    @Composable
    override fun Invoke(node: AlertBlock, modifier: Modifier) {
        val (icon, containerColor, contentColor) = when (node.alertType) {
            AlertBlock.TYPE_INFO -> Triple(Icons.Default.Info, Color(0xFFE3F2FD), Color(0xFF1976D2))
            AlertBlock.TYPE_WARNING -> Triple(
                Icons.Default.Warning,
                Color(0xFFFFF8E1),
                Color(0xFFF57C00)
            )
            AlertBlock.TYPE_SUCCESS -> Triple(
                Icons.Default.CheckCircle,
                Color(0xFFE8F5E8),
                Color(0xFF2E7D32)
            )
            AlertBlock.TYPE_ERROR -> Triple(
                Icons.Default.Delete,
                Color(0xFFFFEBEE),
                Color(0xFFD32F2F)
            )
            else -> Triple(Icons.Default.Info, Color(0xFFE3F2FD), Color(0xFF1976D2))
        }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = RoundedCornerShape(8.dp)
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
                    // Use MarkdownText to render child nodes
                    MarkdownText(node)
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
        inlineContentMap: MutableMap<String, androidx.compose.foundation.text.InlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int
    ) {
        pushStyle(
            SpanStyle(
                color = Color(0xFF1976D2),
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium
            )
        )
        append("@${node.username}")
        pop()
    }
}
```

### Custom Node Definitions

```kotlin
/**
 * Alert block node
 * Syntax: :::type title
 * content
 * :::
 */
class AlertBlock : Block() {
    var alertType: String = TYPE_INFO
    var title: String? = null
    // Content is stored as child nodes, not as a string property

    override fun getSegments(): Array<BasedSequence> = emptyArray()

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
class MentionNode(private val seq: BasedSequence) : Node() {
    var username: String = seq.subSequence(1, seq.length).toString()
    override fun getSegments(): Array<BasedSequence> = arrayOf(seq)
}

/**
 * Hashtag node
 * Syntax: #hashtag
 */
class HashtagNode(private val seq: BasedSequence) : Node() {
    var hashtag: String = seq.subSequence(1, seq.length).toString()
    override fun getSegments(): Array<BasedSequence> = arrayOf(seq)
}

/**
 * Highlight text node
 * Syntax: ==highlight text==
 */
class HighlightNode : Node() {
    var highlightText: String = ""
    override fun getSegments(): Array<BasedSequence> = emptyArray()
}

/**
 * Badge node
 * Syntax: !!type:text!!
 */
class BadgeNode(private val seq: BasedSequence, var badgeType: String, var badgeText: String) :
    Node() {
    override fun getSegments(): Array<BasedSequence> = arrayOf(seq)
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

### Custom Image Loader

The library uses Coil for image loading by default. You can refer to Coil's documentation to
customize image loading behavior -- [coil](https://coil-kt.github.io/coil/image_loaders/)

## 📚 API Reference

### Main Interfaces

#### MarkdownView

Three overloads of the `MarkdownView` Composable function:

- Synchronous version

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

- Asynchronous version

```kotlin
@Composable
fun MarkdownView(
    content: String,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    linkInteractionListener: LinkInteractionListener? = null,
    parseDispatcher: CoroutineDispatcher? = null,
    onLoading: @Composable (() -> Unit)? = null,
    onError: @Composable (Throwable) -> Unit = { DefaultErrorContent(it) }
)
```

- Pre-parsed Node version

```kotlin
@Composable
fun MarkdownView(
    node: Node,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    linkInteractionListener: LinkInteractionListener? = null,
)
```

#### LazyMarkdownView

The `LazyMarkdownView` is designed for efficiently rendering large Markdown files by loading and
displaying content in chunks as the user scrolls. This component is perfect for documents that are
too large to load entirely into memory at once.

**Key Features:**

- 📄 **Chunk-based Loading** - Loads Markdown content progressively as needed
- ⚡ **Memory Efficient** - Only keeps visible and nearby chunks in memory
- 🎯 **Smart Prefetching** - Prefetches content based on scroll direction
- 🔄 **Background Parsing** - Parses content on background threads
- 📱 **Smooth Scrolling** - Built-in LazyColumn with optimized prefetching

```kotlin
@Composable
fun LazyMarkdownView(
    file: File,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    showNotSupportedText: Boolean = false,
    linkInteractionListener: LinkInteractionListener? = null,
    chunkLoaderConfig: ChunkLoaderConfig = ChunkLoaderConfig(parserDispatcher = MarkdownThreadPool.dispatcher),
    nestedPrefetchItemCount: Int = 3,
)
```

**Parameters:**

- `file` - The Markdown file to be displayed
- `markdownRenderConfig` - Configuration for rendering the Markdown
- `modifier` - Modifier to be applied to the LazyColumn
- `showNotSupportedText` - Whether to show text for unsupported elements
- `linkInteractionListener` - Listener for link interactions
- `chunkLoaderConfig` - Configuration for the chunk loader (see ChunkLoaderConfig below)
- `nestedPrefetchItemCount` - Number of items to prefetch for smoother scrolling

**Usage Example:**

```kotlin
@Composable
fun LargeMarkdownDocument() {
    val markdownFile = File("/path/to/large-document.md")
    val config = MarkdownRenderConfig.Builder().build()

    LazyMarkdownView(
        file = markdownFile,
        markdownRenderConfig = config,
        modifier = Modifier.fillMaxSize(),
        chunkLoaderConfig = ChunkLoaderConfig(
            initialLines = 1000,
            incrementalLines = 500,
            chunkSize = 5
        )
    )
}
```

**When to Use LazyMarkdownView:**

- Large Markdown files (>10MB or >10000 lines)
- Documents with many images or complex content
- Mobile devices with limited memory
- When you need responsive scrolling performance

#### ChunkLoaderConfig

Configuration class for controlling how `LazyMarkdownView` loads and caches content:

```kotlin
data class ChunkLoaderConfig(
    val initialLines: Int = 1000,           // Initial number of lines to load
    val incrementalLines: Int = 500,        // Lines to load when expanding
    val maxCachedChunks: Int = 1000,        // Maximum chunks to keep in memory
    val maxCachedFileLines: Int = 2000,     // Maximum file lines to cache
    val chunkSize: Int = 5,                 // Number of blocks per chunk
    val parserDispatcher: CoroutineDispatcher = Dispatchers.Default,  // Background parsing
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO           // File I/O operations
)
```

**Configuration Tips:**

- Increase `initialLines` for faster initial loading of small-medium files
- Increase `incrementalLines` for smoother expansion when scrolling
- Decrease `chunkSize` for more granular loading and better memory usage
- Use `MarkdownThreadPool.dispatcher` for `parserDispatcher` to avoid blocking UI
- Adjust `maxCachedChunks`, `maxCachedFileLines` based on available memory

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
        indentLevel: Int,
        isShowNotSupported: Boolean,
        inlineNodeStringBuilders: InlineNodeStringBuilders,
    )
}
```

#### LinkInteractionListener

handle link click events

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

### MarkdownRenderConfig

```kotlin
class MarkdownRenderConfig private constructor(
    val typographyStyle: TypographyStyle,
    val blockRenderers: Map<Class<*>, IBlockRenderer<*>>,
    val inlineNodeStringBuilders: Map<Class<*>, IInlineNodeStringBuilder<*>>,
    val plugins: List<Plugin>,
    val parser: Parser
) {
    class Builder {
        fun typographyStyle(style: TypographyStyle): Builder
        fun addBlockRenderer(nodeClass: Class<*>, renderer: IBlockRenderer<*>): Builder
        fun addInlineNodeStringBuilder(
            nodeClass: Class<*>,
            builder: IInlineNodeStringBuilder<*>
        ): Builder
        fun addPlugin(plugin: Plugin): Builder
        fun build(): MarkdownRenderConfig
    }
}
```

### Markdown Plugins

```kotlin
interface IMarkdownRenderPlugin {
    fun blockParserFactories(): List<CustomBlockParserFactory> = emptyList()
    fun inlineContentParserFactories(): List<InlineParserExtensionFactory> = emptyList()
    fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<out Block>> = emptyMap()
    fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<out Node>> =
        emptyMap()
}
```

## Future Plans

~~- 🚀 Support load large markdown file and render progressively
Load and render visible blocks to improve performance and memory usage~~  -- Completed in v0.0.4

- Support markdown inline editing mode(inline edit is edit markdown and render at the same time) -- Planned for v0.1.0

- Supports jump-to-section functionality via clickable TOC.

- 🚀 Add more built-in plugins for common use cases

## ❓ FAQ

### Q: How to handle performance issues with large Markdown documents?

A: The library uses an asynchronous parsing mechanism that processes Markdown parsing in background
threads without blocking the UI thread. For very large documents, pagination or lazy loading is
recommended.

### Q: What Markdown extension syntaxes are supported?

A: Currently supports CommonMark standard syntax and GFM tables. More extension syntax support will
be added in the future.

### Q: How to customize syntax highlighting for code blocks?

A: You can implement custom syntax highlighting by creating a custom `CodeBlockRenderer` and
integrating third-party syntax highlighting libraries.

## 🤝 Contributing

We welcome contributions! To get started:
1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Make your changes (keep scope focused; add tests when possible)
4. Run checks locally (examples):
   - `./gradlew build` – compile & run tests
   - `./gradlew lintKotlin detekt` (if configured)
5. Commit using conventional prefixes:
   - `feat:` new feature
   - `fix:` bug fix
   - `docs:` documentation changes
   - `refactor:` code restructure without behavior change
   - `perf:` performance improvement
   - `test:` adding / improving tests
   - `build:` build system / dependency changes
   - `chore:` maintenance tasks
6. Open a Pull Request describing:
   - What & why
   - Screenshots (UI changes) / benchmarks (perf changes)
   - Related issue IDs (e.g. `Closes #12`)

Code Style & Guidelines:
- Prefer small, composable functions
- Avoid premature optimization—measure first
- Keep public APIs documented with KDoc
- Use meaningful, concise commit messages

Issue Reports:
- Provide reproduction steps
- Attach minimal markdown sample content triggering the issue
- Include device / emulator API level & library version

## 📄 License

Released under the MIT License.

```
MIT License

Copyright (c) 2025 Compose Markdown Authors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<div align="center">

**[⬆ Back to top](#compose-markdown)**

Made with ❤️ by the Compose Markdown team

</div>
