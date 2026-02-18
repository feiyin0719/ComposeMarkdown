# ComposeMarkdown API Reference
[English](API.md) |[简体中文](API_zh-CN.md)
> Detailed reference for the core composables and configuration types in the `markdown` module.
>
> - For an overview, installation and feature tour, see [README.md](../README.md).

## Table of Contents

- [Core Composables](#core-composables)
  - [MarkdownView (sync)](#markdownview-sync)
  - [MarkdownView (async)](#markdownview-async)
  - [MarkdownView (node)](#markdownview-node)
  - [LazyMarkdownView](#lazymarkdownview)
- [Configuration](#configuration)
  - [MarkdownRenderConfig](#markdownrenderconfig)
  - [MarkdownRenderConfig.Builder](#markdownrenderconfigbuilder)
  - [MarkdownTheme](#markdowntheme)
  - [ChunkLoaderConfig (overview)](#chunkloaderconfig-overview)
- [Plugins & Extension Points](#plugins--extension-points)
    - [IMarkdownRenderPlugin](#imarkdownrenderplugin)
    - [IBlockRenderer](#iblockrenderer)
    - [IInlineNodeStringBuilder](#iinlinenodestringbuilder)
    - [MarkdownInlineView](#markdowninlineview-inline-views)
    - [RenderRegistry & Core Renderers](#renderregistry--core-renderers)
    - [Custom Block and Inline Parsing](#custom-block-and-inline-parsing)
- [Common Usage Patterns](#common-usage-patterns)

---

## Core Composables

### MarkdownView (sync)

Synchronous parsing version. Parses the Markdown string inside a `remember` block on the UI thread.

Use this for **small or medium** Markdown documents where parsing time is negligible.

**Signature** (from `MarkdownView.kt`):

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

**Parameters**

- `content`: Markdown text to render.
- `markdownRenderConfig`: The rendering configuration, created via `MarkdownRenderConfig.Builder()`.
- `modifier`: Standard Compose modifier for sizing, padding, etc.
- `showNotSupportedText`: If `true`, unsupported elements are rendered as a textual fallback instead of being silently ignored.
- `actionHandler`: Optional handler for clickable actions (e.g. links, images, custom blocks). See project examples for concrete implementations.
- `onError`: Optional composable invoked when parsing fails. When `null`, errors are swallowed (nothing is rendered).

**Notes**

- Parsing is done immediately during composition; avoid using this overload for very large strings.
- If you need loading / error UI, prefer the async overload below.

---

### MarkdownView (async)

Asynchronous parsing version. Offloads parsing to a background dispatcher and exposes loading / error state hooks.

Recommended for **large content** or when you want a loading indicator and error UI.

**Signature** (from `MarkdownView.kt`):

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

**Additional Parameters**

- `parseDispatcher`: Dispatcher used for parsing. When `null`, the library uses `MarkdownThreadPool.dispatcher` (a background thread pool) to avoid blocking the main thread.
- `onLoading`: Optional composable displayed while parsing is in progress (e.g. a progress indicator or skeleton screen).

**Behavior**

- When `content` or the underlying parser instance changes, parsing restarts and `onLoading` is invoked (if provided).
- On success, the parsed AST is passed to the node-based `MarkdownView` under the hood.
- On failure, `onError` is invoked with the thrown exception.

**Example**

```kotlin
MarkdownView(
    content = largeMarkdown,
    markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
    modifier = Modifier.fillMaxSize(),
    showNotSupportedText = true,
    parseDispatcher = Dispatchers.IO, // or null to use MarkdownThreadPool.dispatcher
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

### MarkdownView (node)

Node-based version. Renders a **pre-parsed** Flexmark AST node.

Use this when you:

- Parse Markdown elsewhere (e.g. caching layer, background worker) and want to reuse the AST.
- Need to render the same content multiple times without re-parsing.

**Signature** (from `MarkdownView.kt`):

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

**Notes**

- The `Node` instance is usually produced by `markdownRenderConfig.parser.parse(content)`.
- This overload does not know about loading or error states; you manage them yourself around the call.

**Example**

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

Lazy loading composable for **very large markdown files** stored on disk.

Instead of parsing and rendering the entire document at once, it:

- Splits content into chunks.
- Parses and renders chunks on demand as the user scrolls.
- Uses `LazyColumn` with prefetching to balance memory usage and performance.

**Signature** (from `LazyMarkdownView.kt`):

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

**Parameters**

- `file`: Local markdown file to display. For assets or network content, copy/download to a local `File` first.
- `markdownRenderConfig`: Same config as used by `MarkdownView`.
- `modifier`: Applied to the internal `LazyColumn`.
- `showNotSupportedText`: Whether to show text for unsupported elements.
- `actionHandler`: Optional handler for actions inside the markdown content.
- `chunkLoaderConfig`: Controls how the file is split and parsed. There is a default `parserDispatcher` using `MarkdownThreadPool.dispatcher`.
- `nestedPrefetchItemCount`: How many items before/after the visible viewport should be prefetched to make scrolling smoother.

**Usage notes**

- Best suited for *read-only* long-form content like books, large docs, etc.
- For editable or frequently changing content, prefer the async `MarkdownView` with your own pagination or diffing.

---

## Configuration

### MarkdownRenderConfig

Holds everything needed to parse and render markdown:

- A `MarkdownTheme` describing typography, colors and component styles.
- A Flexmark `Parser` and `HtmlRenderer`.
- A `RenderRegistry` describing how nodes and blocks are rendered.

Instances are created via `MarkdownRenderConfig.Builder`.

```kotlin
val config = MarkdownRenderConfig.Builder()
    // configure theme, plugins, renderers...
    .build()
```

You typically create one `MarkdownRenderConfig` per screen or share it via `remember` / DI.

---

### MarkdownRenderConfig.Builder

Fluent builder to customize parsing, theming and rendering behavior.

**Key methods** (from `MarkdownRenderConfig.kt`):

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

Sets the visual theme for markdown rendering.

- If not set, the library uses `MarkdownTheme()` with default styles.
- Themes typically encapsulate typography, colors, spacings and decorations for:
  - Paragraphs, headings, emphasis, strong emphasis
  - Code blocks, inline code
  - Lists, block quotes, tables, and code blocks

#### addPlugin(plugin: IMarkdownRenderPlugin)

Registers a render plugin.

Plugins can:

- Contribute Flexmark extensions.
- Register extra block/inline renderers.
- Add custom block parsers or inline parser extensions.

Built-in core functionality is provided by an internal `CorePlugin` which is always added automatically.

#### addInlineNodeStringBuilder / addBlockRenderer

Low-level hooks to customize rendering of specific nodes/blocks.

- `addInlineNodeStringBuilder(nodeClass, builder)`: defines how a given inline node type is converted to text spans.
- `addBlockRenderer(blockClass, renderer)`: defines how a given block node type is rendered as Compose UI.

These are typically used inside your own `IMarkdownRenderPlugin` implementation, but can also be used directly on the builder if you just need a small adjustment.

#### markdownTextRenderer / markdownContentRenderer

Advanced customization points:

- `markdownTextRenderer`: Override how text nodes are rendered (e.g., custom text shaping or layout).
- `markdownContentRenderer`: Override rendering of higher-level content nodes.

Most users won't need these; theme and plugins are usually enough.

#### addBlockParserFactory / addInlineContentParserFactory / addDelimiterProcessor / addExtension

Flexmark-level extension APIs:

- `addBlockParserFactory`: Register custom block parsers (for new block syntaxes).
- `addInlineContentParserFactory`: Register inline parser extensions.
- `addDelimiterProcessor`: Handle custom delimiters (e.g., for new emphasis markers).
- `addExtension`: Add raw Flexmark `Extension` instances.

These are mainly intended for advanced/custom markdown dialects.

---

### MarkdownTheme

`MarkdownTheme` (from `com.iffly.compose.markdown.style.MarkdownTheme`) is the **central theme model**
for how markdown content is rendered in Compose.

It controls:

- Base text style (font size, line height, family, color)
- Emphasis / strong emphasis / strikethrough / subscript styles
- Link styles (normal / hovered / focused / pressed)
- Heading styles (H1–H6)
- Layout & look of lists, images, block quotes, spacers, tables, and code blocks

#### Data structure (simplified)

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

Key nested types (from `MarkdownTheme.kt`):

- `BlockQuoteTheme` – border color/width, background, shape, padding
- `SpacerTheme` – whether to show spacers between blocks, height
- `ListTheme` – bullet/number marker spacing and style
- `CodeBlockTheme` – code block text style, background, padding, overflow, etc.

> **Note**:
> - `TableTheme` is now part of `TableMarkdownPlugin` configuration, not `MarkdownTheme`.
> - `ImageTheme` is now part of `ImageMarkdownPlugin` configuration, not `MarkdownTheme`.

#### Heading levels

Headings are configured via the `headStyle` map. Keys are integer levels 1–6 and are exposed as
constants on `MarkdownTheme`:

```kotlin
MarkdownTheme.HEAD1 // level 1 (H1)
MarkdownTheme.HEAD2 // level 2 (H2)
// ... up to HEAD6
```

Example override:

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

#### Link styles

`link` uses `TextLinkStyles` from Compose to describe the visual states of links:

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

#### Lists

`ListTheme` controls spacing and marker text style:

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

Typical customisation:

- Increase `markerSpacerWidth` when you use wide bullets/checkboxes
- Adjust `markerTextStyle` to better align with the body text

#### Images

Moved to `ImageMarkdownPlugin`. See the [Plugins](#plugins) section for details on how to configure `ImageTheme`.


#### Block quotes

`BlockQuoteTheme` decides the look of `>` quoted blocks:

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

You can adjust it to match Material3 surface styles or your brand color.

#### Tables and code blocks

`TableTheme` and `CodeBlockTheme` cover more advanced layouts such as tables and fenced code blocks.
They expose:

- Border color, thickness, row background colors
- Cell/header text styles, padding, shape
- Code text style, background, padding, overflow behaviours, etc.

When you need a full reference of fields, open `MarkdownTheme.kt` in your IDE. The main idea is that
you do **all visual tuning** through `MarkdownTheme`, then provide it to
`MarkdownRenderConfig.Builder().markdownTheme(theme)`.

**Putting it together:**

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

### ChunkLoaderConfig (overview)

`ChunkLoaderConfig` (from `com.iffly.compose.markdown.chunkloader`) controls how `LazyMarkdownView` splits and parses large files.

Typical fields include:

- `parserDispatcher`: Dispatcher used for background parsing (defaults to `MarkdownThreadPool.dispatcher`).
- Other values controlling:
  - Initial number of lines/chunks to load.
  - Incremental load size when the user scrolls.
  - Maximum number of chunks kept in memory.

The exact fields may evolve; refer to the `ChunkLoaderConfig` source for the latest list.

**Example** (conceptual):

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

## Plugins & Extension Points

### IMarkdownRenderPlugin

Entry point for adding features to the markdown engine.

A plugin can:

- Provide Flexmark extensions.
- Register inline string builders and block renderers.
- Contribute block parser factories and inline parser extension factories.

The exact interface is defined in `IMarkdownRenderPlugin` (see the `config` or `core` packages).

Plugins are added via `MarkdownRenderConfig.Builder.addPlugin`.

### Custom Block and Inline Parsing

The library is built on Flexmark. You can extend parsing and rendering of Block (block-level) and Inline nodes by implementing `IMarkdownRenderPlugin`. In practice, you usually only need to know a few main entry points:

- **Custom Block parsing**:
    - Use Flexmark `CustomBlockParserFactory` to define new block syntaxes (for example an `:::alert` style block).
    - Return your factories from `IMarkdownRenderPlugin.blockParserFactories()` so the parser can recognize these blocks.
    - Implement `IBlockRenderer<YourBlock>` for your custom Block type and register it in `blockRenderers()` to provide corresponding Compose UI rendering.

- **Custom Inline parsing**:
    - Implement `InlineParserExtensionFactory` to define new inline syntaxes (for example `@user`, `#tag`, or `==highlight==`).
    - Return your factories from `IMarkdownRenderPlugin.inlineContentParserFactories()` so the parser can find these markers inside text.
    - If you need to control how these inline nodes are turned into text or rich inline content, register an `IInlineNodeStringBuilder` for the corresponding Flexmark `Node` type via `inlineNodeStringBuilders()`.

The typical flow is: **use Flexmark extension points to define syntax → register parsing and rendering strategies in `IMarkdownRenderPlugin` → enable the plugin via `MarkdownRenderConfig.Builder.addPlugin(...)`**. You can refer to the `CustomMarkdownPlugin` sample for a concrete end-to-end example.

For more details about Flexmark extension points and parser customization, see the official docs:
[flexmark-java](https://github.com/vsch/flexmark-java)

### IBlockRenderer

Responsible for rendering a specific Flexmark `Block` node type as Compose UI.

- Registered via `addBlockRenderer` on the builder or through a plugin.
- Allows you to provide completely custom visuals for certain block elements.

**Interface signature (simplified)** (see `IBlockRenderer.kt`):

```kotlin
interface IBlockRenderer<in T> where T : Block {
        @Composable
        fun Invoke(
                node: T,
                modifier: Modifier,
        )
}
```

**Parameters**

- `T`: The block node type to render. Must be a Flexmark `Block` subclass such as `Paragraph`, `Heading`, `BlockQuote`, `TableBlock`, etc.
- `node`: The concrete block node instance. Typically you will:
    - Iterate its children and delegate to `MarkdownContent` / `MarkdownText` or other existing composables;
    - Or read block‑specific fields (for example the language info on `FencedCodeBlock.info`).
- `modifier`: The `Modifier` passed from the outer markdown pipeline. Implementations should **apply this modifier first** and then `.then(...)` any custom modifiers, rather than discarding it, so that layout and outer containers remain consistent.

**Implementation notes**

- Prefer using `MarkdownTheme` to configure **styling** (colors, typography, paddings) and let custom block renderers focus on **structure and layout** (rows, columns, cards, backgrounds, spacing, etc.).
- If your renderer needs to trigger actions (copy, open link, expand, etc.), obtain the current `ActionHandler` via `currentActionHandler()` instead of storing your own reference, so you stay aligned with the library's lifecycle.
- Once registered, a custom `IBlockRenderer` **fully replaces** the default renderer for that `Block` type. Make sure you handle all relevant child nodes or explicitly delegate back to the default behaviour to avoid missing content.

### IInlineNodeStringBuilder

Responsible for converting a specific inline `Node` into styled text spans.

- Registered via `addInlineNodeStringBuilder`.
- Used internally by the text renderer when building annotated strings.

**Interface signature (simplified)** (see `IInlineNodeStringBuilder.kt`):

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
                measureContext: TextMeasureContext,
        )
}
```

**Parameters**

- `T`: The inline node type to handle, e.g. `Text`, `Emphasis`, `StrongEmphasis`, `Link`, etc.
- `node`: The inline node instance. You read its contents (text, URL, emphasis level, etc.) to decide what spans or inline content to build.
- `inlineContentMap`: A mutable map used to register rich inline content:
    - Keys are unique strings; values are `MarkdownInlineView` instances.
    - Builders typically insert `MarkdownRichTextInlineContent` here and then reference it from the `AnnotatedString` via `appendInlineContent(key, placeholder)`.
    - Be aware: **keys behave like stable Compose keys** — if you change only the content and keep the same key, existing inline content will not be recreated.
- `markdownTheme`: The active `MarkdownTheme`. Prefer pulling `SpanStyle` / `ParagraphStyle` from here instead of hard‑coding colours, font sizes, etc., so that your builder stays theme‑aware.
- `actionHandler`: Optional handler for interactions:
    - In the built‑in `Link` builder it is used to create `MarkdownLinkInteractionListener`.
    - For custom inline components (mentions, tags, etc.) you can use it to forward click events to your app logic.
- `indentLevel`: Current indentation level (mainly relevant for nested lists):
    - Can be used to compute leading indentation or marker spacing;
    - Often combined with `ListTheme.markerSpacerWidth` when rendering list bullets and numbers.
- `isShowNotSupported`: Controls whether to emit a visible placeholder for unsupported nodes:
    - When `true`, you may output a fallback like `"[unsupported: NodeName]"` instead of silently skipping the node;
    - When `false`, you can safely ignore unsupported nodes.
- `renderRegistry`: The render registry for the current configuration:
    - Used by helper utilities such as `buildChildNodeAnnotatedString` to recursively render child nodes;
    - Lets your builder compose with other registered inline builders instead of re‑implementing traversal.
- `measureContext`: Text measurement context:
    - Useful when your builder needs the actual text size to make layout decisions (truncation, wrapping, placeholder width, etc.);
    - Most simple builders can ignore it and rely on default metrics.

**Implementation notes**

- When adding styling, prefer using `pushStyle` / `pop`, `withLink`, and similar `AnnotatedString.Builder` helpers instead of rebuilding the whole string from scratch.
- For nodes that can contain children (e.g. `Link`, `Emphasis`), delegate to the common "build children" helper that uses `renderRegistry` instead of manually handling every possible child type; this way new node types added later are handled automatically.
- Avoid storing `Context` or Compose state inside builders. They should be pure string‑building utilities; side‑effects and state belong in composables or the `ActionHandler` layer.

### MarkdownInlineView (inline views)

`MarkdownInlineView` is a small sealed interface used to represent **inline composable content**
that can be embedded inside text. It is primarily consumed by inline node string builders when they
need to register rich inline content instead of plain styled text.

Defined in `render/MarkdownInlineView.kt`:

```kotlin
sealed interface MarkdownInlineView {
    data class MarkdownRichTextInlineContent(
        val inlineContent: RichTextInlineContent,
    ) : MarkdownInlineView
}
```

Key points:

- `MarkdownInlineView` is the value type used in `inlineContentMap: MutableMap<String, MarkdownInlineView>`
  parameters of `IInlineNodeStringBuilder` implementations.
- `MarkdownRichTextInlineContent` wraps a `RichTextInlineContent` instance, which knows how to render
  a composable inline element (icon, chip, badge, custom widget, etc.).
- Inline builders register entries in the `inlineContentMap` with a unique key; the text renderer
  then references those keys from the `AnnotatedString` to display rich inline content alongside
  normal text.
- **notice**: If you only change the content and do not change the key in `inlineContentMap`, the inline content will not update.

**Conceptual usage in a custom inline builder**

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
        measureContext: TextMeasureContext,
    ) {
        // 1. Register rich inline content under a unique key
        val key = "icon-${node.id}"
        inlineContentMap[key] =
            MarkdownInlineView.MarkdownRichTextInlineContent(
                inlineContent = /* build RichTextInlineContent for this icon */
                    buildIconInlineContent(node),
            )

        // 2. Append a placeholder character with that key as an annotation
        appendInlineContent(key, " ")
    }
}
```

You typically do not manipulate `MarkdownInlineView` directly outside of custom
`IInlineNodeStringBuilder` implementations. It forms part of the bridge between flexmark inline
nodes and Compose rich text inline content.

#### RichTextInlineContent types and use cases

`MarkdownRichTextInlineContent` internally wraps a `RichTextInlineContent` (defined in
`widget/richtext/RichTextInlineContent.kt`). There are currently two variants:

- `EmbeddedRichTextInlineContent`
    - **Characteristics**
        - Uses a `Placeholder` to define width, height and alignment, and participates in the same paragraph layout as the surrounding text.
        - The optional `adjustSizeByContent` flag allows fine-tuning the size based on the actual content.
        - `content: @Composable (String) -> Unit` receives a key or placeholder text and renders a small inline composable (icon, badge, etc.).
    - **Recommended scenarios**
        - Small inline icons (e.g. "⚠", "✔"), emoji, status dots, count badges and other components that should appear **on the same line** as text.
        - Custom views that must align precisely with the text baseline and line height.
        - For example, showing an info icon before `**[info] text**`, or a verification badge after a username.

- `StandaloneInlineContent`
    - **Characteristics**
        - Uses a `modifier: Modifier = Modifier` to control the overall layout and size.
        - `content: @Composable (modifier: Modifier) -> Unit` renders a fully custom Compose component.
        - In the text layout it is treated as a **separate paragraph/block**, not mixed inline with text.
    - **Recommended scenarios**
        - Inserting whole custom blocks into Markdown, such as ad cards, button areas, statistics cards, rich media modules, etc.
        - Content that should have vertical spacing and be laid out independently from normal paragraphs.
        - For example, inserting a recommendation card, banner or voting widget between two Markdown paragraphs.

In a custom `IInlineNodeStringBuilder`:

- Use `EmbeddedRichTextInlineContent` when the component should appear **inline with text** and share the same line.
- Use `StandaloneInlineContent` when the component should occupy **its own paragraph/block** in the layout.

### RenderRegistry & Core Renderers

At the heart of the rendering pipeline is `RenderRegistry`, which decides **how each flexmark node
is turned into Compose UI**.

`RenderRegistry` is constructed inside `MarkdownRenderConfig.Builder.build()` and receives:

- `blockRenderers: Map<Class<out Block>, IBlockRenderer<*>>`
- `inlineNodeStringBuilders: Map<Class<out Node>, IInlineNodeStringBuilder<*>>`
- Optional `MarkdownContentRenderer`
- Optional `MarkdownTextRenderer`

You usually interact with it **indirectly** via:

- `Builder.addBlockRenderer(...)`
- `Builder.addInlineNodeStringBuilder(...)`
- Plugins that implement `IMarkdownRenderPlugin` and contribute their own renderers.

#### Built‑in core renderers (from `com.iffly.compose.markdown.core.renders`)

The library ships with a set of built‑in block renderers and inline node string builders that
implement the default CommonMark/GFM behaviour. Understanding them helps when you want to
**override a specific part** of the rendering pipeline.

##### TextBlockRenderer / ParagraphRenderer / HeadingRenderer

File: `core/renders/TextBlockRenderer.kt`

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

- `TextBlockRenderer` is a generic block renderer that delegates to `MarkdownText` to render all
  inline children of a block (paragraphs, headings, etc.).
- `ParagraphRenderer` and `HeadingRenderer` are concrete implementations used for `Paragraph` and
  `Heading` nodes.

If you only want to **tweak text styling**, prefer changing `MarkdownTheme` instead of overriding
these renderers. Override them only when you need completely different layouts for paragraphs or
headings.

##### BlockQuoteRenderer

File: `core/renders/BlockQuoteRenderer.kt`

```kotlin
class BlockQuoteRenderer : IBlockRenderer<BlockQuote> {
    @Composable
    override fun Invoke(node: BlockQuote, modifier: Modifier) {
        MarkdownBlockQuote(node = node, modifier = modifier)
    }
}
```

`MarkdownBlockQuote` uses the current `MarkdownTheme` to style quotes:

- `theme.blockQuoteTheme.borderColor / borderWidth`
- `backgroundColor`, `shape`, `padding`
- `theme.spacerTheme.spacerHeight` for top/bottom spacing

It then iterates over children and calls `MarkdownContent` to render them. To customize block quotes
visually, adjust `MarkdownTheme.blockQuoteTheme`; to radically change their layout, you can provide
your own `IBlockRenderer<BlockQuote>` and register it via `addBlockRenderer`.

##### ParagraphNodeStringBuilder

File: `core/renders/ParagraphNodeStringBuilder.kt`

```kotlin
class ParagraphNodeStringBuilder : CompositeChildNodeStringBuilder<Node>() {
    override fun getParagraphStyle(
        node: Node,
        markdownTheme: MarkdownTheme,
    ): ParagraphStyle? = markdownTheme.getNodeParagraphStyle(node)
}
```

This is a default inline string builder for paragraph‑like nodes. It:

- Delegates to `markdownTheme.getNodeParagraphStyle(node)` (a helper in the util package) to pick
  a `ParagraphStyle` based on the node type and current theme.
- Uses `CompositeChildNodeStringBuilder` to iterate over child nodes and compose their text.

If you want to change paragraph alignment, spacing, or other paragraph‑level behaviour globally,
you can adjust how `getNodeParagraphStyle` reads from `MarkdownTheme` (or provide your own
`CompositeChildNodeStringBuilder` implementation).

##### HeadingNodeStringBuilder

File: `core/renders/HeadingNodeStringBuilder.kt`

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

For headings, this builder:

- Uses `markdownTheme.getNodeSpanStyle(node)` to choose a `SpanStyle` (typically mapped from
  `MarkdownTheme.headStyle` for H1–H6).
- Uses `markdownTheme.getNodeParagraphStyle(node)` for paragraph‑level styling.

This is the link between `MarkdownTheme.headStyle` and an actual rendered heading. To customise
heading typography, colors or spacing, configure `MarkdownTheme` instead of replacing this
string builder.

##### LinkNodeStringBuilder

File: `core/renders/LinkNodeStringBuilder.kt`

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
        measureContext: TextMeasureContext,
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
                measureContext,
            )
        }
    }
}
```

Key points:

- It wraps the link’s children inside a `LinkAnnotation.Url` with styles from `markdownTheme.link`.
- If an `ActionHandler` is provided, it creates a `MarkdownLinkInteractionListener` so that link
  clicks can be forwarded to your app code.
- Actual inline contents are produced via `buildChildNodeAnnotatedString`, which uses other
  inline builders registered in `RenderRegistry`.

To change link behaviour:

- For **visuals**, adjust `MarkdownTheme.link`.
- For **interaction**, implement your own `ActionHandler` to handle link clicks.
- For very custom links, you can provide your own `IInlineNodeStringBuilder<Link>` and register it
  with `addInlineNodeStringBuilder`.

##### MarkdownTable (tables)

`TableMarkdownPlugin` (in `markdown-table` module)

File: `markdown-table/src/main/java/com/iffly/compose/markdown/table/MarkdownTable.kt`

This module provides a fully featured table renderer based on flexmark's `TableBlock` extension and
a small Compose `Table` widget DSL.

Key types:

- `TableWidgetRenderer<T : Node>` – fun interface for rendering **parts** of a table.
- `TableTitleRenderer` – default renderer for a table title/header area.
- `TableCellRenderer` – default renderer for each table cell.
- `TableRenderer` – `IBlockRenderer<TableBlock>` that wires everything together.
- `MarkdownTable` – top-level composable that lays out the table.

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

`MarkdownTable` uses `MarkdownTheme.tableTheme` for:

- `borderColor`, `borderThickness`
- `titleBackgroundColor`, `tableHeaderBackgroundColor`, `tableCellBackgroundColor`
- `cellPadding`, `cellTextStyle`, `headerTextStyle`, `copyTextStyle`, `shape`

and builds a horizontally scrollable table when there are many columns.

**Customising table title only**

You can override just the title widget while reusing default cell rendering:

```kotlin
class CustomTableTitleRenderer : TableWidgetRenderer<TableBlock> {
    @Composable
    override fun invoke(node: TableBlock, modifier: Modifier) {
        // Render custom title UI, e.g. title + row/column count + actions
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

**Customising cell widget**

To change only the way cells are rendered (e.g. center all numeric values, add icons, etc.):

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
                textAlign = TextAlign.Center, // force center for all cells
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

You can combine both `tableTitleRenderer` and `tableCellRenderer` overrides if you need full control
but still want to reuse `MarkdownTable`’s layout and scrolling behaviour.

---

##### MarkdownCodeBlock (code blocks)

File: `core/renders/MarkdownCodeBlock.kt`

This module contains composable widgets for fenced and indented code blocks:

- `CodeWidgetRenderer<T : Block>` – fun interface for small code block widgets.
- `CopyRenderer<T>` – default "Copy" button.
- `CodeHeaderRenderer<T>` – default header with language label and copy button.
- `CodeContentRenderer<T>` – default code content with line numbers and optional scroll.
- `CodeRenderer<T : Block>` – base implementation combining header + content.
- `FencedCodeBlockRenderer` / `IndentedCodeBlockRenderer` – concrete `IBlockRenderer` implementations.

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

`CodeRenderer` uses `MarkdownTheme.codeBlockTheme` for:

- `backgroundColor`, `borderWidth`, `borderColor`, `shape`
- `blockModifier`, `headerModifier`
- `showHeader`, `showCopyButton`
- `codeTitleTextStyle`, `codeCopyTextStyle`
- `contentTheme` (code font, line numbers, padding, height, softWrap, etc.)

**Customising only the copy button**

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

**Customising the header**

For example, show file name, language tag, or a custom toolbar:

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
            // You can add more actions here (run, copy, expand, etc.)
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

**Customising the content widget**

If you want to fully control how code is displayed (e.g. plug in a syntax highlighter, or show only
part of the code with "show more"):

```kotlin
class CustomCodeContentRenderer<T : Block> : CodeWidgetRenderer<T> {
    @Composable
    override fun invoke(block: T, modifier: Modifier) {
        val code = when (block) {
            is FencedCodeBlock -> block.contentChars.toString()
            is IndentedCodeBlock -> block.contentChars.toString()
            else -> return
        }
        // Replace LineNumberText with your own composable
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

You can mix and match `renderCopyOverride`, `renderHeaderOverride`, and `renderContentOverride`
when constructing `FencedCodeBlockRenderer` / `IndentedCodeBlockRenderer`, depending on which part
of the widget you want to take over.

---

##### MarkdownImage (images)

`ImageMarkdownPlugin` (in `markdown-image` module)

You can customize the image appearance by passing an `ImageTheme` to the plugin constructor:

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

**ImageTheme Configuration**:

```kotlin
data class ImageTheme(
    val alignment: Alignment = Alignment.Center,
    val contentScale: ContentScale = ContentScale.Inside,
    val shape: Shape = RoundedCornerShape(8.dp),
    val modifier: Modifier = Modifier,
    val errorPlaceholderColor: Color = Color(0xFFE0E0E0),
)
```

File: `markdown-image/src/main/java/com/iffly/compose/markdown/image/MarkdownImage.kt`

`MarkdownImage` is the default image composable used for flexmark `Image` nodes. It is built on top
of Coil 3 and exposes hooks to customise loading and error UI.

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

Internally it uses:

- `SubcomposeAsyncImage` with an `ImageRequest` built from `node.url`.
- `currentActionHandler()` to forward image clicks via `handleImageClick(url, node)`.
- `MarkdownImageErrorView` as the default error placeholder (`ic_image_error` drawable).

**Custom loading / error widgets**

You can replace just the loading or error composables while keeping the rest of the behaviour:

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

**Using a custom image renderer**

To apply these custom widgets to all markdown images, you can register a custom block renderer for
flexmark `Image` nodes that wraps `MarkdownImage`:

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

This pattern mirrors how you customise tables and code blocks: you override only the widget you care
about (loading/error/content), while leaving the rest of the markdown pipeline unchanged.

---

## Common Usage Patterns

### Small / medium markdown text

- Use **synchronous** `MarkdownView`.
- Default `MarkdownRenderConfig.Builder().build()` is usually enough.

### Large in-memory content (e.g. loaded from network)

- Use **asynchronous** `MarkdownView`.
- Provide `onLoading` and `onError` composables.
- Optionally set `parseDispatcher = Dispatchers.IO` (or leave `null` to use `MarkdownThreadPool.dispatcher`).

### Very large local files (books, long documents)

- Use `LazyMarkdownView` with a `File`.
- Tune `ChunkLoaderConfig` to balance load speed vs memory.

### Advanced customization

- Create a shared `MarkdownRenderConfig`:
  - Set `markdownTheme` to match your design system.
  - Add plugins for tasks, LaTeX, custom alerts, etc.
  - Optionally register custom block renderers or inline builders.
