# Changelog

## Unreleased / 未发布

### New Features / 新增功能

- Added `appendMarkdownInlineContent()` to atomically register embedded or standalone inline content and append its annotation, with collision-safe IDs by default and explicit overwrite support
- 新增 `appendMarkdownInlineContent()`，以原子方式注册 embedded 或 standalone inline content 并追加 annotation；默认安全处理 ID 冲突，同时支持显式覆盖
- Rebuilt `LazyMarkdownView` around line sources, trailing-block preservation, node watermarks, node/source-line cache limits, bidirectional AST recycling, and structured loading states
- 基于行数据源、尾部 block 保留、节点水位、node/source-line 双重缓存上限、双向 AST 回收和结构化 loading 状态重构 `LazyMarkdownView`
- Added append-only streaming parsing to `MarkdownView` and `MarkdownText`, with tail-block reparsing, final full parsing, and custom `StreamingMarkdownParser` support
- 为 `MarkdownView` 与 `MarkdownText` 新增 append-only streaming 解析，支持尾部 block 重解析、结束时全量解析及自定义 `StreamingMarkdownParser`

---

## 0.2.0

### New Features

#### Render Dependency Context
- Added `renderDependencies` to the public Markdown rendering components so callers can provide services and other external objects to custom renderers
- Added `currentRenderDependencies()` for Composable renderers and `NodeStringBuilderContext.renderDependencies` for node string builders

#### Stable Text-Mode Content Identity
- Replaced identity-only text-mode block keys with bounded, iterative content hashes
- Kept repeated blocks with identical content distinct so each retains its own renderer and interaction node
- Added `NodeContentHashProvider` so custom nodes can define the fields that affect their rendered identity

#### Shared Annotated Text Pipeline
- Added `rememberMarkdownAnnotatedStringResult()` as the shared text and inline-content builder for `MarkdownText` and `MarkdownInlineText`
- Text-mode registry overrides now flow through the Markdown CompositionLocal provider

### Bug Fixes

#### Placeholder Geometry Under Font Scaling
- Added a linear pixel-to-sp conversion for measured placeholder dimensions, preserving inline content geometry under non-linear Android font scaling
- Updated adaptive inline content and fixed-size LaTeX placeholders to use the new conversion

#### LaTeX Image Refresh
- Rebuilds LaTeX drawables when either the formula or rendering configuration changes

### Performance Improvements

- Made `MarkdownRenderConfig` immutable and kept custom/default instances stable with `remember`
- Cached text-mode render registries and syntax-highlighted code results
- Avoided line-layout state and offset calculations when line numbers are hidden
- Reused RichText inline-content groups, adaptive entries, segment callbacks, and immutable measurement maps

---

## 更新日志

## 0.2.0

### 新增功能

#### 渲染依赖上下文
- 所有公开 Markdown 渲染组件新增 `renderDependencies`，调用方可向自定义 renderer 提供服务或其他外部对象
- Composable renderer 可通过 `currentRenderDependencies()` 获取依赖，节点字符串构建器可通过 `NodeStringBuilderContext.renderDependencies` 获取依赖

#### 稳定的文本模式内容标识
- 将仅依赖对象 identity 的文本模式块 key 替换为有节点数上限的迭代式内容哈希
- 内容相同的重复块仍保持独立，确保每个块保留各自的 renderer 与交互节点
- 新增 `NodeContentHashProvider`，自定义节点可声明影响渲染标识的字段

#### 共享 AnnotatedString 构建流程
- 新增 `rememberMarkdownAnnotatedStringResult()`，供 `MarkdownText` 与 `MarkdownInlineText` 共用文本及内联内容构建流程
- 文本模式 registry 覆盖改为通过 Markdown CompositionLocal provider 向下传递

### Bug 修复

#### 字体缩放下的 Placeholder 几何尺寸
- 新增用于测量型 placeholder 的线性 px-to-sp 转换，在 Android 非线性字体缩放下保持内联内容几何尺寸
- Adaptive inline content 与固定尺寸 LaTeX placeholder 已改用新转换

#### LaTeX 图片刷新
- 公式或渲染配置变化时重新构建 LaTeX drawable

### 性能优化

- 将 `MarkdownRenderConfig` 改为不可变对象，并通过 `remember` 保持自定义及默认实例稳定
- 缓存文本模式 render registry 与语法高亮结果
- 关闭行号时不再维护文本布局状态或计算行偏移
- 复用 RichText 内联内容分组、adaptive entries、segment 回调与测量阶段 immutable map

---

## 0.1.9

### New Features

#### Syntax Highlighting (`BasicSyntaxHighlighter`)
Added `BasicSyntaxHighlighter`, a built-in regex-based implementation of `CodeAnnotator` that applies token-level syntax highlighting to code blocks.
- Supports Kotlin, Java, Python, JavaScript, TypeScript, Go, Rust, C/C++, Swift, and more
- Six configurable token categories: keyword, string, comment, number, annotation, type
- `CodeColors` data class added to the `style` package for per-token color customization
- `codeColors` field added to `CodeBlockTheme` for theme-level color configuration
- All code block renderers default to `BasicSyntaxHighlighter()`; pass `null` to disable highlighting

#### List Marker Alignment (`MarkerAlignment`)
Added `MarkerAlignment` enum to `ListTheme` for controlling the vertical alignment of list markers relative to the first line of content.
- Options: `Top`, `Center`, `Bottom`, `Baseline` (default)
- Uses `FirstLineMetrics` for precise baseline-relative positioning

### Improvements

#### List Marker Rendering
Replaced the `Row + Text + Spacer` layout for list markers with a custom `Layout + drawBehind` canvas approach. Markers no longer participate in text selection, eliminating spurious newlines when copying list content.

#### `IBlockRenderer.supportTextMode()`
Added `supportTextMode()` default method to `IBlockRenderer`. Renderers returning `false` are excluded from `RenderRegistry.textModeRegistry()`, giving fine-grained control over which blocks render in text mode (e.g. `MarkdownText`).

#### Text-Mode Spacer Handling
`DocumentInlineStringBuilder` now respects the `showSpacer` theme setting when building text-mode output, preventing unwanted blank lines in single-string rendering.

---

## 更新日志

## 0.1.9

### 新增功能

#### 语法高亮（`BasicSyntaxHighlighter`）
新增内置正则表达式语法高亮器 `BasicSyntaxHighlighter`，实现 `CodeAnnotator` 接口，对代码块进行 Token 级别着色。
- 支持 Kotlin、Java、Python、JavaScript、TypeScript、Go、Rust、C/C++、Swift 等语言
- 六种可配置 Token 类型：关键字、字符串、注释、数字、注解、类型
- `style` 包新增 `CodeColors` 数据类，用于自定义各 Token 颜色
- `CodeBlockTheme` 新增 `codeColors` 字段，支持主题层面统一配置颜色
- 所有代码块渲染器默认使用 `BasicSyntaxHighlighter()`，传入 `null` 可禁用高亮

#### 列表标记对齐（`MarkerAlignment`）
`ListTheme` 新增 `MarkerAlignment` 枚举，控制列表标记相对于内容首行的垂直对齐方式。
- 可选值：`Top`、`Center`、`Bottom`、`Baseline`（默认）
- 使用 `FirstLineMetrics` 实现精确的基线对齐定位

### 改进

#### 列表标记渲染
将列表标记的 `Row + Text + Spacer` 布局替换为 `Layout + drawBehind` Canvas 绘制方案。标记不再参与文字选择，彻底消除复制列表内容时产生的多余换行符。

#### `IBlockRenderer.supportTextMode()`
`IBlockRenderer` 接口新增 `supportTextMode()` 默认方法。返回 `false` 的渲染器将从 `RenderRegistry.textModeRegistry()` 中排除，可精细控制哪些块参与文本模式渲染（如 `MarkdownText`）。

#### 文本模式 Spacer 处理
`DocumentInlineStringBuilder` 在构建文本模式输出时现遵循主题的 `showSpacer` 设置，避免单字符串渲染时产生多余空行。

---

## 0.1.8

### Bug Fixes

#### `AutoLineHeightText` Bounds Check
- Added guard check for `startIndex < contentEnd` in `buildAdjustLineHeightText`
  to prevent `IllegalArgumentException` when trailing newline causes `contentEnd`
  to equal or precede `startIndex` (e.g. placeholder alternateText containing `\n`)

---

## 更新日志

## 0.1.8

### Bug 修复

#### `AutoLineHeightText` 边界检查
- 在 `buildAdjustLineHeightText` 中增加 `startIndex < contentEnd` 的边界检查，
  防止尾部换行符导致 `contentEnd` 等于或小于 `startIndex` 时抛出
  `IllegalArgumentException`（例如 placeholder 的 alternateText 包含 `\n` 的情况）

---

## 0.1.7

### New Features

#### `MarkdownText` Component
A new `MarkdownText` composable that complements the existing `MarkdownView`.
Instead of rendering each block as a separate composable in a Column, it renders
the entire Markdown document through a single `RichText` composable.
- **Supports `maxLines` and `overflow`** — ideal for message previews, list summaries,
  and any scenario that requires line-limited rendering
- Enables cross-paragraph text selection
- Non-text blocks (code blocks, block quotes, lists, etc.) are embedded as InlineContent
  using existing `IBlockRenderer` implementations

```kotlin
MarkdownText(
    text = markdownString,
    maxLines = 3,
    overflow = TextOverflow.Ellipsis
)
```

#### `IBlockRenderer.shouldSkipRender()`
Added `shouldSkipRender(node)` to the `IBlockRenderer` interface (defaults to `false`).
When it returns `true`, the block and its surrounding spacers are skipped entirely,
allowing conditional filtering of specific blocks.

### Bug Fixes

#### Table inline parser cross-cell matching
Replaced `TablesExtension` with `SafeTablesExtension`. The original implementation
passed the entire row to the inline parser, which allowed custom delimiters (e.g. LaTeX `$`)
to match across cell boundaries. The new implementation splits rows into individual cells
first, then runs inline parsing on each cell independently.

#### `AutoLineHeightText` recomposition stability
- Fixed `AnnotatedString` equality check: excluded `LinkAnnotation` listeners that
  change on every recomposition, which caused the line height adjustment calculation to be skipped
- Fixed trailing newline removal logic: replaced unconditional removal with a binary
  search check to avoid truncating `ParagraphStyle` annotations

---

## 更新日志

### 新增功能

#### `MarkdownText` 组件
新增 `MarkdownText` 组件，作为现有 `MarkdownView` 的补充。将整个 Markdown 文档通过
单个 `RichText` 渲染，段落和标题合并为一个 `AnnotatedString`。
- **支持 `maxLines` 和 `overflow`**，适用于消息预览、列表摘要等需要行数限制的场景
- 支持跨段落文本选择
- 非文本块（代码块、引用、列表等）以 InlineContent 嵌入，复用现有 `IBlockRenderer` 实现

```kotlin
MarkdownText(
    text = markdownString,
    maxLines = 3,
    overflow = TextOverflow.Ellipsis
)
```

#### `IBlockRenderer.shouldSkipRender()`
`IBlockRenderer` 接口新增 `shouldSkipRender(node)` 方法（默认 `false`）。
返回 `true` 时该块及其前后间距将被完全跳过，方便按条件过滤特定块。

### Bug 修复

#### 表格中内联解析器跨单元格匹配问题
替换 `TablesExtension` 为 `SafeTablesExtension`。原实现将整行传入 inline parser，
导致 LaTeX `$` 等自定义分隔符可能跨越单元格边界匹配。新实现会先拆分单元格，
再对每个单元格独立执行 inline 解析。

#### `AutoLineHeightText` 重组稳定性
- 修复 `AnnotatedString` 相等性判断：排除每次重组都会变化的 `LinkAnnotation` 回调，该问题会导致行高调整计算被跳过
- 修复段落尾部换行符移除逻辑，改用二分查找精确判断是否会截断 `ParagraphStyle` 注解
