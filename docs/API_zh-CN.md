# ComposeMarkdown API 参考
[English](API.md) |[简体中文](API_zh-CN.md)
> 本文是 `markdown` 模块中核心可组合函数（composable）和配置类型的详细参考文档。
>
> - 关于整体介绍、安装与特性说明，请查看[README](../README_zh-CN.md)。

## 目录

- [核心 Composable](#核心-composable)
  - [MarkdownView（同步）](#markdownview-同步)
  - [MarkdownView（异步）](#markdownview-异步)
  - [MarkdownView（节点）](#markdownview-节点)
  - [LazyMarkdownView](#lazymarkdownview)
  - [MarkdownChildren](#markdownchildren)
- [配置](#配置)
  - [MarkdownRenderConfig](#markdownrenderconfig)
  - [MarkdownRenderConfig.Builder](#markdownrenderconfigbuilder)
  - [MarkdownTheme](#markdowntheme)
  - [ChunkLoaderConfig（概览）](#chunkloaderconfig-概览)
- [插件与扩展点](#插件与扩展点)
  - [IMarkdownRenderPlugin](#imarkdownrenderplugin)
  - [IBlockRenderer](#iblockrenderer)
  - [IInlineNodeStringBuilder](#iinlinenodestringbuilder)
  - [MarkdownInlineView](#markdowninlineview-内联视图)
  - [RenderRegistry 与核心渲染器](#renderregistry-与核心渲染器)
  - [自定义 Block 和 Inline 解析](#自定义-block-和-inline-解析)
- [常见使用模式](#常见使用模式)

---

## 核心 Composable

### MarkdownView（同步）

同步解析版本。在 UI 线程的 `remember` 代码块中解析 Markdown 字符串。

适用于解析时间可以忽略的**小型或中等体量**的 Markdown 文档。

**函数签名**（来自 `MarkdownView.kt`）：

```kotlin
@Composable
fun MarkdownView(
 content: String,
 markdownRenderConfig: MarkdownRenderConfig,
 modifier: Modifier = Modifier,
 showNotSupportedText: Boolean = false,
 actionHandler: ActionHandler? = null,
 onError: (@Composable (Throwable) -> Unit)? = null,
)
```

**参数说明**

- `content`：要渲染的 Markdown 文本。
- `markdownRenderConfig`：渲染配置，通过 `MarkdownRenderConfig.Builder()` 创建。
- `modifier`：标准 Compose `Modifier`，用于尺寸、内边距等设置。
- `showNotSupportedText`：当为 `true` 时，不支持的元素会以文本回退的方式显示，而不是被静默忽略。
- `actionHandler`：可选的点击行为处理器（例如链接、图片、自定义块等）。具体实现可以参考项目示例。
- `onError`：可选的错误回调 Composable，当解析失败时被调用；若为 `null`，错误会被吞掉（不显示任何内容）。

**注意事项**

- 解析在组合阶段同步完成；对于很大的字符串应避免使用此重载。
- 如果你需要加载中 / 错误状态的 UI，请优先考虑下方的异步版本。

---

### MarkdownView（异步）

异步解析版本。将解析工作卸载到后台调度器上，并提供加载 / 错误状态的钩子。

推荐在以下场景使用：**内容较大**，或者你希望有加载指示和错误 UI。

**函数签名**（来自 `MarkdownView.kt`）：

```kotlin
@Composable
fun MarkdownView(
 content: String,
 markdownRenderConfig: MarkdownRenderConfig,
 modifier: Modifier = Modifier,
 showNotSupportedText: Boolean = false,
 actionHandler: ActionHandler? = null,
 parseDispatcher: CoroutineDispatcher? = null,
 onLoading: (@Composable () -> Unit)? = null,
 onError: (@Composable (Throwable) -> Unit)? = null,
)
```

**额外参数**

- `parseDispatcher`：用于解析的协程调度器。当为 `null` 时，库会使用 `MarkdownThreadPool.dispatcher`（后台线程池）以避免阻塞主线程。
- `onLoading`：可选的加载中 Composable，在解析进行时显示（例如进度指示器或骨架屏）。

**行为说明**

- 当 `content` 或底层解析器实例变化时，会重新开始解析，并在此期间调用 `onLoading`（如果提供）。
- 解析成功后，会在内部将解析得到的 AST 交由基于节点的 `MarkdownView` 渲染。
- 解析失败时，调用 `onError`，并将异常作为参数传入。

**示例**

```kotlin
MarkdownView(
 content = largeMarkdown,
 markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
 modifier = Modifier.fillMaxSize(),
 showNotSupportedText = true,
 parseDispatcher = Dispatchers.IO, // 或者为 null 使用 MarkdownThreadPool.dispatcher
 onLoading = {
  CircularProgressIndicator()
 },
 onError = { error ->
  Text(
   text = "Failed to load markdown: ${error.message}",
   color = MaterialTheme.colorScheme.error,
  )
 },
)
```

---

### MarkdownView（节点）

基于节点的版本。渲染一个**预先解析好的** Flexmark AST 节点。

在以下场景使用：

- 在其他地方（例如缓存层、后台任务）完成 Markdown 解析，希望复用 AST。
- 需要多次渲染同一份内容而不希望重复解析。

**函数签名**（来自 `MarkdownView.kt`）：

```kotlin
@Composable
fun MarkdownView(
 node: Node,
 markdownRenderConfig: MarkdownRenderConfig,
 modifier: Modifier = Modifier,
 showNotSupportedText: Boolean = false,
 actionHandler: ActionHandler? = null,
)
```

**说明**

- `Node` 实例通常通过 `markdownRenderConfig.parser.parse(content)` 得到。
- 此重载本身并不知道加载或错误状态；你需要在调用外部自行管理这些逻辑。

**示例**

```kotlin
val config = remember { MarkdownRenderConfig.Builder().build() }
val parser = remember(config) { config.parser }
val node = remember(markdownText) { parser.parse(markdownText) }

MarkdownView(
 node = node,
 markdownRenderConfig = config,
)
```

---

### LazyMarkdownView

用于磁盘上**超大 Markdown 文件**的懒加载 Composable。

它不会一次性解析和渲染整篇文档，而是：

- 将内容切分为多个块（chunk）。
- 随着用户滚动按需解析和渲染这些块。
- 使用带预取的 `LazyColumn` 在内存占用和性能之间取得平衡。

**函数签名**（来自 `LazyMarkdownView.kt`）：

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyMarkdownView(
 file: File,
 markdownRenderConfig: MarkdownRenderConfig,
 modifier: Modifier = Modifier,
 showNotSupportedText: Boolean = false,
 actionHandler: ActionHandler? = null,
 chunkLoaderConfig: ChunkLoaderConfig = ChunkLoaderConfig(
  parserDispatcher = MarkdownThreadPool.dispatcher,
 ),
 nestedPrefetchItemCount: Int = 3,
)
```

**参数说明**

- `file`：要显示的本地 Markdown 文件。若内容来自 assets 或网络，请先复制 / 下载为本地 `File`。
- `markdownRenderConfig`：与 `MarkdownView` 相同的渲染配置。
- `modifier`：应用于内部 `LazyColumn` 的修饰符。
- `showNotSupportedText`：是否对不支持的元素显示文本提示。
- `actionHandler`：可选，处理文档内的交互行为（如链接、图片、自定义块等）。
- `chunkLoaderConfig`：控制文件如何被切分和解析。默认会使用 `MarkdownThreadPool.dispatcher` 作为 `parserDispatcher`。
- `nestedPrefetchItemCount`：视口前后预取的列表项数量，用于提升滚动流畅度。

**使用建议**

- 适合只读的长篇内容，比如书籍、大型文档等。
- 若是可编辑或频繁变动的内容，更推荐使用异步版 `MarkdownView`，并自行实现分页或 diff 逻辑。

---

### MarkdownChildren

一个遍历并渲染父节点所有子节点的实用 Composable。它会根据当前主题或传入参数自动处理 `Spacer` 的插入逻辑。

**函数签名**（来自 `MarkdownContent.kt`）：

```kotlin
@Composable
fun MarkdownChildren(
    parent: Node,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    spacerHeight: Dp = currentTheme().spacerTheme.spacerHeight,
    showSpacer: Boolean = currentTheme().spacerTheme.showSpacer,
    childModifierFactory: (child: Node) -> Modifier = {
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    },
    onBeforeChild: (@Composable (child: Node, parent: Node) -> Unit)? = null,
    onAfterChild: (@Composable (child: Node, parent: Node) -> Unit)? = null,
    onBeforeAll: (@Composable (parent: Node) -> Unit)? = null,
    onAfterAll: (@Composable (parent: Node) -> Unit)? = null,
)
```

**参数说明**

- `parent`：需要渲染子节点的 `Node` 对象。
- `verticalArrangement`：子节点的垂直排列方式。
- `spacerHeight`：子节点间的垂直间距。默认为 `theme.spacerTheme.spacerHeight`。
- `showSpacer`：是否插入间距。默认为 `theme.spacerTheme.showSpacer`。
- `onBeforeChild`：可选的 Composable 回调，在渲染每个子节点前调用（适用于自定义标记或缩进）。
- `onAfterChild`：可选的 Composable 回调，在渲染每个子节点后调用。
- `onBeforeAll`：可选的 Composable 回调，在渲染所有子节点之前调用（例如用于顶部间距）。
- `onAfterAll`：可选的 Composable 回调，在渲染所有子节点之后调用（例如用于底部间距）。
- `childModifierFactory`：为每个子节点创建 Modifier 的工厂函数。

**适用场景**

当你实现自定义的 `IBlockRenderer`（例如自定义容器块）并需要以标准的间距规则渲染嵌套内容时使用。
注意：`MarkdownChildren` 提供容器布局（如 `Column`），你可以通过 `modifier` 自定义。


---

## 配置

### MarkdownRenderConfig

`MarkdownRenderConfig` 包含解析和渲染 Markdown 所需的一切：

- 一个 `MarkdownTheme`，描述排版、颜色和组件样式。
- 一个 Flexmark 的 `Parser` 与 `HtmlRenderer`。
- 一个 `RenderRegistry`，描述如何渲染各类节点和块。

实例通过 `MarkdownRenderConfig.Builder` 创建：

```kotlin
val config = MarkdownRenderConfig.Builder()
 // 配置主题、插件、渲染器等...
 .build()
```

通常你会为每个界面创建一个 `MarkdownRenderConfig`，或者通过 `remember` / DI 进行共享。

---

### MarkdownRenderConfig.Builder

用于以流式 API 自定义解析、主题和渲染行为的构建器。

**关键方法**（来自 `MarkdownRenderConfig.kt`）：

```kotlin
class MarkdownRenderConfig {
 class Builder {
  fun markdownTheme(markdownTheme: MarkdownTheme): Builder
  fun addPlugin(plugin: IMarkdownRenderPlugin): Builder
  fun addInlineNodeStringBuilder(
   nodeClass: Class<out Node>,
   builder: IInlineNodeStringBuilder<*>,
  ): Builder
  fun addBlockRenderer(
   blockClass: Class<out Block>,
   renderer: IBlockRenderer<*>,
  ): Builder
  fun markdownTextRenderer(renderer: MarkdownTextRenderer): Builder
  fun markdownContentRenderer(renderer: MarkdownContentRenderer): Builder
  fun addBlockParserFactory(factory: CustomBlockParserFactory): Builder
  fun addInlineContentParserFactory(factory: InlineParserExtensionFactory): Builder
  fun addDelimiterProcessor(processor: DelimiterProcessor): Builder
  fun addExtension(extension: Extension): Builder
  fun build(): MarkdownRenderConfig
 }
}
```

#### markdownTheme(markdownTheme: MarkdownTheme)

设置 Markdown 渲染的视觉主题。

- 如果未设置，库会使用默认的 `MarkdownTheme()`。
- 主题通常会封装以下内容的排版、颜色、间距和装饰：
  - 段落、标题、强调、加粗等文本样式
  - 代码块、行内代码
  - 列表、引用块、表格和代码块

#### addPlugin(plugin: IMarkdownRenderPlugin)

注册渲染插件。

插件可以：

- 提供 Flexmark 扩展。
- 注册额外的块 / 内联渲染器。
- 添加自定义块解析器或内联解析扩展。

内置核心能力由内部的 `CorePlugin` 提供，并总是自动添加。

#### addInlineNodeStringBuilder / addBlockRenderer

用于自定义特定节点 / 块的渲染逻辑的底层钩子。

- `addInlineNodeStringBuilder(nodeClass, builder)`：定义某个内联节点类型如何被转换为文本 span。
- `addBlockRenderer(blockClass, renderer)`：定义某个块节点类型如何被渲染为 Compose UI。

这类方法通常在你自己的 `IMarkdownRenderPlugin` 实现中使用；如果只是做少量调整，也可以直接在 Builder 上调用。

#### markdownTextRenderer / markdownContentRenderer

高级自定义入口：

- `markdownTextRenderer`：重写文本节点的渲染方式（例如自定义排版或文字布局）。
- `markdownContentRenderer`：重写更高层级内容节点的渲染方式。

大多数场景下，你只需要配置主题和插件即可，无需动到这些高级接口。

#### addBlockParserFactory / addInlineContentParserFactory / addDelimiterProcessor / addExtension

Flexmark 级别的扩展 API：

- `addBlockParserFactory`：注册自定义块解析器（用于新增块语法）。
- `addInlineContentParserFactory`：注册内联解析扩展。
- `addDelimiterProcessor`：处理自定义分隔符（例如新的强调标记）。
- `addExtension`：直接添加 Flexmark 的 `Extension` 实例。

这部分主要用于高级 / 自定义 Markdown 方言的场景。

---

### MarkdownTheme

`MarkdownTheme`（来自 `com.iffly.compose.markdown.style.MarkdownTheme`）是控制 Markdown 内容在 Compose 中如何呈现的**核心主题模型**。

它负责：

- 基本文本样式（字号、行高、字体、颜色等）。
- 强调 / 加粗 / 删除线 / 下标等样式。
- 链接样式（普通 / 悬停 / 聚焦 / 按下）。
- 各级标题样式（H1–H6）。
- 列表、图片、引用块、分隔、表格、代码块等的布局与外观。

#### 数据结构（简化版）

```kotlin
@Stable
data class MarkdownTheme(
 val breakLineHeight: Dp = 1.dp,
 val breakLineColor: Color = Color(0xFFE0E0E0),
 val textStyle: TextStyle = TextStyle(...),
 val strongEmphasis: SpanStyle = SpanStyle(...),
 val emphasis: SpanStyle = SpanStyle(...),
 val code: TextStyle = TextStyle(...),
 val strikethrough: SpanStyle = SpanStyle(...),
 val subscript: SpanStyle = SpanStyle(...),
 val link: TextLinkStyles = TextLinkStyles(...),
 val headStyle: Map<Int, TextStyle> = defaultHeadStyles,
 val listTheme: ListTheme = ListTheme(),
 val blockQuoteTheme: BlockQuoteTheme = BlockQuoteTheme(),
 val spacerTheme: SpacerTheme = SpacerTheme(),
 val codeBlockTheme: CodeBlockTheme = CodeBlockTheme(),
)
```

`MarkdownTheme.kt` 中的重要嵌套类型包括：

- `BlockQuoteTheme` —— 控制边框颜色 / 宽度、背景、形状、内边距等。
- `SpacerTheme` —— 是否在块之间显示间隔，以及间隔高度。
- `ListTheme` —— 列表项目符号 / 序号的间距与样式。
- `CodeBlockTheme` —— 代码块文本样式、背景、内边距、溢出行为等。

> **注意**：
> - `TableTheme` 现已移至 `TableMarkdownPlugin` 配置中，不再属于 `MarkdownTheme`。
> - `ImageTheme` 现已移至 `ImageMarkdownPlugin` 配置中，不再属于 `MarkdownTheme`。

#### 标题级别

标题通过 `headStyle` 映射配置。key 为 1–6 的整数，并在 `MarkdownTheme` 上通过常量暴露：

```kotlin
MarkdownTheme.HEAD1 // 1 级标题（H1）
MarkdownTheme.HEAD2 // 2 级标题（H2）
// ... 一直到 HEAD6
```

示例覆盖：

```kotlin
val theme = MarkdownTheme(
 headStyle = mapOf(
  MarkdownTheme.HEAD1 to
   TextStyle(
    fontSize = 32.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
   ),
  MarkdownTheme.HEAD2 to
   TextStyle(
    fontSize = 28.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurface,
   ),
 ),
)
```

#### 链接样式

`link` 使用 Compose 的 `TextLinkStyles` 来描述链接在不同交互状态下的样式：

```kotlin
val theme = MarkdownTheme(
 link =
  TextLinkStyles(
   style =
    SpanStyle(
     color = MaterialTheme.colorScheme.primary,
     textDecoration = TextDecoration.Underline,
    ),
   hoveredStyle =
    SpanStyle(
     color =
      MaterialTheme.colorScheme.primary.copy(
       alpha = 0.8f,
      ),
     textDecoration = TextDecoration.Underline,
    ),
   focusedStyle =
    SpanStyle(
     color = MaterialTheme.colorScheme.primary,
     textDecoration = TextDecoration.Underline,
    ),
   pressedStyle =
    SpanStyle(
     color =
      MaterialTheme.colorScheme.primary.copy(
       alpha = 0.6f,
      ),
     textDecoration = TextDecoration.Underline,
    ),
  ),
)
```

#### 列表

`ListTheme` 用于控制列表中项目符号的间距和文本样式：

```kotlin
@Immutable
data class ListTheme(
 val markerSpacerWidth: Dp = 4.dp,
 val showSpacerInTightList: Boolean = true,
 val tightListSpacerHeight: Dp = 4.dp,
 val markerTextStyle: TextStyle? =
  TextStyle(
   lineHeight = 24.sp,
   fontSize = 17.sp,
   textAlign = TextAlign.End,
  ),
)
```

常见自定义方式：

- 当使用较宽的项目符号 / 复选框时，可以增大 `markerSpacerWidth`。
- 调整 `markerTextStyle` 以便更好地与正文对齐。

#### 图片

已移至 `ImageMarkdownPlugin`。请参阅 [插件](#插件) 部分以了解如何配置 `ImageTheme`。


#### 引用块

`BlockQuoteTheme` 决定 `>` 引用块的外观：

```kotlin
@Immutable
data class BlockQuoteTheme(
 val borderColor: Color = Color.LightGray,
 val borderWidth: Dp = 5.dp,
 val backgroundColor: Color = Color(0xFFF5F5F5),
 val shape: Shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
 val padding: PaddingValues = PaddingValues(horizontal = 12.dp),
)
```

你可以将其调整为更贴合 Material3 Surface 风格或你的品牌配色。

#### 表格与代码块

`TableTheme` 和 `CodeBlockTheme` 覆盖了表格和围栏代码块等更复杂的布局：

- 表格的边框颜色、厚度、行背景色等。
- 单元格 / 头部文本样式、内边距、形状等。
- 代码文本样式、背景、内边距、溢出行为等。

在需要完整字段说明时，可以直接打开 `MarkdownTheme.kt`。核心思想是：

- 所有视觉层面的调优都通过 `MarkdownTheme` 完成，
- 然后在 `MarkdownRenderConfig.Builder().markdownTheme(theme)` 中传入该主题即可。

**综合示例：**

```kotlin
val markdownTheme =
 MarkdownTheme(
  textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
  strongEmphasis =
   SpanStyle(
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
   ),
  emphasis =
   SpanStyle(
    fontStyle = FontStyle.Italic,
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
   ),
  headStyle =
   mapOf(
    MarkdownTheme.HEAD1 to
     TextStyle(
      fontSize = 32.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
     ),
   ),
 )

val config = MarkdownRenderConfig.Builder()
 .markdownTheme(markdownTheme)
 .build()

MarkdownView(
 content = markdownContent,
 markdownRenderConfig = config,
)
```

---

### ChunkLoaderConfig（概览）

`ChunkLoaderConfig`（来自 `com.iffly.compose.markdown.chunkloader`）用于控制 `LazyMarkdownView` 如何切分和解析大文件。

典型字段包括：

- `parserDispatcher`：用于后台解析的调度器（默认 `MarkdownThreadPool.dispatcher`）。
- 以及其他控制：
  - 初始加载的行数 / 块数量。
  - 用户滚动时增量加载的大小。
  - 内存中缓存的最大块数量等。

具体字段可能随版本演进而变化，请以 `ChunkLoaderConfig` 源码为准。

**概念示例：**

```kotlin
val chunkConfig = ChunkLoaderConfig(
 // initialLines = 800,
 // incrementalLines = 400,
 // maxCachedChunks = 8,
 parserDispatcher = MarkdownThreadPool.dispatcher,
)

LazyMarkdownView(
 file = largeFile,
 markdownRenderConfig = config,
 chunkLoaderConfig = chunkConfig,
)
```

---

## 插件与扩展点

### IMarkdownRenderPlugin

向 Markdown 引擎添加功能的入口。

一个插件可以：

- 提供 Flexmark 扩展。
- 注册内联字符串构建器和块渲染器。
- 提供块解析工厂和内联解析扩展工厂。

该接口的具体定义在 `IMarkdownRenderPlugin` 中（位于 `config` 或 `core` 包）。

插件通过 `MarkdownRenderConfig.Builder.addPlugin` 添加。

### 自定义 Block 和 Inline 解析

本库基于 Flexmark，你可以通过实现 `IMarkdownRenderPlugin` 来扩展 Block（块级节点）和 Inline（行内节点）的解析和渲染。实际使用时通常只需要了解几个主要入口：

- **自定义 Block 解析**：
  - 使用 Flexmark 的 `CustomBlockParserFactory` 定义新的块级语法（例如 `:::alert` 这类提示块）。
  - 在 `IMarkdownRenderPlugin.blockParserFactories()` 中返回自定义 factory 列表，让解析器识别这些块。
  - 为自定义 Block 类型实现 `IBlockRenderer<YourBlock>`，并在 `blockRenderers()` 中注册，为其提供对应的 Compose UI 渲染。

- **自定义 Inline 解析**：
  - 通过实现 `InlineParserExtensionFactory` 定义新的行内语法（例如 `@user`、`#tag` 或 `==highlight==`）。
  - 在 `IMarkdownRenderPlugin.inlineContentParserFactories()` 中返回自定义的 inline factory，让解析器在文本中识别这些标记。
  - 如需控制这些 inline 节点如何转换为文本或富文本内容，可以在 `inlineNodeStringBuilders()` 中为对应的 Flexmark `Node` 注册 `IInlineNodeStringBuilder`。

典型流程是：**使用 Flexmark 的扩展点定义语法 → 在 `IMarkdownRenderPlugin` 中注册解析与渲染策略 → 通过 `MarkdownRenderConfig.Builder.addPlugin(...)` 启用插件**。你可以参考示例 `CustomMarkdownPlugin` 来查看完整用法。

关于 Flexmark 扩展点和解析器定制的更多细节，请参考官方文档：
[flexmark-java](https://github.com/vsch/flexmark-java)

### IBlockRenderer

负责将某个特定的 Flexmark `Block` 节点渲染为 Compose UI。

- 通过 Builder 的 `addBlockRenderer` 或插件注册。
- 允许你为特定块元素提供完全自定义的渲染外观。

**接口签名（简化）**（见 `IBlockRenderer.kt`）：

```kotlin
interface IBlockRenderer<in T> where T : Block {
 @Composable
 fun Invoke(
  node: T,
  modifier: Modifier,
 )
}
```

**参数说明**

- `T`：要渲染的块节点类型，必须是 Flexmark `Block` 的子类，例如 `Paragraph`、`Heading`、`BlockQuote`、`TableBlock` 等。
- `node`：当前要渲染的块节点实例。你通常会：
  - 遍历它的子节点并调用 `MarkdownContent` / `MarkdownText` 等已有组件；
  - 或者根据节点类型读取特定信息（如 `FencedCodeBlock.info` 中的语言标签）。
- `modifier`：传入的 `Modifier`，由上层 `MarkdownContent` 负责组合。实现时应**优先调用 `modifier`** 再在其基础上 `then(...)` 追加自己的修饰，而不是完全丢弃它，以便保持布局和外层容器的一致性。

**实现建议**

- 通常只对**结构和布局**做自定义（行/列、卡片、背景、间距等），文本样式优先通过 `MarkdownTheme` 调整。
- 若你的渲染依赖交互（点击、复制、跳转），应通过 `currentActionHandler()` 访问全局 `ActionHandler`，不要在渲染器中直接持有引用，以避免与库的生命周期管理冲突。
- 自定义渲染器注册后会**完全覆盖**该 Block 类型的默认渲染，需要确保处理好所有子节点，否则可能出现内容缺失。

### IInlineNodeStringBuilder

负责将某个特定的内联 `Node` 转换为带样式的文本 span。

- 通过 `addInlineNodeStringBuilder` 注册。
- 文本渲染器在构建 `AnnotatedString` 时会使用这些构建器。

**接口签名（简化）**（见 `IInlineNodeStringBuilder.kt`）：

```kotlin
interface IInlineNodeStringBuilder<in T> where T : Node {
 fun AnnotatedString.Builder.buildInlineNodeString(
  node: T,
  inlineContentMap: MutableMap<String, MarkdownInlineView>,
  markdownTheme: MarkdownTheme,
  actionHandler: ActionHandler?,
  indentLevel: Int,
  isShowNotSupported: Boolean,
  renderRegistry: RenderRegistry,
  nodeStringBuilderContext: NodeStringBuilderContext,
 )
}
```

**参数说明**

- `T`：要处理的内联节点类型，必须是 Flexmark `Node` 的子类，例如 `Text`、`Emphasis`、`StrongEmphasis`、`Link` 等。
- `node`：当前内联节点实例，用于读取内容（如文本、链接 URL、强调级别等）。
- `inlineContentMap`：用于注册富内联内容的可变 Map：
  - key 为唯一字符串，value 为 `MarkdownInlineView`；
  - 构建器通常会在这里放入 `MarkdownRichTextInlineContent`，然后在 `AnnotatedString` 中通过 `appendInlineContent(key, placeholder)` 引用；
  - 注意：**key 稳定性等同于 Compose key**，若只更改内容不更改 key，会导致已有内联内容无法更新。
- `markdownTheme`：当前生效的 `MarkdownTheme`，你应优先从这里读取样式（`SpanStyle` / `ParagraphStyle` 等），而不是在代码中硬编码颜色和字号。
- `actionHandler`：可选交互处理器，例如：
  - 在 `Link` 构建器中用来创建 `MarkdownLinkInteractionListener`；
  - 在自定义 mention、tag 等内联组件中，用于分发点击事件到业务层。
- `indentLevel`：当前缩进层级，多用于列表 / 嵌套结构：
  - 可以基于它计算前导空格或缩进宽度；
  - 一般与 `ListTheme.markerSpacerWidth` 组合使用。
- `isShowNotSupported`：是否显示“不支持”的占位提示：
  - 当某类节点还未有完整实现时，可以在为 `true` 时输出占位文本（如 `"[unsupported: NodeName]"`）；
  - 为 `false` 时则可以选择直接忽略该节点。
- `renderRegistry`：渲染注册表，用于：
  - 递归构建子节点字符串（例如 `buildChildNodeAnnotatedString` 内部会依赖它）；
  - 查找其他已注册的内联构建器，实现组合式渲染。
- `nodeStringBuilderContext`：节点字符串构建上下文：
  - 提供按职责分组的子上下文：布局（`layoutContext`）、文本样式（`designContext`）与系统能力（`systemContext`）；
  - 当需要文本测量或密度转换时，使用 `nodeStringBuilderContext.layoutContext`。

**实现建议**

- 在扩展 `buildInlineNodeString` 时优先使用 `pushStyle` / `pop`、`withLink` 等 Compose 提供的 API 叠加样式，而不是重新构造整段文本。
- 若你的节点可能包含子节点（如 `Link`、`Emphasis`），应通过 `renderRegistry` 调用通用的“构建子节点”工具方法，而不是手动遍历所有子类型，以便自动获得后续新节点类型的支持。
- 避免在实现中持有 `Context` 或 `Composable` 状态，所有副作用都应交给 `ActionHandler` 或外层 Composable 处理，构建器本身只负责“描述文本 & 内联视图”。

### MarkdownInlineView（内联视图）

`MarkdownInlineView` 是一个小型的 sealed interface，用于表示可以嵌入文本中的**内联 Composable 内容**。

它主要由内联节点字符串构建器消费，当它们需要注册富内联内容（而不是纯文本）时会用到该类型。

定义于 `render/MarkdownInlineView.kt`：

```kotlin
sealed interface MarkdownInlineView {
 data class MarkdownRichTextInlineContent(
  val inlineContent: RichTextInlineContent,
 ) : MarkdownInlineView
}
```

关键点：

- `MarkdownInlineView` 是 `inlineContentMap: MutableMap<String, MarkdownInlineView>` 中的 value 类型，传入 `IInlineNodeStringBuilder` 的实现。
- `MarkdownRichTextInlineContent` 包裹了一个 `RichTextInlineContent`，该类型负责真正渲染内联的 Composable 元素（图标、chip、徽章、自定义小组件等）。
- 内联构建器会在 `inlineContentMap` 中以唯一 key 注册条目，文本渲染器再通过 `AnnotatedString` 中的内联内容 key 来显示这些富内容。
- 如果你只修改了inline content, 没有修改`inlineContentMap`中的key,它将不会更新

**自定义内联构建器的概念示例**

```kotlin
class IconInlineNodeStringBuilder : IInlineNodeStringBuilder<IconNode> {
 override fun AnnotatedString.Builder.buildInlineNodeString(
  node: IconNode,
  inlineContentMap: MutableMap<String, MarkdownInlineView>,
  markdownTheme: MarkdownTheme,
  actionHandler: ActionHandler?,
  indentLevel: Int,
  isShowNotSupported: Boolean,
  renderRegistry: RenderRegistry,
  nodeStringBuilderContext: NodeStringBuilderContext,
 ) {
  // 1. 使用唯一 key 注册富内联内容
  val key = "icon-${node.id}"
  inlineContentMap[key] =
   MarkdownInlineView.MarkdownRichTextInlineContent(
    inlineContent = /* 为该 icon 构建 RichTextInlineContent */
     buildIconInlineContent(node),
   )

  // 2. 在文本中插入一个占位字符，并附带该 key
  appendInlineContent(key, " ")
 }
}
```

通常你不会在 `IInlineNodeStringBuilder` 之外直接操作 `MarkdownInlineView`；它主要作为 Flexmark 内联节点与 Compose 富文本内联内容之间的桥梁。

#### RichTextInlineContent 的类型及使用场景

`MarkdownRichTextInlineContent` 内部封装的 `RichTextInlineContent`（定义于 `widget/richtext/RichTextInlineContent.kt`）目前有两种形态：

- `EmbeddedRichTextInlineContent`
  - **特性**
   	- 使用 `Placeholder` 来定义宽度、高度和对齐方式，并与周围文本处于同一段落布局中。
   	- 可选的 `adjustSizeByContent` 标志可以根据内容动态微调尺寸。
   	- `content: @Composable (String) -> Unit` 接收一个 key 或占位文本，渲染一个小型内联 Composable（icon、badge 等）。
  - **推荐场景**
   	- 小型内联图标（如 “⚠”、“✔”）、emoji、状态点、计数徽章等，需要**与文本同一行**展示的内容。
   	- 需要与文本基线和行高精确对齐的自定义视图。
   	- 例如在 `**[info] text**` 前插入一个提示图标，或在用户名后显示认证徽章。

- `StandaloneInlineContent`
  - **特性**
   	- 使用 `modifier: Modifier = Modifier` 控制整体布局和尺寸。
   	- `content: @Composable (modifier: Modifier) -> Unit` 渲染一个完整的自定义 Compose 组件。
   	- 在文本布局中被视为独立段落 / 区块，而不是内联文本的一部分。
  - **推荐场景**
   	- 向 Markdown 中插入完整的自定义区块，如广告卡片、按钮区域、统计卡片、富媒体模块等。
   	- 需要垂直留白，并与普通段落分离布局的内容。
   	- 例如在两段文本之间插入推荐卡片、横幅或投票组件。

在自定义 `IInlineNodeStringBuilder` 中：

- 当组件需要**随文本内联显示**并位于同一行时，使用 `EmbeddedRichTextInlineContent`。
- 当组件需要**单独占据一个段落 / 区块**时，使用 `StandaloneInlineContent`。

### RenderRegistry 与核心渲染器

渲染管线的核心是 `RenderRegistry`，它负责决定每个 Flexmark 节点如何转换为 Compose UI。

`RenderRegistry` 在 `MarkdownRenderConfig.Builder.build()` 中被构建，接收以下信息：

- `blockRenderers: Map<Class<out Block>, IBlockRenderer<*>>`
- `inlineNodeStringBuilders: Map<Class<out Node>, IInlineNodeStringBuilder<*>>`
- 可选的 `MarkdownContentRenderer`
- 可选的 `MarkdownTextRenderer`

通常你会通过以下方式**间接**与它交互：

- `Builder.addBlockRenderer(...)`
- `Builder.addInlineNodeStringBuilder(...)`
- 在插件中实现 `IMarkdownRenderPlugin`，并贡献自定义渲染器。

#### 内置核心渲染器（来自 `com.iffly.compose.markdown.core.renders`）

库内置了一系列块渲染器和内联字符串构建器，实现了默认的 CommonMark / GFM 行为。理解这些有助于你在需要时**替换特定部分**的渲染逻辑。

##### TextBlockRenderer / ParagraphRenderer / HeadingRenderer

文件：`core/renders/TextBlockRenderer.kt`

```kotlin
open class TextBlockRenderer<T> : IBlockRenderer<T> where T : Block {
 @Composable
 override fun Invoke(node: T, modifier: Modifier) {
  MarkdownText(parent = node, modifier = modifier)
 }
}

class ParagraphRenderer : TextBlockRenderer<Paragraph>()
class HeadingRenderer : TextBlockRenderer<Heading>()
```

- `TextBlockRenderer` 是一个通用块渲染器，会委托给 `MarkdownText` 来渲染块中所有的内联子节点（段落、标题等）。
- `ParagraphRenderer` 和 `HeadingRenderer` 分别用于渲染 `Paragraph` 和 `Heading` 节点。

如果你只想**微调文字样式**，更推荐通过修改 `MarkdownTheme` 来完成，而不是重写这些渲染器；只有在需要完全不同的布局时，才有必要自定义块渲染器。

##### BlockQuoteRenderer

文件：`core/renders/BlockQuoteRenderer.kt`

```kotlin
class BlockQuoteRenderer : IBlockRenderer<BlockQuote> {
 @Composable
 override fun Invoke(node: BlockQuote, modifier: Modifier) {
  MarkdownBlockQuote(node = node, modifier = modifier)
 }
}
```

`MarkdownBlockQuote` 会使用当前的 `MarkdownTheme` 来渲染引用块：

- `theme.blockQuoteTheme.borderColor / borderWidth`
- `backgroundColor`、`shape`、`padding`
- `theme.spacerTheme.spacerHeight`（上下间隔）

它随后会迭代子节点，并调用 `MarkdownContent` 进行渲染。若想在视觉上自定义引用块，可以调整 `MarkdownTheme.blockQuoteTheme`；若需要完全不同的布局，可以提供你自己的 `IBlockRenderer<BlockQuote>`，并通过 `addBlockRenderer` 注册。

##### ParagraphNodeStringBuilder

文件：`core/renders/ParagraphNodeStringBuilder.kt`

```kotlin
class ParagraphNodeStringBuilder : CompositeChildNodeStringBuilder<Node>() {
 override fun getParagraphStyle(
  node: Node,
  markdownTheme: MarkdownTheme,
 ): ParagraphStyle? = markdownTheme.getNodeParagraphStyle(node)
}
```

这是默认的“段落类”内联字符串构建器：

- 通过 `markdownTheme.getNodeParagraphStyle(node)`（工具函数）来为节点选择合适的 `ParagraphStyle`。
- 使用 `CompositeChildNodeStringBuilder` 遍历子节点并构建文本内容。

如果你希望在全局层面修改段落的对齐方式、间距等段落级行为，可以调整 `getNodeParagraphStyle` 中的逻辑，或者提供你自己的 `CompositeChildNodeStringBuilder` 实现。

##### HeadingNodeStringBuilder

文件：`core/renders/HeadingNodeStringBuilder.kt`

```kotlin
class HeadingNodeStringBuilder : CompositeChildNodeStringBuilder<Heading>() {
 override fun getSpanStyle(
  node: Heading,
  markdownTheme: MarkdownTheme,
 ): SpanStyle? = markdownTheme.getNodeSpanStyle(node)

 override fun getParagraphStyle(
  node: Heading,
  markdownTheme: MarkdownTheme,
 ): ParagraphStyle? = markdownTheme.getNodeParagraphStyle(node)
}
```

针对标题：

- 通过 `markdownTheme.getNodeSpanStyle(node)` 选择 `SpanStyle`，通常由 `MarkdownTheme.headStyle` 映射得到。
- 通过 `markdownTheme.getNodeParagraphStyle(node)` 设置段落级样式。

它是 `MarkdownTheme.headStyle` 与实际标题渲染之间的桥梁。如需自定义标题排版、颜色或间距，优先调整 `MarkdownTheme`，而不是直接替换该构建器。

##### LinkNodeStringBuilder

文件：`core/renders/LinkNodeStringBuilder.kt`

```kotlin
class LinkNodeStringBuilder : IInlineNodeStringBuilder<Link> {
 override fun AnnotatedString.Builder.buildInlineNodeString(
  node: Link,
  inlineContentMap: MutableMap<String, MarkdownInlineView>,
  markdownTheme: MarkdownTheme,
  actionHandler: ActionHandler?,
  indentLevel: Int,
  isShowNotSupported: Boolean,
  renderRegistry: RenderRegistry,
  nodeStringBuilderContext: NodeStringBuilderContext,
 ) {
  val linkInteractionListener =
   actionHandler?.let {
    MarkdownLinkInteractionListener(actionHandler = it, node = node)
   }
  val linkAnnotation =
   LinkAnnotation.Url(
    url = node.url.toString(),
    styles = markdownTheme.link,
    linkInteractionListener = linkInteractionListener,
   )
  withLink(linkAnnotation) {
   buildChildNodeAnnotatedString(
    node,
    indentLevel,
    inlineContentMap,
    markdownTheme,
    renderRegistry,
    actionHandler,
    isShowNotSupported,
    nodeStringBuilderContext,
   )
  }
 }
}
```

要点：

- 使用 `LinkAnnotation.Url` 包裹链接子节点，并应用 `markdownTheme.link` 中配置的样式。
- 如果提供了 `ActionHandler`，会创建 `MarkdownLinkInteractionListener`，以便在点击链接时把事件分发到你的应用逻辑。
- 实际的内联内容由 `buildChildNodeAnnotatedString` 生成，该方法会使用 `RenderRegistry` 中注册的其他内联构建器。

如需修改链接行为：

- **视觉层面**：调整 `MarkdownTheme.link`。
- **交互层面**：实现自定义 `ActionHandler` 来处理链接点击。
- 若需完全自定义链接的渲染方式，可以提供自己的 `IInlineNodeStringBuilder<Link>` 并通过 `addInlineNodeStringBuilder` 注册。

##### MarkdownTable（表格）

`TableMarkdownPlugin` (位于 `markdown-table` 模块中)

文件：`markdown-table/src/main/java/com/iffly/compose/markdown/table/MarkdownTable.kt`

该模块基于 Flexmark 的 `TableBlock` 扩展及一个小型 Compose `Table` DSL，提供完整的表格渲染能力。

核心类型包括：

- `TableWidgetRenderer<T : Node>` —— 用于渲染表格**各部分**的函数式接口。
- `TableTitleRenderer` —— 默认表格标题 / 头部区域渲染器。
- `TableCellRenderer` —— 默认单元格渲染器。
- `TableRenderer` —— 针对 `TableBlock` 的 `IBlockRenderer` 实现，将上述组件串联起来。
- `MarkdownTable` —— 顶层 Composable，负责表格布局。

```kotlin
fun interface TableWidgetRenderer<T : Node> {
 @Composable
 operator fun invoke(node: T, modifier: Modifier)
}

class TableTitleRenderer : TableWidgetRenderer<TableBlock> { /* ... */ }
class TableCellRenderer : TableWidgetRenderer<TableCell> { /* ... */ }

class TableRenderer(
 private val tableTitleRenderer: TableWidgetRenderer<TableBlock> = TableTitleRenderer(),
 private val tableCellRenderer: TableWidgetRenderer<TableCell> = TableCellRenderer(),
) : IBlockRenderer<TableBlock> {
 @Composable
 override fun Invoke(node: TableBlock, modifier: Modifier) {
  MarkdownTable(
   tableBlock = node,
   modifier = modifier,
   tableTitleRenderer = tableTitleRenderer,
   tableCellRenderer = tableCellRenderer,
  )
 }
}
```

`MarkdownTable` 会使用 `MarkdownTheme.tableTheme` 中的配置：

- `borderColor`、`borderThickness`
- `titleBackgroundColor`、`tableHeaderBackgroundColor`、`tableCellBackgroundColor`
- `cellPadding`、`cellTextStyle`、`headerTextStyle`、`copyTextStyle`、`shape`

并在列较多时构建支持水平滚动的表格。

**仅自定义表格标题**

你可以在复用默认单元格渲染的前提下，仅替换标题区域：

```kotlin
class CustomTableTitleRenderer : TableWidgetRenderer<TableBlock> {
 @Composable
 override fun invoke(node: TableBlock, modifier: Modifier) {
  // 渲染自定义标题 UI，例如标题 + 行列数 + 操作按钮
  Row(
   modifier = modifier
    .fillMaxWidth()
    .background(MaterialTheme.colorScheme.surfaceVariant)
    .padding(horizontal = 12.dp, vertical = 8.dp),
   horizontalArrangement = Arrangement.SpaceBetween,
   verticalAlignment = Alignment.CenterVertically,
  ) {
   Text("Table", style = MaterialTheme.typography.titleSmall)
   Text("${node.cells().size} rows")
  }
 }
}

val config = MarkdownRenderConfig.Builder()
 .addBlockRenderer(
  TableBlock::class.java,
  TableRenderer(
   tableTitleRenderer = CustomTableTitleRenderer(),
  ),
 )
 .build()
```

**自定义单元格渲染**

若只想改变单元格内部的展示方式（例如将所有数字内容居中、添加图标等）：

```kotlin
class CustomTableCellRenderer : TableWidgetRenderer<TableCell> {
 @Composable
 override fun invoke(node: TableCell, modifier: Modifier) {
  val theme = currentTheme()
  val isHeader = node.parent is TableRow && node.parent?.parent is TableHead
  SelectionContainer {
   MarkdownText(
    parent = node,
    modifier = Modifier,
    textAlign = TextAlign.Center, // 将所有单元格内容强制居中
    textStyle = if (isHeader) theme.tableTheme.headerTextStyle else theme.tableTheme.cellTextStyle,
   )
  }
 }
}

val config = MarkdownRenderConfig.Builder()
 .addBlockRenderer(
  TableBlock::class.java,
  TableRenderer(tableCellRenderer = CustomTableCellRenderer()),
 )
 .build()
```

你可以同时自定义 `tableTitleRenderer` 与 `tableCellRenderer`，在保留 `MarkdownTable` 布局与滚动行为的基础上，完全掌控表格的视觉表现。

---

##### MarkdownCodeBlock（代码块）

文件：`core/renders/MarkdownCodeBlock.kt`

该模块包含用于围栏代码块和缩进代码块的 Composable 组件：

- `CodeWidgetRenderer<T : Block>` —— 小型代码块子组件的函数式接口。
- `CopyRenderer<T>` —— 默认“复制”按钮。
- `CodeHeaderRenderer<T>` —— 默认头部区域，包含语言标签和复制按钮。
- `CodeContentRenderer<T>` —— 默认代码内容展示，带行号和可选滚动。
- `CodeRenderer<T : Block>` —— 组合头部 + 内容的基础实现。
- `FencedCodeBlockRenderer` / `IndentedCodeBlockRenderer` —— 针对围栏 / 缩进代码块的具体 `IBlockRenderer` 实现。

```kotlin
fun interface CodeWidgetRenderer<T : Block> {
 @Composable
 operator fun invoke(block: T, modifier: Modifier)
}

class FencedCodeBlockRenderer(
 renderCopyOverride: CodeWidgetRenderer<FencedCodeBlock>? = null,
 renderContentOverride: CodeWidgetRenderer<FencedCodeBlock>? = null,
 renderHeaderOverride: CodeWidgetRenderer<FencedCodeBlock>? = null,
) : IBlockRenderer<FencedCodeBlock> by CodeRenderer(
  renderCopyOverride,
  renderContentOverride,
  renderHeaderOverride,
 )
```

`CodeRenderer` 使用 `MarkdownTheme.codeBlockTheme` 中的配置：

- `backgroundColor`、`borderWidth`、`borderColor`、`shape`
- `blockModifier`、`headerModifier`
- `showHeader`、`showCopyButton`
- `codeTitleTextStyle`、`codeCopyTextStyle`
- `contentTheme`（代码字体、行号、内边距、高度、是否软换行等）。

**仅自定义复制按钮**

```kotlin
class IconCopyRenderer<T : Block> : CodeWidgetRenderer<T> {
 @Composable
 override fun invoke(block: T, modifier: Modifier) {
  val actionHandler = currentActionHandler()
  Icon(
   imageVector = Icons.Default.ContentCopy,
   contentDescription = "Copy code",
   modifier = modifier.clickable {
    actionHandler?.handleCopyClick(block)
   },
  )
 }
}

val config = MarkdownRenderConfig.Builder()
 .addBlockRenderer(
  FencedCodeBlock::class.java,
  FencedCodeBlockRenderer(
   renderCopyOverride = IconCopyRenderer(),
  ),
 )
 .build()
```

**自定义头部**

例如显示文件名、语言标签或自定义工具栏：

```kotlin
class CustomCodeHeaderRenderer<T : Block> : CodeWidgetRenderer<T> {
 @Composable
 override fun invoke(block: T, modifier: Modifier) {
  val theme = currentTheme().codeBlockTheme
  val language = (block as? FencedCodeBlock)?.info?.toString().orEmpty()

  Row(
   modifier =
    modifier
     .fillMaxWidth()
     .then(theme.headerModifier),
   horizontalArrangement = Arrangement.SpaceBetween,
   verticalAlignment = Alignment.CenterVertically,
  ) {
   Text(text = language.ifBlank { "Code" }, style = theme.codeTitleTextStyle)
   // 可以在这里添加更多操作（运行、复制、展开等）
  }
  HorizontalDivider(
   color = theme.borderColor,
   thickness = theme.borderWidth,
  )
 }
}

val config = MarkdownRenderConfig.Builder()
 .addBlockRenderer(
  FencedCodeBlock::class.java,
  FencedCodeBlockRenderer(
   renderHeaderOverride = CustomCodeHeaderRenderer(),
  ),
 )
 .build()
```

**自定义内容区域**

如果你希望完全掌控代码展示方式（例如接入语法高亮、仅展示部分代码并提供“展开更多”）：

```kotlin
class CustomCodeContentRenderer<T : Block> : CodeWidgetRenderer<T> {
 @Composable
 override fun invoke(block: T, modifier: Modifier) {
  val code = when (block) {
   is FencedCodeBlock -> block.contentChars.toString()
   is IndentedCodeBlock -> block.contentChars.toString()
   else -> return
  }
  // 将 LineNumberText 替换为你自己的 Composable
  MySyntaxHighlightedCode(
   code = code,
   modifier = modifier
    .verticalScroll(rememberScrollState()),
  )
 }
}

val config = MarkdownRenderConfig.Builder()
 .addBlockRenderer(
  FencedCodeBlock::class.java,
  FencedCodeBlockRenderer(renderContentOverride = CustomCodeContentRenderer()),
 )
 .build()
```

在构造 `FencedCodeBlockRenderer` / `IndentedCodeBlockRenderer` 时，你可以按需组合 `renderCopyOverride`、`renderHeaderOverride` 和 `renderContentOverride`，只接管你关心的部分。

---

##### MarkdownImage（图片）

`ImageMarkdownPlugin` (位于 `markdown-image` 模块中)

你可以通过向插件构造函数传递 `ImageTheme` 来自定义图片外观：

```kotlin
.addPlugin(
    ImageMarkdownPlugin(
        imageTheme = ImageTheme(
            alignment = Alignment.Center,
            contentScale = ContentScale.Inside,
            // ...
        )
    )
)
```

**ImageTheme 配置参数**：

```kotlin
data class ImageTheme(
    val alignment: Alignment = Alignment.Center,
    val contentScale: ContentScale = ContentScale.Inside,
    val shape: Shape = RoundedCornerShape(8.dp),
    val modifier: Modifier = Modifier,
    val errorPlaceholderColor: Color = Color(0xFFE0E0E0),
)
```

文件：`markdown-image/src/main/java/com/iffly/compose/markdown/image/MarkdownImage.kt`

`MarkdownImage` 是用于 Flexmark `Image` 节点的默认图片 Composable。它基于 Coil 3，并提供自定义加载与错误 UI 的钩子。

```kotlin
@Composable
fun MarkdownImage(
 node: Image,
 modifier: Modifier = Modifier,
 alignment: Alignment = Alignment.Center,
 contentScale: ContentScale = ContentScale.Inside,
 errorView: @Composable (image: Image, modifier: Modifier) -> Unit = { image, modifier ->
  MarkdownImageErrorView(
   modifier = modifier,
   altText = image.text?.toString() ?: image.title?.toString(),
  )
 },
 loadingView: @Composable (image: Image, modifier: Modifier) -> Unit = { image, modifier ->
  LoadingView(modifier = modifier)
 },
)
```

内部实现要点：

- 使用 `SubcomposeAsyncImage` 加载图片，`ImageRequest` 来自 `node.url`。
- 通过 `currentActionHandler()` 来转发图片点击事件（`handleImageClick(url, node)`）。
- 默认错误占位使用 `MarkdownImageErrorView`（`ic_image_error` 图标）。

**自定义加载 / 错误组件**

你可以只替换加载或错误 Composable，保留其他行为：

```kotlin
@Composable
fun CustomLoading(image: Image, modifier: Modifier) {
 Box(modifier, contentAlignment = Alignment.Center) {
  CircularProgressIndicator(strokeWidth = 2.dp)
 }
}

@Composable
fun CustomError(image: Image, modifier: Modifier) {
 Column(
  modifier = modifier,
  horizontalAlignment = Alignment.CenterHorizontally,
 ) {
  Icon(Icons.Default.BrokenImage, contentDescription = null)
  Text(text = image.text?.toString().orEmpty())
 }
}
```

**使用自定义图片渲染器**

若希望对所有 Markdown 图片都应用上述自定义组件，可以为 Flexmark `Image` 节点注册一个自定义块渲染器，并内部调用 `MarkdownImage`：

```kotlin
class CustomImageRenderer : IBlockRenderer<Image> {
 @Composable
 override fun Invoke(node: Image, modifier: Modifier) {
  MarkdownImage(
   node = node,
   modifier = modifier,
   loadingView = ::CustomLoading,
   errorView = ::CustomError,
  )
 }
}

val config = MarkdownRenderConfig.Builder()
 .addBlockRenderer(Image::class.java, CustomImageRenderer())
 .build()
```

这种模式与自定义表格和代码块类似：你只接管关心的小部件（加载 / 错误 / 内容），其余 Markdown 管线保持不变。

---

## 常见使用模式

### 小型 / 中等体量 Markdown 文本

- 使用**同步**版本的 `MarkdownView`。
- 默认的 `MarkdownRenderConfig.Builder().build()` 通常已经足够。

### 大型内存内容（例如从网络加载）

- 使用**异步**版本 `MarkdownView`。
- 提供 `onLoading` 与 `onError` Composable。
- 可选地设置 `parseDispatcher = Dispatchers.IO`（或保持 `null` 使用 `MarkdownThreadPool.dispatcher`）。

### 超大本地文件（书籍、长文档）

- 使用基于 `File` 的 `LazyMarkdownView`。
- 通过 `ChunkLoaderConfig` 在加载速度与内存占用之间做权衡调整。

### 高级自定义

- 创建可共享的 `MarkdownRenderConfig`：
  - 设置 `markdownTheme` 以匹配你的设计体系。
  - 添加插件以支持任务列表、LaTeX、自定义提示块等。
  - 按需注册自定义块渲染器或内联构建器。

