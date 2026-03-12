package com.iffly.compose.markdown.html.handlers

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import com.iffly.compose.markdown.MarkdownLinkInteractionListener
import com.iffly.compose.markdown.html.HtmlInlineTagContext
import com.iffly.compose.markdown.html.HtmlInlineTagHandler

class BoldTagHandler : HtmlInlineTagHandler {
    override val tagNames = setOf("b", "strong")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pushStyle(context.markdownTheme.strongEmphasis)
    }
}

class ItalicTagHandler : HtmlInlineTagHandler {
    override val tagNames = setOf("i", "em")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pushStyle(context.markdownTheme.emphasis)
    }
}

class UnderlineTagHandler : HtmlInlineTagHandler {
    override val tagNames = setOf("u", "ins")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
    }
}

class StrikethroughTagHandler : HtmlInlineTagHandler {
    override val tagNames = setOf("del", "s", "strike")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pushStyle(context.markdownTheme.strikethrough)
    }
}

class LinkTagHandler : HtmlInlineTagHandler {
    companion object {
        private val HREF_REGEX = Regex("""href\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE)
    }

    override val tagNames = setOf("a")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        val href = HREF_REGEX.find(rawTag)?.groupValues?.get(1) ?: ""
        val linkInteractionListener =
            context.actionHandler?.let {
                MarkdownLinkInteractionListener(actionHandler = it, node = context.node)
            }
        builder.pushLink(
            LinkAnnotation.Url(
                url = href,
                styles = context.markdownTheme.link,
                linkInteractionListener = linkInteractionListener,
            ),
        )
    }
}

class SpanTagHandler : HtmlInlineTagHandler {
    override val tagNames = setOf("span")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pushStyle(CssStyleParser.parseInlineCssStyle(rawTag) ?: SpanStyle())
    }
}

fun defaultHtmlInlineTagHandlers(): List<HtmlInlineTagHandler> =
    listOf(
        BoldTagHandler(),
        ItalicTagHandler(),
        UnderlineTagHandler(),
        StrikethroughTagHandler(),
        LinkTagHandler(),
        SpanTagHandler(),
    )
