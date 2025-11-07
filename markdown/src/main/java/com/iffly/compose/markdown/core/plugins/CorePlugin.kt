package com.iffly.compose.markdown.core.plugins

import com.iffly.compose.markdown.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.core.renders.BlockQuoteRenderer
import com.iffly.compose.markdown.core.renders.BreakLineRenderer
import com.iffly.compose.markdown.core.renders.BulletListItemNodeStringBuilder
import com.iffly.compose.markdown.core.renders.BulletListNodeStringBuilder
import com.iffly.compose.markdown.core.renders.BulletListRenderer
import com.iffly.compose.markdown.core.renders.CodeNodeStringBuilder
import com.iffly.compose.markdown.core.renders.EmphasisNodeStringBuilder
import com.iffly.compose.markdown.core.renders.FencedCodeBlockRenderer
import com.iffly.compose.markdown.core.renders.HardLineBreakNodeStringBuilder
import com.iffly.compose.markdown.core.renders.HeadingNodeStringBuilder
import com.iffly.compose.markdown.core.renders.HeadingRenderer
import com.iffly.compose.markdown.core.renders.ImageNodeStringBuilder
import com.iffly.compose.markdown.core.renders.IndentedCodeBlockRenderer
import com.iffly.compose.markdown.core.renders.LinkNodeStringBuilder
import com.iffly.compose.markdown.core.renders.MarkdownDocumentRenderer
import com.iffly.compose.markdown.core.renders.OrderedListItemNodeStringBuilder
import com.iffly.compose.markdown.core.renders.OrderedListNodeStringBuilder
import com.iffly.compose.markdown.core.renders.OrderedListRenderer
import com.iffly.compose.markdown.core.renders.ParagraphNodeStringBuilder
import com.iffly.compose.markdown.core.renders.ParagraphRenderer
import com.iffly.compose.markdown.core.renders.SoftLineBreakNodeStringBuilder
import com.iffly.compose.markdown.core.renders.StrikethroughNodeStringBuilder
import com.iffly.compose.markdown.core.renders.StrongEmphasisNodeStringBuilder
import com.iffly.compose.markdown.core.renders.SubscriptNodeStringBuilder
import com.iffly.compose.markdown.core.renders.TableCellNodeStringBuilder
import com.iffly.compose.markdown.core.renders.TableRenderer
import com.iffly.compose.markdown.core.renders.TextNodeStringBuilder
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.vladsch.flexmark.ast.BlockQuote
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.BulletListItem
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.HardLineBreak
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.IndentedCodeBlock
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.OrderedListItem
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.SoftLineBreak
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.ast.ThematicBreak
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript
import com.vladsch.flexmark.ext.tables.TableBlock
import com.vladsch.flexmark.ext.tables.TableCell
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.misc.Extension

class CorePlugin : IMarkdownRenderPlugin {
    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<*>> {
        return mutableMapOf(
            Document::class.java to MarkdownDocumentRenderer(),
            TableBlock::class.java to TableRenderer(),
            Paragraph::class.java to ParagraphRenderer(),
            Heading::class.java to HeadingRenderer(),
            OrderedList::class.java to OrderedListRenderer(),
            BulletList::class.java to BulletListRenderer(),
            FencedCodeBlock::class.java to FencedCodeBlockRenderer(),
            IndentedCodeBlock::class.java to IndentedCodeBlockRenderer(),
            ThematicBreak::class.java to BreakLineRenderer(),
            BlockQuote::class.java to BlockQuoteRenderer(),
        )
    }

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<*>> {
        return mutableMapOf(
            Text::class.java to TextNodeStringBuilder(),
            Paragraph::class.java to ParagraphNodeStringBuilder(),
            Image::class.java to ImageNodeStringBuilder(),
            Code::class.java to CodeNodeStringBuilder(),
            BulletList::class.java to BulletListNodeStringBuilder(),
            BulletListItem::class.java to BulletListItemNodeStringBuilder(),
            OrderedList::class.java to OrderedListNodeStringBuilder(),
            OrderedListItem::class.java to OrderedListItemNodeStringBuilder(),
            StrongEmphasis::class.java to StrongEmphasisNodeStringBuilder(),
            Emphasis::class.java to EmphasisNodeStringBuilder(),
            Subscript::class.java to SubscriptNodeStringBuilder(),
            Strikethrough::class.java to StrikethroughNodeStringBuilder(),
            SoftLineBreak::class.java to SoftLineBreakNodeStringBuilder(),
            HardLineBreak::class.java to HardLineBreakNodeStringBuilder(),
            Link::class.java to LinkNodeStringBuilder(),
            Heading::class.java to HeadingNodeStringBuilder(),
            TableCell::class.java to TableCellNodeStringBuilder(),
        )
    }

    override fun extensions(): List<Extension> {
        return listOf(
            TablesExtension.create(),
            StrikethroughSubscriptExtension.create(),
        )
    }
}