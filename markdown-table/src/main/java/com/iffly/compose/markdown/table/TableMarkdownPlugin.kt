package com.iffly.compose.markdown.table

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.config.AbstractMarkdownRenderPlugin
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.vladsch.flexmark.ext.tables.TableBlock
import com.vladsch.flexmark.ext.tables.TableCell
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.misc.Extension

/**
 * Theme for tables in Markdown.
 * @param borderColor The color of the table borders.
 * @param borderThickness The thickness of the table borders.
 * @param titleBackgroundColor The background color of the table title.
 * @param tableHeaderBackgroundColor The background color of the table header.
 * @param tableCellBackgroundColor The background color of the table cells.
 * @param cellTextStyle The text style for the table cells.
 * @param headerTextStyle The text style for the table headers.
 * @param copyTextStyle The text style for the copy button in table cells.
 * @param shape The shape of the table.
 * @param cellPadding The padding inside the table cells.
 */
data class TableTheme(
    val borderColor: Color = Color.Gray,
    val borderThickness: Dp = 1.dp,
    val titleBackgroundColor: Color = Color.LightGray,
    val tableHeaderBackgroundColor: Color = Color.White,
    val tableCellBackgroundColor: Color = Color.White,
    val cellTextStyle: TextStyle? = null,
    val headerTextStyle: TextStyle? = TextStyle(fontWeight = FontWeight.Bold),
    val copyTextStyle: TextStyle =
        TextStyle(
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Black,
        ),
    val shape: Shape = RoundedCornerShape(8.dp),
    val cellPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
)

/**
 * Markdown table plugin providing GFM table support.
 * @see AbstractMarkdownRenderPlugin
 */
class TableMarkdownPlugin(
    private val tableTheme: TableTheme = TableTheme(),
) : AbstractMarkdownRenderPlugin() {
    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<*>> =
        mapOf(
            TableBlock::class.java to TableRenderer(tableTheme),
        )

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(
            TableCell::class.java to TableCellNodeStringBuilder(tableTheme),
        )

    override fun extensions(): List<Extension> =
        listOf(
            TablesExtension.create(),
        )
}
