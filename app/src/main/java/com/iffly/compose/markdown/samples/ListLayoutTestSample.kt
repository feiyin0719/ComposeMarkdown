package com.iffly.compose.markdown.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig

@Composable
fun ListLayoutTestExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
    ) {
        SelectionContainer {
            MarkdownView(
                content =
                    """
                    # List Layout Test

                    Test cases for list rendering under various conditions: long text, mixed languages, different font sizes, and deep nesting.

                    ---

                    ## 1. Long Text Wrapping

                    - This is an extremely long list item that should wrap across multiple lines to verify the layout handles text overflow correctly when the content exceeds the available width of the screen.
                    - Short item.
                    - Another very long paragraph in a list item. When the marker is drawn via drawText on the canvas, the content area must be properly constrained so that line breaks occur at the right position without overlapping the marker region.

                    1. First ordered item with a long description that spans multiple lines. The number marker should stay aligned to the top-left while the text wraps beneath it without shifting.
                    2. Short.
                    3. Third item also has a lengthy explanation to test whether the ordered list number alignment (right-aligned with figure spaces) works correctly alongside multi-line wrapping behavior.
                    10. Item number ten — this tests right-alignment of double-digit numbers. The marker area should be wide enough to accommodate "10." without clipping.
                    11. Another double-digit item to verify consistent alignment with the item above.
                    100. Triple-digit number. Layout must allocate enough horizontal space for "100." while keeping content properly offset.

                    ---

                    ## 2. Mixed Languages / 多语言混排

                    - English: The quick brown fox jumps over the lazy dog.
                    - 中文：敏捷的棕色狐狸跳过了懒惰的狗。这是一段较长的中文文本，用于测试中文字符在列表布局中的换行和对齐效果。
                    - 日本語：素早い茶色の狐が怠惰な犬を飛び越えた。リストレイアウトで日本語テキストが正しく折り返されるかテストします。
                    - 한국어: 빠른 갈색 여우가 게으른 개를 뛰어넘었습니다. 목록 레이아웃에서 한국어 텍스트의 줄 바꿈을 테스트합니다.
                    - العربية: الثعلب البني السريع يقفز فوق الكلب الكسول. هذا اختبار للنص العربي في تخطيط القائمة.
                    - Mixed 混合: This sentence mixes English, 中文, 日本語, and 한국어 together in a single list item to verify that mixed-script rendering does not break the layout.

                    1. 第一项：包含**粗体**和*斜体*的中文列表项。
                    2. Second item: Contains `inline code` with English text.
                    3. 第三项混合：This is a mixed-language item，包含中英文混排的内容，and it should wrap correctly across lines.

                    ---

                    ## 3. Inline Style Variations

                    - **Bold item**: This entire item is bold and should maintain proper line height and alignment.
                    - *Italic item with a long description that wraps to multiple lines, testing whether italic text affects the vertical alignment of the marker.*
                    - ~~Strikethrough text in a list item to verify that decoration styles do not affect layout calculations.~~
                    - `Inline code in a list item that is long enough to potentially wrap to the next line if the screen is narrow enough.`
                    - Normal text with **bold**, *italic*, ~~strikethrough~~, and `code` all mixed in a single item. This tests whether different spans cause inconsistent line heights.
                    - H~2~O and E = mc^2^ — subscript and superscript in list items.

                    ---

                    ## 4. Deep Nesting

                    - Level 1
                      - Level 2
                        - Level 3
                          - Level 4 — deeply nested item with enough text to verify that the increasing indent does not push content off-screen or cause horizontal overflow.
                            - Level 5: Even deeper nesting. The marker offset accumulates with each level, so we need to ensure the content area remains usable.

                    1. Ordered Level 1
                       1. Ordered Level 2
                          1. Ordered Level 3 — at this depth, the combined indent and number marker width should still leave enough room for content text to render properly.
                             1. Ordered Level 4

                    - Mixed nesting:
                      1. Ordered inside unordered
                         - Unordered inside ordered inside unordered
                           1. And ordered again — four levels deep with alternating list types.

                    ---

                    ## 5. Loose List with Paragraphs

                    1. First item with multiple paragraphs.

                       This is the second paragraph of the first item. It should be indented to align with the content above, not with the marker.

                       > A blockquote inside a loose list item. The quote border should render correctly within the indented content area.

                       Third paragraph with **bold** and *italic* and `code`.

                    2. Second item — also loose.

                       Another paragraph here. 这里也包含中文内容，测试宽松列表中多语言段落的布局。

                       ```kotlin
                       fun example() {
                           println("Code block inside a loose list item")
                       }
                       ```

                    3. Third item with a nested list:

                       - Nested bullet A
                       - Nested bullet B with a long description that should wrap within the nested content area without interfering with the outer list item's layout.

                    ---

                    ## 6. Single-Character and Emoji Items

                    - A
                    - B
                    - C
                    - 字
                    - あ

                    1. X
                    2. Y
                    3. Z

                    ---

                    ## 7. Links and Images in Lists

                    - [Kotlin Official Site](https://kotlinlang.org) — a link inside a list item with additional description text that follows the link.
                    - Contains a [short link](https://example.com) mid-sentence and continues with more text to verify that inline links do not break the layout flow.
                    - [A very long link text that might wrap to the next line](https://developer.android.com/develop/ui/compose) and still maintain proper alignment.

                    ---

                    ## 8. Large Number of Items

                    1. Item 1
                    2. Item 2
                    3. Item 3
                    4. Item 4
                    5. Item 5
                    6. Item 6
                    7. Item 7
                    8. Item 8
                    9. Item 9
                    10. Item 10
                    11. Item 11
                    12. Item 12
                    13. Item 13
                    14. Item 14
                    15. Item 15
                    16. Item 16
                    17. Item 17
                    18. Item 18
                    19. Item 19
                    20. Item 20

                    ---

                    ## 9. Marker–Content Line Height Mismatch

                    Test that the marker aligns to the baseline of the first line of text when the content line height differs from the marker's.

                    ### 9.1 Heading Inside List Item

                    - # Heading 1 in list
                    - ## Heading 2 in list
                    - ### Heading 3 in list
                    - Normal text after headings

                    ### 9.2 Mixed Font Sizes in First Line

                    - **Large bold text** followed by normal text, the marker should align with the baseline of the first line.
                    - *Small italic text* and then some longer content to verify alignment when italic causes different metrics.
                    - `code span at the start` which may use a different font family and size from the marker.

                    ### 9.3 Loose List with Blockquote as First Content

                    1. First item — normal paragraph.

                    2. > This item starts with a blockquote. The marker should still align to the first line of the blockquote text.

                    3. ```
                       Code block as first content
                       ```

                    4. Normal item after code block items.

                    ### 9.4 Superscript / Subscript Affecting Line Height

                    - E = mc^2^ — superscript in the first line may increase the line top.
                    - H~2~O — subscript in the first line may increase the line bottom.
                    - Normal item for comparison.

                    ### 9.5 Nested List with Different Line Heights

                    - Outer item with normal text
                      - ## Nested heading item
                        - ### Deeper nested heading
                          - Normal nested item

                    1. Outer ordered item
                       1. ## Nested ordered heading
                          1. Normal nested content

                    ### 9.6 Inline Image Affecting Line Height

                    - ![icon](https://via.placeholder.com/40x40) Image at the start of a list item may increase the line height beyond the marker's natural height.
                    - Normal item for comparison.
                    - Text before ![icon](https://via.placeholder.com/24x24) image mid-sentence and more text following.

                    """.trimIndent(),
                markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
