package com.iffly.compose.markdown.core.plugins

import com.iffly.compose.markdown.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.render.BlockQuoteRenderer
import com.iffly.compose.markdown.render.BreakLineRenderer
import com.iffly.compose.markdown.render.BulletListItemNodeStringBuilder
import com.iffly.compose.markdown.render.BulletListNodeStringBuilder
import com.iffly.compose.markdown.render.BulletListRenderer
import com.iffly.compose.markdown.render.CodeNodeStringBuilder
import com.iffly.compose.markdown.render.EmphasisNodeStringBuilder
import com.iffly.compose.markdown.render.FencedCodeBlockRenderer
import com.iffly.compose.markdown.render.HardLineBreakNodeStringBuilder
import com.iffly.compose.markdown.render.HeadingRenderer
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.ImageNodeStringBuilder
import com.iffly.compose.markdown.render.IndentedCodeBlockRenderer
import com.iffly.compose.markdown.render.LinkNodeStringBuilder
import com.iffly.compose.markdown.render.OrderedListItemNodeStringBuilder
import com.iffly.compose.markdown.render.OrderedListNodeStringBuilder
import com.iffly.compose.markdown.render.OrderedListRenderer
import com.iffly.compose.markdown.render.ParagraphNodeStringBuilder
import com.iffly.compose.markdown.render.ParagraphRenderer
import com.iffly.compose.markdown.render.SoftLineBreakNodeStringBuilder
import com.iffly.compose.markdown.render.StrikethroughNodeStringBuilder
import com.iffly.compose.markdown.render.StrongEmphasisNodeStringBuilder
import com.iffly.compose.markdown.render.SubscriptNodeStringBuilder
import com.iffly.compose.markdown.render.TableRenderer
import com.iffly.compose.markdown.render.TextNodeStringBuilder
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
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.misc.Extension

class CorePlugin : IMarkdownRenderPlugin {
    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<out Block>> {
        return mutableMapOf(
            TableBlock::class.java to TableRenderer,
            Paragraph::class.java to ParagraphRenderer,
            Heading::class.java to HeadingRenderer,
            OrderedList::class.java to OrderedListRenderer,
            BulletList::class.java to BulletListRenderer,
            FencedCodeBlock::class.java to FencedCodeBlockRenderer,
            IndentedCodeBlock::class.java to IndentedCodeBlockRenderer,
            ThematicBreak::class.java to BreakLineRenderer,
            BlockQuote::class.java to BlockQuoteRenderer,
        )
    }

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<out Node>> {
        return mutableMapOf(
            Text::class.java to TextNodeStringBuilder,
            Paragraph::class.java to ParagraphNodeStringBuilder,
            Image::class.java to ImageNodeStringBuilder,
            Code::class.java to CodeNodeStringBuilder,
            BulletList::class.java to BulletListNodeStringBuilder,
            BulletListItem::class.java to BulletListItemNodeStringBuilder,
            OrderedList::class.java to OrderedListNodeStringBuilder,
            OrderedListItem::class.java to OrderedListItemNodeStringBuilder,
            StrongEmphasis::class.java to StrongEmphasisNodeStringBuilder,
            Emphasis::class.java to EmphasisNodeStringBuilder,
            Subscript::class.java to SubscriptNodeStringBuilder,
            Strikethrough::class.java to StrikethroughNodeStringBuilder,
            SoftLineBreak::class.java to SoftLineBreakNodeStringBuilder,
            HardLineBreak::class.java to HardLineBreakNodeStringBuilder,
            Link::class.java to LinkNodeStringBuilder,
        )
    }

    override fun extensions(): List<Extension> {
        return listOf(
            TablesExtension.create(),
            StrikethroughSubscriptExtension.create(),
        )
    }
}