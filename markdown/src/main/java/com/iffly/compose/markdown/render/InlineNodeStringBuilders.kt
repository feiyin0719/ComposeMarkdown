package com.iffly.compose.markdown.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import org.commonmark.node.Node

class InlineNodeStringBuilders private constructor(
    private val annotatedStringBuilders: Map<Class<out Node>, IInlineNodeStringBuilder<out Node>>
) {

    class Builder {
        private val annotatedStringBuilders: MutableMap<Class<out Node>, IInlineNodeStringBuilder<out Node>> =
            mutableMapOf()

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

private val defaultInlineNodeStringBuilders =
    InlineNodeStringBuilders.Builder().build()

private val LocalInlineNodeAnnotatedStringBuildersProvider =
    staticCompositionLocalOf { defaultInlineNodeStringBuilders }

@Composable
internal fun currentInlineNodeAnnotatedStringBuilders(): InlineNodeStringBuilders =
    LocalInlineNodeAnnotatedStringBuildersProvider.current