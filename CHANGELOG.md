# Changelog

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
