package com.iffly.compose.markdown.plugin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.config.AbstractMarkdownRenderPlugin
import com.iffly.compose.markdown.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.render.MarkdownInlineTextContent
import com.iffly.compose.markdown.render.MarkdownText
import com.iffly.compose.markdown.render.RenderRegistry
import com.iffly.compose.markdown.style.TypographyStyle
import com.vladsch.flexmark.parser.InlineParser
import com.vladsch.flexmark.parser.InlineParserExtension
import com.vladsch.flexmark.parser.InlineParserExtensionFactory
import com.vladsch.flexmark.parser.LightInlineParser
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.block.AbstractBlockParser
import com.vladsch.flexmark.parser.block.AbstractBlockParserFactory
import com.vladsch.flexmark.parser.block.BlockContinue
import com.vladsch.flexmark.parser.block.BlockParserFactory
import com.vladsch.flexmark.parser.block.BlockStart
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory
import com.vladsch.flexmark.parser.block.MatchedBlockParser
import com.vladsch.flexmark.parser.block.ParserState
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.data.MutableDataHolder
import com.vladsch.flexmark.util.sequence.BasedSequence

// --- Alert Block using CustomBlock ---
class AlertBlock : Block() {
    var alertType: String = TYPE_INFO
    var title: String? = null
    // Remove the original content string, now use child nodes to store content

    override fun getSegments(): Array<BasedSequence> = emptyArray()

    companion object {
        const val TYPE_INFO = "info"
        const val TYPE_WARNING = "warning"
        const val TYPE_SUCCESS = "success"
        const val TYPE_ERROR = "error"
    }
}

// --- Inline custom nodes extend CustomNode ---
class MentionNode(private val seq: BasedSequence) : Node() {
    var username: String = seq.subSequence(1, seq.length).toString()
    override fun getSegments(): Array<BasedSequence> = arrayOf(seq)
}

class HashtagNode(private val seq: BasedSequence) : Node() {
    var hashtag: String = seq.subSequence(1, seq.length).toString()
    override fun getSegments(): Array<BasedSequence> = arrayOf(seq)
}

class HighlightNode : Node() {
    var highlightText: String = ""
    override fun getSegments(): Array<BasedSequence> = emptyArray()
}

class BadgeNode(private val seq: BasedSequence, var badgeType: String, var badgeText: String) :
    Node() {
    override fun getSegments(): Array<BasedSequence> = arrayOf(seq)
}

// --- Block Parser ---
class AlertBlockParser(private val block: AlertBlock) : AbstractBlockParser() {
    private var finished = false
    private val contentLines = mutableListOf<String>()

    override fun getBlock(): Block = block

    override fun tryContinue(state: ParserState): BlockContinue? {
        if (finished) return BlockContinue.none()
        val line = state.line
        val nextNonSpace = state.nextNonSpaceIndex
        // Check if it's the end marker
        if (nextNonSpace + 3 <= line.length) {
            if (line.subSequence(nextNonSpace, nextNonSpace + 3).toString() == ":::") {
                finished = true
                // Consume this line to prevent other parsers from processing it
                return BlockContinue.atIndex(line.length)
            }
        }
        return BlockContinue.atIndex(state.index)
    }

    override fun addLine(state: ParserState, line: BasedSequence) {
        // Only add content when not finished
        if (!finished) {
            contentLines.add(line.toString())
        }
    }

    override fun closeBlock(state: ParserState) {
        if (contentLines.isNotEmpty()) {
            // First line as title
            val firstLine = contentLines[0].trim()

            block.title = firstLine
            // Remaining lines as content, need inline parsing
            val remainingContent = if (contentLines.size > 1) {
                contentLines.drop(1).joinToString("\n")
            } else {
                ""
            }

            if (remainingContent.isNotEmpty()) {
                // Parse content as child nodes
                parseContentAsChildren(state, remainingContent)
            }

        }
    }

    private fun parseContentAsChildren(state: ParserState, content: String) {
        // Directly use basic Parser to parse content
        val inlineParser = state.inlineParser
        inlineParser.parse(BasedSequence.of(content), block)
    }
}

class AlertBlockParserFactory : CustomBlockParserFactory, Parser.ParserExtension {
    override fun apply(options: DataHolder): BlockParserFactory =
        object : AbstractBlockParserFactory(options) {
            override fun tryStart(
                state: ParserState,
                matchedBlockParser: MatchedBlockParser
            ): BlockStart? {
                val nextNonSpace = state.nextNonSpaceIndex
                val line = state.line
                // Fix boundary check: ensure there are enough characters to check ":::"
                if (state.indent < 4 && nextNonSpace + 3 <= line.length) {
                    val marker = line.subSequence(nextNonSpace, nextNonSpace + 3).toString()
                    if (marker == ":::") {
                        val rest = line.subSequence(nextNonSpace + 3, line.length).toString().trim()
                        val alertType = when {
                            rest.startsWith("info") -> AlertBlock.TYPE_INFO
                            rest.startsWith("warning") -> AlertBlock.TYPE_WARNING
                            rest.startsWith("success") -> AlertBlock.TYPE_SUCCESS
                            rest.startsWith("error") -> AlertBlock.TYPE_ERROR
                            else -> AlertBlock.TYPE_INFO
                        }
                        val alertBlock = AlertBlock().apply { this.alertType = alertType }
                        val parser = AlertBlockParser(alertBlock)
                        // Fix index calculation: skip the entire start line
                        return BlockStart.of(parser).atIndex(line.length)
                    }
                }
                return BlockStart.none()
            }
        }

    override fun getAfterDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun getBeforeDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun affectsGlobalScope(): Boolean = false
    override fun parserOptions(options: MutableDataHolder) {}
    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customBlockParserFactory(this)
    }
}

// --- Inline Parser Extensions ---
private fun isCjk(char: Char): Boolean = char.code in 0x4e00..0x9fff

abstract class BaseInlineExt : InlineParserExtension {
    override fun finalizeDocument(inlineParser: InlineParser) {}
    override fun finalizeBlock(inlineParser: InlineParser) {}
}

class MentionInlineParserExtension : BaseInlineExt() {
    companion object {
        private val USERNAME_PATTERN = Regex("^[a-zA-Z0-9_-]{2,20}$")
    }

    override fun parse(inlineParser: LightInlineParser): Boolean {
        if (inlineParser.peek() != '@') return false
        val start = inlineParser.index
        val input = inlineParser.input
        var i = start + 1
        while (i < input.length && (input[i].isLetterOrDigit() || input[i] == '_' || input[i] == '-')) i++
        if (i == start + 1) return false
        val seq = input.subSequence(start, i)
        val username = seq.subSequence(1, seq.length).toString()
        if (!USERNAME_PATTERN.matches(username)) return false
        inlineParser.appendNode(MentionNode(seq))
        inlineParser.index = i
        return true
    }
}

class MentionInlineParserFactory : InlineParserExtensionFactory {
    override fun getCharacters(): CharSequence = "@"
    override fun apply(inlineParser: LightInlineParser): InlineParserExtension =
        MentionInlineParserExtension()

    override fun getAfterDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun getBeforeDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun affectsGlobalScope(): Boolean = false
}

class HashtagInlineParserExtension : BaseInlineExt() {
    companion object {
        private val HASHTAG_PATTERN = Regex("^[a-zA-Z0-9_\u4e00-\u9fff]{1,30}$")
    }

    override fun parse(inlineParser: LightInlineParser): Boolean {
        if (inlineParser.peek() != '#') return false
        val start = inlineParser.index
        val input = inlineParser.input
        var i = start + 1
        while (i < input.length) {
            val ch = input[i]
            if (!(ch.isLetterOrDigit() || ch == '_' || isCjk(ch))) break
            i++
        }
        if (i == start + 1) return false
        val seq = input.subSequence(start, i)
        val hashtag = seq.subSequence(1, seq.length).toString()
        if (!HASHTAG_PATTERN.matches(hashtag)) return false
        inlineParser.appendNode(HashtagNode(seq))
        inlineParser.index = i
        return true
    }
}

class HashtagInlineParserFactory : InlineParserExtensionFactory {
    override fun getCharacters(): CharSequence = "#"
    override fun apply(inlineParser: LightInlineParser): InlineParserExtension =
        HashtagInlineParserExtension()

    override fun getAfterDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun getBeforeDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun affectsGlobalScope(): Boolean = false
}

class BadgeInlineParserExtension : BaseInlineExt() {
    override fun parse(inlineParser: LightInlineParser): Boolean {
        if (inlineParser.peek() != '!') return false
        val input = inlineParser.input
        val start = inlineParser.index
        if (start + 1 >= input.length || input[start + 1] != '!') return false
        var i = start + 2
        var found = false
        while (i < input.length - 1) {
            if (input[i] == '!' && input[i + 1] == '!') {
                found = true; break
            }
            i++
        }
        if (!found) return false
        val contentStart = start + 2
        val contentEnd = i
        val seq = input.subSequence(start, i + 2)
        val raw = input.subSequence(contentStart, contentEnd).toString()
        val parts = raw.split(":", limit = 2)
        val (type, text) = if (parts.size == 2) parts[0] to parts[1] else "default" to parts[0]
        inlineParser.appendNode(BadgeNode(seq, type, text))
        inlineParser.index = i + 2
        return true
    }
}

class BadgeInlineParserFactory : InlineParserExtensionFactory {
    override fun getCharacters(): CharSequence = "!"
    override fun apply(inlineParser: LightInlineParser): InlineParserExtension =
        BadgeInlineParserExtension()

    override fun getAfterDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun getBeforeDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun affectsGlobalScope(): Boolean = false
}

// --- Highlight Inline Parser Extension ---
class HighlightInlineParserExtension : BaseInlineExt() {
    override fun parse(inlineParser: LightInlineParser): Boolean {
        if (inlineParser.peek() != '=') return false
        val start = inlineParser.index
        val input = inlineParser.input

        // Check if we have at least "==x=="
        if (start + 3 >= input.length) return false
        if (input[start + 1] != '=') return false

        // Find the closing ==
        var i = start + 2
        var found = false
        while (i < input.length - 1) {
            if (input[i] == '=' && input[i + 1] == '=') {
                found = true
                break
            }
            i++
        }

        if (!found || i == start + 2) return false // Empty content not allowed

        val contentStart = start + 2
        val contentEnd = i
        val text = input.subSequence(contentStart, contentEnd).toString()

        val highlightNode = HighlightNode()
        highlightNode.highlightText = text

        inlineParser.appendNode(highlightNode)
        inlineParser.index = i + 2
        return true
    }
}

class HighlightInlineParserFactory : InlineParserExtensionFactory {
    override fun getCharacters(): CharSequence = "="
    override fun apply(inlineParser: LightInlineParser): InlineParserExtension =
        HighlightInlineParserExtension()

    override fun getAfterDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun getBeforeDependents(): MutableSet<Class<*>> = mutableSetOf()
    override fun affectsGlobalScope(): Boolean = false
}

// --- Renderers & Builders ----------------
class AlertBlockRenderer : IBlockRenderer<AlertBlock> {
    @Composable
    override fun Invoke(node: AlertBlock, modifier: Modifier) {
        val (icon, containerColor, contentColor) = when (node.alertType) {
            AlertBlock.TYPE_INFO -> Triple(Icons.Default.Info, Color(0xFFE3F2FD), Color(0xFF1976D2))
            AlertBlock.TYPE_WARNING -> Triple(
                Icons.Default.Warning,
                Color(0xFFFFF8E1),
                Color(0xFFF57C00)
            )

            AlertBlock.TYPE_SUCCESS -> Triple(
                Icons.Default.CheckCircle,
                Color(0xFFE8F5E8),
                Color(0xFF2E7D32)
            )

            AlertBlock.TYPE_ERROR -> Triple(
                Icons.Default.Delete,
                Color(0xFFFFEBEE),
                Color(0xFFD32F2F)
            )

            else -> Triple(Icons.Default.Info, Color(0xFFE3F2FD), Color(0xFF1976D2))
        }
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = node.alertType,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val nodeTitle = node.title
                    if (!nodeTitle.isNullOrBlank()) {
                        Text(
                            text = nodeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    MarkdownText(node)
                }
            }
        }
    }
}

class MentionNodeStringBuilder : IInlineNodeStringBuilder<MentionNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: MentionNode,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
    ) {
        withStyle(
            SpanStyle(
                color = Color(0xFF1976D2),
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium
            )
        ) {
            append("@${node.username}")
        }
    }
}

class HashtagNodeStringBuilder : IInlineNodeStringBuilder<HashtagNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HashtagNode,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
    ) {
        withStyle(SpanStyle(color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)) {
            append("#${node.hashtag}")
        }
    }
}

class HighlightNodeStringBuilder : IInlineNodeStringBuilder<HighlightNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HighlightNode,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
    ) {
        withStyle(
            SpanStyle(
                background = Color(0xFFFFEB3B),
                color = Color(0xFF212121),
                fontWeight = FontWeight.Medium
            )
        ) {
            append(node.highlightText)
        }
    }
}

class BadgeNodeStringBuilder : IInlineNodeStringBuilder<BadgeNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: BadgeNode,
        inlineContentMap: MutableMap<String, MarkdownInlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
    ) {
        val (bg, fg) = when (node.badgeType.lowercase()) {
            "primary" -> Color(0xFF1976D2) to Color.White
            "success" -> Color(0xFF2E7D32) to Color.White
            "warning" -> Color(0xFFF57C00) to Color.White
            "error", "danger" -> Color(0xFFD32F2F) to Color.White
            "info" -> Color(0xFF0288D1) to Color.White
            else -> Color(0xFF616161) to Color.White
        }
        withStyle(
            SpanStyle(
                background = bg,
                color = fg,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Default
            )
        ) {
            append(" " + node.badgeText + " ")
        }
    }
}

// --- Plugin ----------------
class CustomMarkdownPlugin : AbstractMarkdownRenderPlugin() {
    override fun blockParserFactories(): List<CustomBlockParserFactory> =
        listOf(AlertBlockParserFactory())

    override fun inlineContentParserFactories(): List<InlineParserExtensionFactory> = listOf(
        MentionInlineParserFactory(),
        HashtagInlineParserFactory(),
        BadgeInlineParserFactory(),
        HighlightInlineParserFactory()
    )

    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<*>> =
        mapOf(AlertBlock::class.java to AlertBlockRenderer())

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(
            AlertBlock::class.java to CompositeChildNodeStringBuilder<AlertBlock>(),
            MentionNode::class.java to MentionNodeStringBuilder(),
            HashtagNode::class.java to HashtagNodeStringBuilder(),
            HighlightNode::class.java to HighlightNodeStringBuilder(),
            BadgeNode::class.java to BadgeNodeStringBuilder()
        )
}
