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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.render.IBlockRenderer
import com.iffly.compose.markdown.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.style.TypographyStyle
import org.commonmark.node.Block
import org.commonmark.node.CustomBlock
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.node.Visitor
import org.commonmark.parser.SourceLine
import org.commonmark.parser.beta.InlineContentParser
import org.commonmark.parser.beta.InlineContentParserFactory
import org.commonmark.parser.beta.InlineParserState
import org.commonmark.parser.beta.ParsedInline
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.AbstractBlockParserFactory
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockParserFactory
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun

/**
 * 自定义告警块
 * 语法: :::type 内容 :::
 * 支持类型: info, warning, success, error
 */
class AlertBlock : CustomBlock() {
    var alertType: String = TYPE_INFO
    var title: String? = null
    var content: String = ""

    companion object {
        const val TYPE_INFO = "info"
        const val TYPE_WARNING = "warning"
        const val TYPE_SUCCESS = "success"
        const val TYPE_ERROR = "error"
    }
}

/**
 * 自定义提及节点
 * 语法: @username
 */
class MentionNode : Node() {
    var username: String = ""
    override fun accept(visitor: Visitor?) {
        TODO("Not yet implemented")
    }
}

/**
 * 自定义标签节点
 * 语法: #hashtag
 */
class HashtagNode : Node() {
    var hashtag: String = ""
    override fun accept(visitor: Visitor?) {
        TODO("Not yet implemented")
    }
}

/**
 * 自定义高亮文本节点
 * 语法: ==高亮文本==
 */
class HighlightNode : Node() {
    var highlightText: String = ""
    override fun accept(visitor: Visitor?) {
        TODO("Not yet implemented")
    }
}

/**
 * 自定义徽章节点
 * 语法: !!badge:text!!
 */
class BadgeNode : Node() {
    var badgeText: String = ""
    var badgeType: String = "default"
    override fun accept(visitor: Visitor?) {
        TODO("Not yet implemented")
    }
}

/**
 * 告警块解析器
 */
class AlertBlockParser : AbstractBlockParser() {
    private val block = AlertBlock()
    private var finished = false

    override fun getBlock(): Block = block
    override fun isContainer(): Boolean = false
    override fun canContain(childBlock: Block): Boolean = false

    override fun tryContinue(parserState: ParserState): BlockContinue? {
        if (finished) {
            return BlockContinue.finished()
        }

        val nextNonSpace = parserState.nextNonSpaceIndex
        val line = parserState.line.content

        // 检查是否遇到结束标记 ":::"
        if (nextNonSpace + 2 < line.length) {
            val marker = line.subSequence(nextNonSpace, nextNonSpace + 3)
            if (marker == ":::") {
                finished = true
                return BlockContinue.finished()
            }
        }

        return BlockContinue.atIndex(parserState.index)
    }

    override fun addLine(line: SourceLine) {
        val content = line.content.toString()
        if (block.content.isEmpty()) {
            block.content = content
        } else {
            block.content += "\n" + content
        }
    }

    override fun closeBlock() {
        // 解析标题（如果有的话）
        val content = block.content
        val lines = content.split('\n')
        if (lines.isNotEmpty()) {
            val firstLine = lines[0].trim()
            if (firstLine.isNotEmpty()) {
                block.title = firstLine
                if (lines.size > 1) {
                    block.content = lines.drop(1).joinToString("\n")
                } else {
                    block.content = ""
                }
            }
        }
        super.closeBlock()
    }
}

/**
 * 告警块解析器工厂
 */
class AlertBlockParserFactory : AbstractBlockParserFactory() {
    override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart? {
        val nextNonSpace = state.nextNonSpaceIndex
        val line = state.line.content

        // 检查是否以 ":::" 开头的告警块
        if (state.indent < 4 && nextNonSpace + 2 < line.length) {
            val marker = line.subSequence(nextNonSpace, nextNonSpace + 3)
            if (marker == ":::") {
                val restOfLine = line.subSequence(nextNonSpace + 3, line.length).toString().trim()
                val alertType = when {
                    restOfLine.startsWith("info") -> AlertBlock.TYPE_INFO
                    restOfLine.startsWith("warning") -> AlertBlock.TYPE_WARNING
                    restOfLine.startsWith("success") -> AlertBlock.TYPE_SUCCESS
                    restOfLine.startsWith("error") -> AlertBlock.TYPE_ERROR
                    else -> AlertBlock.TYPE_INFO
                }

                val parser = AlertBlockParser()
                (parser.getBlock() as AlertBlock).alertType = alertType

                return BlockStart.of(parser).atIndex(nextNonSpace + 3 + alertType.length)
            }
        }
        return BlockStart.none()
    }
}

/**
 * 提及解析器
 */
class MentionInlineParser : InlineContentParser {
    companion object {
        private val USERNAME_PATTERN = Regex("^[a-zA-Z0-9_-]{2,20}$")
    }

    override fun tryParse(inlineParserState: InlineParserState): ParsedInline? {
        val scanner = inlineParserState.scanner()
        scanner.next() // 跳过 '@'

        val textStart = scanner.position()

        // 扫描用户名字符
        while (scanner.hasNext()) {
            val char = scanner.peek()
            if (!char.isLetterOrDigit() && char != '_' && char != '-') {
                break
            }
            scanner.next()
        }

        val textSource = scanner.getSource(textStart, scanner.position())
        val username = textSource.content

        if (USERNAME_PATTERN.matches(username)) {
            val mentionNode = MentionNode().apply {
                this.username = username
                sourceSpans = textSource.sourceSpans
            }
            return ParsedInline.of(mentionNode, scanner.position())
        }

        return ParsedInline.none()
    }

    class Factory : InlineContentParserFactory {
        override fun getTriggerCharacters(): Set<Char> = setOf('@')
        override fun create(): InlineContentParser = MentionInlineParser()
    }
}

/**
 * 标签解析器
 */
class HashtagInlineParser : InlineContentParser {
    companion object {
        private val HASHTAG_PATTERN = Regex("^[a-zA-Z0-9_\\u4e00-\\u9fff]{1,30}$")
    }

    override fun tryParse(inlineParserState: InlineParserState): ParsedInline? {
        val scanner = inlineParserState.scanner()
        scanner.next() // 跳过 '#'

        val textStart = scanner.position()

        // 扫描标签字符（支持中文）
        while (scanner.hasNext()) {
            val char = scanner.peek()
            if (!char.isLetterOrDigit() && char != '_' && !isCJK(char)) {
                break
            }
            scanner.next()
        }

        val textSource = scanner.getSource(textStart, scanner.position())
        val hashtag = textSource.content

        if (HASHTAG_PATTERN.matches(hashtag)) {
            val hashtagNode = HashtagNode().apply {
                this.hashtag = hashtag
                setSourceSpans(textSource.sourceSpans)
            }
            return ParsedInline.of(hashtagNode, scanner.position())
        }

        return ParsedInline.none()
    }

    private fun isCJK(char: Char): Boolean {
        return char.code in 0x4e00..0x9fff
    }

    class Factory : InlineContentParserFactory {
        override fun getTriggerCharacters(): Set<Char> = setOf('#')
        override fun create(): InlineContentParser = HashtagInlineParser()
    }
}

/**
 * 高亮文本分隔符处理器
 */
class HighlightDelimiterProcessor : DelimiterProcessor {
    override fun getOpeningCharacter(): Char = '='
    override fun getClosingCharacter(): Char = '='
    override fun getMinLength(): Int = 2
    override fun process(openingRun: DelimiterRun, closingRun: DelimiterRun): Int {
        if (openingRun.length() >= 2 && closingRun.length() >= 2) {
            val highlightNode = HighlightNode()

            // 提取高亮文本内容
            var node = openingRun.opener?.next
            val content = StringBuilder()
            while (node != null && node != closingRun.closer) {
                if (node is Text) {
                    content.append(node.literal)
                }
                node = node.next
            }

            highlightNode.highlightText = content.toString()

            // 替换原有节点
            val textNode = Text(content.toString())
            highlightNode.appendChild(textNode)

            openingRun.opener?.insertAfter(highlightNode)

            // 移除处理过的节点
            node = openingRun.opener?.next
            while (node != null && node != closingRun.closer?.next) {
                val next = node.next
                if (node != highlightNode) {
                    node.unlink()
                }
                node = next
            }

            return 2
        }
        return 0
    }
}

/**
 * 徽章解析器
 */
class BadgeInlineParser : InlineContentParser {
    override fun tryParse(inlineParserState: InlineParserState): ParsedInline? {
        val scanner = inlineParserState.scanner()

        // 检查开始标记 "!!"
        if (scanner.peek() != '!') {
            return ParsedInline.none()
        }

        scanner.next() // 跳过第一个 '!'

        if (!scanner.hasNext() || scanner.peek() != '!') {
            return ParsedInline.none()
        }

        scanner.next() // 跳过第二个 '!'

        val textStart = scanner.position()
        val content = StringBuilder()

        // 查找结束标记 "!!"
        while (scanner.hasNext()) {
            val char = scanner.peek()
            if (char == '!' && scanner.hasNext()) {
                scanner.next() // 移动到下一个字符
                if (scanner.hasNext() && scanner.peek() == '!') {
                    // 找到结束标记
                    val textSource = scanner.getSource(textStart, scanner.position())
                    scanner.next() // 跳过第二个 '!'

                    // 解析徽章内容 (badge:text 或者 type:text)
                    val contentStr = content.toString()
                    val parts = contentStr.split(":", limit = 2)
                    val badgeType = if (parts.size > 1) parts[0] else "default"
                    val badgeText = if (parts.size > 1) parts[1] else parts[0]

                    val badgeNode = BadgeNode().apply {
                        this.badgeText = badgeText
                        this.badgeType = badgeType
                        sourceSpans = textSource.sourceSpans
                    }

                    return ParsedInline.of(badgeNode, scanner.position())
                } else {
                    content.append('!')
                }
            } else {
                content.append(char)
                scanner.next()
            }
        }

        return ParsedInline.none()
    }

    class Factory : InlineContentParserFactory {
        override fun getTriggerCharacters(): Set<Char> = setOf('!')
        override fun create(): InlineContentParser = BadgeInlineParser()
    }
}

/**
 * 告警块渲染器
 */
class AlertBlockRenderer : IBlockRenderer<AlertBlock> {
    @Composable
    override fun Invoke(node: AlertBlock, modifier: Modifier) {
        val (icon, containerColor, contentColor) = when (node.alertType) {
            AlertBlock.TYPE_INFO -> Triple(
                Icons.Default.Info,
                Color(0xFFE3F2FD),
                Color(0xFF1976D2)
            )

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

            else -> Triple(
                Icons.Default.Info,
                Color(0xFFE3F2FD),
                Color(0xFF1976D2)
            )
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

                    if (node.content.isNotBlank()) {
                        Text(
                            text = node.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 提及节点字符串构建器
 */
class MentionNodeStringBuilder : IInlineNodeStringBuilder<MentionNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: MentionNode,
        inlineContentMap: MutableMap<String, androidx.compose.foundation.text.InlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int
    ) {
        pushStyle(
            SpanStyle(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.None,
                background = Color(0xFFE3F2FD)
            )
        )
        append("@${node.username}")
        pop()
    }
}

/**
 * 标签节点字符串构建器
 */
class HashtagNodeStringBuilder : IInlineNodeStringBuilder<HashtagNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HashtagNode,
        inlineContentMap: MutableMap<String, androidx.compose.foundation.text.InlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int
    ) {
        pushStyle(
            SpanStyle(
                color = Color(0xFF7B1FA2),
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.None,
                background = Color(0xFFF3E5F5)
            )
        )
        append("#${node.hashtag}")
        pop()
    }
}

/**
 * 高亮节点字符串构建器
 */
class HighlightNodeStringBuilder : IInlineNodeStringBuilder<HighlightNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HighlightNode,
        inlineContentMap: MutableMap<String, androidx.compose.foundation.text.InlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int
    ) {
        pushStyle(
            SpanStyle(
                background = Color(0xFFFFEB3B),
                color = Color(0xFF212121),
                fontWeight = FontWeight.Medium
            )
        )
        append(node.highlightText)
        pop()
    }
}

/**
 * 徽章节点字符串构建器
 */
class BadgeNodeStringBuilder : IInlineNodeStringBuilder<BadgeNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: BadgeNode,
        inlineContentMap: MutableMap<String, androidx.compose.foundation.text.InlineTextContent>,
        typographyStyle: TypographyStyle,
        linkInteractionListener: LinkInteractionListener?,
        indentLevel: Int
    ) {
        val (bgColor, textColor) = when (node.badgeType.lowercase()) {
            "primary" -> Color(0xFF1976D2) to Color.White
            "success" -> Color(0xFF2E7D32) to Color.White
            "warning" -> Color(0xFFF57C00) to Color.White
            "error", "danger" -> Color(0xFFD32F2F) to Color.White
            "info" -> Color(0xFF0288D1) to Color.White
            else -> Color(0xFF616161) to Color.White
        }

        pushStyle(
            SpanStyle(
                background = bgColor,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Default
            )
        )
        append(" ${node.badgeText} ")
        pop()
    }
}

/**
 * 自定义Markdown插件
 */
class CustomMarkdownPlugin : IMarkdownRenderPlugin {

    override fun blockParserFactories(): List<BlockParserFactory> {
        return listOf(AlertBlockParserFactory())
    }

    override fun inlineContentParserFactories(): List<InlineContentParserFactory> {
        return listOf(
            MentionInlineParser.Factory(),
            HashtagInlineParser.Factory(),
            BadgeInlineParser.Factory()
        )
    }

    override fun blockRenderers(): Map<Class<out Block>, IBlockRenderer<out Block>> {
        return mapOf(
            AlertBlock::class.java to AlertBlockRenderer()
        )
    }

    override fun inlineNodeStringBuilders(): Map<Class<out Node>, IInlineNodeStringBuilder<out Node>> {
        return mapOf(
            MentionNode::class.java to MentionNodeStringBuilder(),
            HashtagNode::class.java to HashtagNodeStringBuilder(),
            HighlightNode::class.java to HighlightNodeStringBuilder(),
            BadgeNode::class.java to BadgeNodeStringBuilder()
        )
    }
}
