package com.iffly.compose.markdown.render

import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.BulletListItem
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.HardLineBreak
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.OrderedListItem
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.SoftLineBreak
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript
import com.vladsch.flexmark.util.ast.Node


class InlineNodeStringBuilders private constructor(
    private val annotatedStringBuilders: Map<Class<out Node>, IInlineNodeStringBuilder<out Node>>
) {

    companion object {
        private val originalAnnotatedStringBuilders: Map<Class<out Node>, IInlineNodeStringBuilder<out Node>> =
            mapOf(
                // Add default inline node string builders here if any
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

    class Builder {
        private val annotatedStringBuilders: MutableMap<Class<out Node>, IInlineNodeStringBuilder<out Node>> =
            originalAnnotatedStringBuilders.toMutableMap()

        fun addAnnotatedStringBuilder(
            nodeClass: Class<out Node>,
            builder: IInlineNodeStringBuilder<out Node>
        ): Builder {
            annotatedStringBuilders[nodeClass] = builder
            return this
        }

        fun build(): InlineNodeStringBuilders {
            val inlineNodeStringBuilders =
                InlineNodeStringBuilders(annotatedStringBuilders.toMap())
            return inlineNodeStringBuilders
        }
    }

    operator fun get(nodeClass: Class<out Node>): IInlineNodeStringBuilder<Node>? {
        return annotatedStringBuilders[nodeClass] as? IInlineNodeStringBuilder<Node>
    }
}

val defaultInlineNodeStringBuilders by lazy {
    InlineNodeStringBuilders.Builder().build()
}
