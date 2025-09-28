# Compose Markdown

[English](README.md) | 简体中文

一个强大且高度可定制的 Jetpack Compose Markdown 渲染库，支持丰富的 Markdown 语法与自定义样式。

## 目录

- [功能](#-功能)
- [技术栈](#-技术栈)
- [安装](#-安装)
- [快速开始](#-快速开始)
- [核心组件](#-核心组件)
- [样式自定义](#-样式自定义)
- [高级特性](#-高级特性)
- [插件](#-插件)
- [API 参考](#-api-参考)
- [常见问题](#-常见问题)
- [贡献](#-贡献)
- [许可证](#-许可证)

## 📋 功能

### 📸 示例截图

| 自定义样式 | 表格与代码块 | 自定义插件（提示框） |
| :---: | :---: | :---: |
| 自定义排版样式 | 复杂表格与代码高亮 | 自定义块级组件 |
| ![Custom Style](_posts/images/custom_style.png) | ![Table and Code](_posts/images/table_and_code.png) | ![Custom Alert](_posts/images/custom_alert.png) |

### 核心能力

- ✅ 标准 Markdown 支持——完整兼容 CommonMark 规范
- ✅ 扩展语法——支持 GFM（GitHub Flavored Markdown）表格
- ✅ 代码语法高亮——多语言代码块高亮
- ✅ 多媒体支持——渲染图片、链接等富内容
- ✅ 响应式设计——良好适配不同屏幕尺寸

### 性能与扩展

- ⚡ 异步解析——后台线程解析，保证 UI 流畅
- 🎨 完全可定制——支持自定义样式、渲染器与解析器
- 🔌 插件系统——灵活的插件架构，便于功能扩展
- 🛡️ 错误处理——完善的错误状态处理机制

## 🔧 技术栈

| 技术 | 版本 | 作用 |
|------|------|------|
| Jetpack Compose | 2024.09.00+ | 现代化 UI 框架 |
| Flexmark | 0.64.8 | Markdown 解析引擎 |
| Kotlin 协程 | 1.7+ | 异步处理 |
| Material Design 3 | 最新 | 设计规范 |

## 📦 安装

### 系统要求

- Android API：24+（Android 7.0）
- Kotlin：2.0.21+
- Compose BOM：2024.09.00+
- Java：8+

### 添加依赖

1. 在项目中添加 `jitpack.io` 仓库：

```kotlin
repositories {
    ...
    maven(url = "https://jitpack.io")
}
```

2. 在项目的 `./gradle/libs.versions.toml` 中定义依赖：

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

在模块 `build.gradle.kts` 中添加依赖：

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

## 🚀 快速开始

### 基本用法

最简单的使用方式：

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

### 自定义配置用法

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
            // 处理链接点击事件
            println("Link clicked: $url")
        },
        onError = { error ->
            // 自定义错误处理
            Text(
                text = "内容解析失败: ${error.message}",
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}
```

## 🧩 核心组件

### MarkdownView

用于渲染 Markdown 内容的主 Composable 组件。

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

用于自定义 Markdown 渲染行为的配置类。

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

## 🎨 样式自定义

### 基本样式配置

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

### 标题样式配置

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
    // ... 其他级别
)
```

### 链接样式配置

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

## 🔧 高级特性

### MarkdownView 四种用法模式

#### 1. 同步解析（即时解析）

适用于小体量内容，解析瞬时完成且不会阻塞 UI。

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

用法示例：

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
            // 处理链接点击
            Log.d("MarkdownView", "Link clicked: $url")
        },
        onError = { error ->
            Text(
                text = "解析失败: ${error.message}",
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}
```

#### 2. 异步解析（后台解析）

推荐用于大体量内容或需要显示加载/错误状态的场景。

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

用法示例：

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
                    // 处理邮件链接
                    val email = url.removePrefix("mailto:")
                    openEmailClient(email)
                }
                url.startsWith("tel:") -> {
                    // 处理电话链接
                    val phone = url.removePrefix("tel:")
                    openDialer(phone)
                }
                else -> {
                    // 处理网页链接
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
                        text = "正在解析 Markdown 内容...",
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
                            text = "解析错误",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error.message ?: "发生未知错误",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    )
}
```

#### 3. 预解析节点版本

适用于已提前解析得到 Node 的场景。

```kotlin
@Composable
fun MarkdownView(
    node: Node,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    linkInteractionListener: LinkInteractionListener? = null,
)
```

用法示例：

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
            // 处理链接点击
        }
    )
}
```

#### 4. 懒加载大文件（LazyMarkdownView）

当需要展示超大 Markdown 文件（例如：> 1~2 MB、上万行、包含大量图片或代码块）时，使用普通 `MarkdownView` 即使异步解析也可能出现：

- 首次解析耗时长，白屏时间增加
- 一次性构建所有节点导致内存占用高
- 滚动时可能出现掉帧

`LazyMarkdownView` 通过“按需分块解析 + 懒加载渲染”策略解决上述问题：

特点：
- 分块解析：只解析当前可见区域附近的 Markdown 片段
- 后台线程：解析与文件读取在独立线程池执行，UI 流畅
- 滚动感知：根据滚动方向预取后续或前一部分内容
- 内存友好：可通过配置限制缓存块/缓存行数

核心 API：
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

基础示例：
```kotlin
@Composable
fun LazyLargeDocExample() {
    // 真实项目中可将网络/Assets 中的内容先写入 cacheFile 再传入
    val context = LocalContext.current
    val cacheFile = remember {
        File(context.cacheDir, "large_article.md").apply {
            if (!exists()) {
                writeText(generateOrWriteLargeContent()) // 示例：生成或写入超大内容
            }
        }
    }
    val config = remember { MarkdownRenderConfig.Builder().build() }

    LazyMarkdownView(
        file = cacheFile,
        markdownRenderConfig = config,
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        chunkLoaderConfig = ChunkLoaderConfig(
            initialLines = 1200,      // 首屏多加载一些，减少首滚动空白
            incrementalLines = 600,   // 每次向前/向后扩展的行数
            chunkSize = 6,            // 一个“渲染块”含多少 markdown 结构节点
            maxCachedChunks = 300,    // 根据内存情况调整
            maxCachedFileLines = 3000 // 限制文本缓存行数
        ),
        linkInteractionListener = LinkInteractionListener { url ->
            // 统一处理链接（外链 / 内部锚点 / tel: / mailto: 等）
        }
    )
}
```

最小配置（用默认分块策略即可）：
```kotlin
LazyMarkdownView(
    file = File(path),
    markdownRenderConfig = MarkdownRenderConfig.Builder().build()
)
```

常见调优参数说明（`ChunkLoaderConfig`）：
- `initialLines`：首屏预加载行数，过小可能导致快速滚动出现短暂空白；过大会拉长首次启动时间
- `incrementalLines`：滚动触发的追加加载行数，越大滚动越顺但单次解析耗时增加
- `chunkSize`：一个渲染块包含的节点数量（语义块，比如段落/标题/列表等），越小越细粒度，越大越少组合开销
- `maxCachedChunks` / `maxCachedFileLines`：限制内存占用，防止长时间阅读后缓存无限增长
- `parserDispatcher`：解析线程调度器；默认使用库内线程池，必要时可自建

适用场景建议：
- 技术文档 / 笔记合集 / 导出 Wiki / AI 生成的海量长文
- 含大量图片（可结合图片懒加载策略）
- 阅读器类 App 中的本地离线 MD 文档

与普通 `MarkdownView` 对比：
| 对比点 | MarkdownView (异步) | LazyMarkdownView |
|--------|---------------------|------------------|
| 首次渲染延迟 | 解析整个文档 | 解析局部（首屏 + 预取） |
| 内存占用 | 全量节点常驻 | 可控（缓存上限） |
| 超大文件可行性 | 易 OOM / 长等待 | 流畅、可扩展 |
| 滚动体验 | 与文档规模相关 | 大文件仍平滑 |

集成提示：
1. 远程 Markdown：先下载写入 `cacheDir` 后再传入 `file`
2. 动态更新：若文件内容发生变化，可变更 `File` 引用或修改时间戳触发重建
3. 锚点跳转：可结合未来的 TOC 支持，通过记录块索引滚动到指定位置
4. 预加载策略：根据用户阅读方向（当前实现已自动推断）进行前瞻解析

> 若你的场景只是在一个会频繁变化的编辑区实时预览，不建议使用 `LazyMarkdownView`，而应考虑增量解析或局部 diff 方案（规划中）。

### 性能优化建议

#### 1. 选择合适的使用方式

- 小内容（< 1KB）：使用同步版本，避免不必要的加载状态
- 大内容：使用异步版本，确保 UI 流畅

#### 2. 自定义 Dispatcher

```kotlin
val customDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

MarkdownView(
    content = largeContent,
    parseDispatcher = customDispatcher,
    // ... 其他参数
)
```

#### 3. 节点缓存

```kotlin
val nodeCache = remember { mutableMapOf<String, Node>() }
val cachedNode = nodeCache.getOrPut(contentKey) {
    parser.parse(content)
}

MarkdownView(node = cachedNode, ...)
```

#### 4. 内存管理

对于超大文档，可考虑分页或虚拟滚动方案。

### Markdown 插件用法

MarkdownPlugin 是一套强大的插件系统，可扩展 Markdown 的解析与渲染能力。通过实现 `IMarkdownRenderPlugin` 接口，你可以添加自定义块级元素、行内元素与对应的渲染器。

#### 自定义插件示例

```kotlin
class CustomMarkdownPlugin : AbstractMarkdownRenderPlugin() {

    // 注册自定义块级解析器工厂
    override fun blockParserFactories(): List<CustomBlockParserFactory> =
        listOf(AlertBlockParserFactory())

    // 注册自定义行内解析扩展工厂
    override fun inlineContentParserFactories(): List<InlineParserExtensionFactory> = listOf(
        MentionInlineParserFactory(),
        HashtagInlineParserFactory(),
        BadgeInlineParserFactory(),
        HighlightInlineParserFactory()
    )

    // 注册自定义块级渲染器
    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<out Block>> =
        mapOf(AlertBlock::class.java to AlertBlockRenderer())

    // 注册自定义行内节点字符串构建器
    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<out Node>> =
        mapOf(
            MentionNode::class.java to MentionNodeStringBuilder(),
            HashtagNode::class.java to HashtagNodeStringBuilder(),
            HighlightNode::class.java to HighlightNodeStringBuilder(),
            BadgeNode::class.java to BadgeNodeStringBuilder()
        )
}
```

#### 使用自定义插件

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
        .addPlugin(CustomMarkdownPlugin())
        .build()

    MarkdownView(
        content = markdownContent,
        markdownRenderConfig = config,
        modifier = Modifier.padding(16.dp)
    )
}
```

#### 支持的扩展语法

通过 `CustomMarkdownPlugin`，你可以使用以下扩展语法：

- 提示块（Alert Blocks）：`:::info Title` `Content` `:::`
- 用户提及：`@username`
- 话题标签：`#hashtag`
- 高亮文本：`==highlighted content==`
- 徽章：`!!type:text!!`

### 自定义块级渲染器

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
                    // 使用 MarkdownText 渲染子节点
                    MarkdownText(node)
                }
            }
        }
    }
}
```

### 自定义行内节点构建器

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

### 自定义节点定义

```kotlin
/**
 * 提示框块级节点
 * 语法：:::type title
 * content
 * :::
 */
class AlertBlock : Block() {
    var alertType: String = TYPE_INFO
    var title: String? = null
    // 内容保存为子节点，而非字符串属性

    override fun getSegments(): Array<BasedSequence> = emptyArray()

    companion object {
        const val TYPE_INFO = "info"
        const val TYPE_WARNING = "warning"
        const val TYPE_SUCCESS = "success"
        const val TYPE_ERROR = "error"
    }
}

/**
 * 提及节点
 * 语法：@username
 */
class MentionNode(private val seq: BasedSequence) : Node() {
    var username: String = seq.subSequence(1, seq.length).toString()
    override fun getSegments(): Array<BasedSequence> = arrayOf(seq)
}

/**
 * 话题标签节点
 * 语法：#hashtag
 */
class HashtagNode(private val seq: BasedSequence) : Node() {
    var hashtag: String = seq.subSequence(1, seq.length).toString()
    override fun getSegments(): Array<BasedSequence> = arrayOf(seq)
}

/**
 * 高亮文本节点
 * 语法：==highlight text==
 */
class HighlightNode : Node() {
    var highlightText: String = ""
    override fun getSegments(): Array<BasedSequence> = emptyArray()
}

/**
 * 徽章节点
 * 语法：!!type:text!!
 */
class BadgeNode(private val seq: BasedSequence, var badgeType: String, var badgeText: String) :
    Node() {
    override fun getSegments(): Array<BasedSequence> = arrayOf(seq)
}
```

### 注册自定义组件

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

### 自定义图片加载

库默认使用 Coil 进行图片加载。你可以参考 Coil 文档自定义加载行为——[coil](https://coil-kt.github.io/coil/image_loaders/)

## 🔌 插件

当前支持的官方插件模块如下：

| 插件 | 模块 (artifact) | 功能 |
|------|-----------------|------|
| 任务列表 (Task List) | markdown-task | 支持 GitHub 风格任务列表 `- [ ]` / `- [x]` |
| LaTeX / 数学公式 | markdown-latex | 支持行内与块级公式：`$...$`、`$$...$$` |

### 依赖声明（若插件以独立 artifact 发布）
```kotlin
dependencies {
    implementation("com.github.feiyin0719:markdown-task:<version>")
    implementation("com.github.feiyin0719:markdown-latex:<version>")
}
```
若只发布根库（如 `ComposeMarkdown`），这些模块可能已打包，可直接导入其类。

### 任务列表示例
```kotlin
val config = MarkdownRenderConfig.Builder()
    .addPlugin(
        TaskMarkdownRenderPlugin(
            taskStyle = SpanStyle(/* 自定义颜色/字重等 */)
        )
    )
    .build()
```
Markdown 示例：
```
- [ ] 未完成事项
- [x] 已完成事项
```

### LaTeX / 数学公式示例
```kotlin
val mathConfig = MarkdownRenderConfig.Builder()
    .addPlugin(
        MarkdownMathPlugin(
            mathStyle = SpanStyle(fontStyle = FontStyle.Italic),
            width = 200.sp,
            height = 80.sp,
            align = TextAlign.Center,
            enableGitLabExtension = false
        )
    )
    .build()
```
支持：
- 行内：`$E = mc^2$`
- 多行块级：
  ```
  $$
  E = mc^2
  $$
  ```
- 单行块级：`$$ E = mc^2 $$`

### 同时启用多个插件
```kotlin
val fullConfig = MarkdownRenderConfig.Builder()
    .addPlugin(TaskMarkdownRenderPlugin())
    .addPlugin(
        MarkdownMathPlugin(
            mathStyle = SpanStyle(fontStyle = FontStyle.Italic),
            width = 180.sp,
            height = 72.sp,
            align = TextAlign.Center
        )
    )
    .build()
```

### 自定义插件回顾
实现 `IMarkdownRenderPlugin`（或继承 `AbstractMarkdownRenderPlugin`）并通过 `addPlugin()` 注册。典型插件可：
- 添加 Flexmark 扩展（重写 `extensions()`）
- 提供自定义块级 / 行内解析器
- 提供块级渲染器 / 行内节点字符串构建器

> 完整示例见前文 “创建自定义插件” 部分。

---

## 📚 API 参考

### 主要接口

#### MarkdownView

`MarkdownView` 提供四种重载的 Composable 方法：

- 同步版本

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

- 异步版本

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

- 预解析 Node 版本

```kotlin
@Composable
fun MarkdownView(
    node: Node,
    markdownRenderConfig: MarkdownRenderConfig,
    modifier: Modifier = Modifier,
    linkInteractionListener: LinkInteractionListener? = null,
)
```

- LazyMarkdownView 版本（针对大文件懒加载）

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

#### LazyMarkdownView

`LazyMarkdownView` 专为高效渲染大型 Markdown 文件设计，随着滚动按块加载与展示内容。该组件适用于无法一次性全部载入内存的长文档。

特点：

- 📄 分块加载——按需逐步加载 Markdown 内容
- ⚡ 内存友好——仅保留可见与附近块在内存中
- 🎯 智能预取——基于滚动方向的预取策略
- 🔄 后台解析——在后台线程解析内容
- 📱 流畅滚动——基于 LazyColumn 的优化预取

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

参数说明：

- `file`——要展示的 Markdown 文件
- `markdownRenderConfig`——渲染配置
- `modifier`——应用于 LazyColumn 的修饰符
- `showNotSupportedText`——是否显示不支持元素的文本
- `linkInteractionListener`——链接交互回调
- `chunkLoaderConfig`——分块加载配置（见下文）
- `nestedPrefetchItemCount`——预取条目数量，提升滚动体验

用法示例：

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

适用场景：

- 超大 Markdown 文件（>10MB 或 >10000 行）
- 包含大量图片或复杂内容的文档
- 移动设备内存受限场景
- 需要保证滚动交互响应

#### ChunkLoaderConfig

用于控制 `LazyMarkdownView` 如何加载与缓存内容的配置：

```kotlin
data class ChunkLoaderConfig(
    val initialLines: Int = 1000,           // 初始加载行数
    val incrementalLines: Int = 500,        // 扩展加载的行数
    val maxCachedChunks: Int = 1000,        // 内存保留的最大块数
    val maxCachedFileLines: Int = 2000,     // 文件缓存的最大行数
    val chunkSize: Int = 5,                 // 每个块包含的区块数
    val parserDispatcher: CoroutineDispatcher = Dispatchers.Default,  // 后台解析
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO           // 文件 I/O
)
```

配置建议：

- 小中等文件可提高 `initialLines`，加快首屏加载
- 提高 `incrementalLines` 以在滚动展开时更顺滑
- 降低 `chunkSize` 获得更细粒度加载与更优内存占用
- 使用 `MarkdownThreadPool.dispatcher` 作为 `parserDispatcher` 以避免阻塞 UI
- 根据设备内存调整 `maxCachedChunks`、`maxCachedFileLines`

适用场景建议：
- 技术文档 / 笔记合集 / 导出 Wiki / AI 生成的海量长文
- 含大量图片（可结合图片懒加载策略）
- 阅读器类 App 中的本地离线 MD 文档

### IBlockRenderer<T>

```kotlin
interface IBlockRenderer<T : Node> {
    @Composable
    fun Invoke(node: T, modifier: Modifier = Modifier)
}
```

### IInlineNodeStringBuilder<T>

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

### LinkInteractionListener

用于处理链接点击事件。

### 样式类

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

### Markdown 插件接口

```kotlin
interface IMarkdownRenderPlugin {
    fun blockParserFactories(): List<CustomBlockParserFactory> = emptyList()
    fun inlineContentParserFactories(): List<InlineParserExtensionFactory> = emptyList()
    fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<out Block>> = emptyMap()
    fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<out Node>> =
        emptyMap()
}
```

## 未来计划

~~- 🚀 支持加载超大 Markdown 文件并逐步渲染\
按可见区块渲染以提升性能与内存使用~~ —— 已在 v0.0.4 完成

- 支持 Markdown 行内编辑模式（编辑与渲染同步进行）—— 计划在 v0.1.0 实现

- 通过可点击目录（TOC）支持跳转到指定章节

- 🚀 增加更多内置插件，覆盖常见场景

## ❓ 常见问题

### 问：如何处理超大 Markdown 文档的性能问题？

答：本库使用异步解析机制，在后台线程进行 Markdown 解析，不阻塞 UI 线程。对于超大文档，建议使用分页或懒加载。

### 问：当前支持哪些 Markdown 扩展语法？

答：目前支持 CommonMark 标准语法与 GFM 表格。未来将持续增强更多扩展语法支持。

### 问：如何自定义代码块的语法高亮？

答：你可以实现自定义 `CodeBlockRenderer`，并集成第三方语法高亮库。

## 🤝 贡献

欢迎你的贡献！协作步骤：
1. Fork 本仓库
2. 新建分支：`git checkout -b feat/my-feature`
3. 开发与提交（保持改动聚焦，必要时补充/更新测试）
4. 本地执行检查：
   - `./gradlew build` 编译并运行测试
   - 若配置额外工具，可执行：`./gradlew lintKotlin detekt`
5. 使用约定式提交信息：
   - `feat:` 新功能
   - `fix:` 缺陷修复
   - `docs:` 文档更新
   - `refactor:` 重构（无行为变更）
   - `perf:` 性能优化
   - `test:` 测试补充/调整
   - `build:` 构建或依赖相关改动
   - `chore:` 其他维护性工作
6. 提交 PR，描述：
   - 改动目的与背景
   - 截图（UI 变更）/ 性能对比（性能相关）
   - 关联 issue（例如 `Closes #12`）

问题反馈请附：
- 复现步骤（越简越好）
- 最小触发 markdown 片段
- 运行环境：设备/模拟器 API、库版本、系统版本

代码风格建议：
- 拆分可复用的小型 Composable 与函数
- 公共 API 添加 KDoc
- 避免过早优化，先用数据支撑

## 📄 许可证

本项目基于 MIT License 开源发布。

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

**[⬆ 回到顶部](#compose-markdown)**

由 Compose Markdown 团队用 ❤️ 打造

</div>
