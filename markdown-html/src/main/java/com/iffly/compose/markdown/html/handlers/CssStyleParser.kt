package com.iffly.compose.markdown.html.handlers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

internal object CssStyleParser {
    private val STYLE_REGEX = Regex("""style\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE)

    private val NAMED_COLORS =
        mapOf(
            "red" to Color.Red,
            "blue" to Color.Blue,
            "green" to Color.Green,
            "black" to Color.Black,
            "white" to Color.White,
            "gray" to Color.Gray,
            "grey" to Color.Gray,
            "yellow" to Color.Yellow,
            "cyan" to Color.Cyan,
            "magenta" to Color.Magenta,
            "transparent" to Color.Transparent,
            "orange" to Color(0xFFFFA500),
            "purple" to Color(0xFF800080),
            "pink" to Color(0xFFFFC0CB),
            "brown" to Color(0xFF8B4513),
            "darkred" to Color(0xFF8B0000),
            "darkblue" to Color(0xFF00008B),
            "darkgreen" to Color(0xFF006400),
            "lightgray" to Color.LightGray,
            "lightgrey" to Color.LightGray,
            "darkgray" to Color.DarkGray,
            "darkgrey" to Color.DarkGray,
        )

    fun parseInlineCssStyle(tag: String): SpanStyle? {
        val styleAttr = STYLE_REGEX.find(tag)?.groupValues?.get(1) ?: return null
        val props = parseCssProperties(styleAttr)
        if (props.isEmpty()) return null

        var color: Color? = null
        var backgroundColor: Color? = null
        var fontWeight: FontWeight? = null
        var fontStyle: FontStyle? = null
        var textDecoration: TextDecoration? = null
        var fontSize: Float? = null

        for ((key, value) in props) {
            when (key) {
                "color" -> color = parseCssColor(value)
                "background-color", "background" -> backgroundColor = parseCssColor(value)
                "font-weight" -> fontWeight = parseFontWeight(value)
                "font-style" -> fontStyle = parseFontStyle(value)
                "text-decoration" -> textDecoration = parseTextDecoration(value)
                "font-size" -> fontSize = parseFontSize(value)
            }
        }

        return SpanStyle(
            color = color ?: Color.Unspecified,
            background = backgroundColor ?: Color.Unspecified,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textDecoration = textDecoration,
            fontSize = fontSize?.sp ?: androidx.compose.ui.unit.TextUnit.Unspecified,
        )
    }

    private fun parseCssProperties(css: String): Map<String, String> =
        css
            .split(";")
            .mapNotNull { prop ->
                val parts = prop.split(":", limit = 2)
                if (parts.size == 2) {
                    parts[0].trim().lowercase() to parts[1].trim()
                } else {
                    null
                }
            }.toMap()

    private fun parseCssColor(value: String): Color? {
        val v = value.trim().lowercase()
        if (v.startsWith("#")) {
            return try {
                val hex = v.removePrefix("#")
                when (hex.length) {
                    3 -> {
                        val r = hex[0].toString().repeat(2).toInt(16)
                        val g = hex[1].toString().repeat(2).toInt(16)
                        val b = hex[2].toString().repeat(2).toInt(16)
                        Color(r, g, b)
                    }

                    6 -> {
                        Color(("FF$hex").toLong(16))
                    }

                    8 -> {
                        Color(hex.toLong(16))
                    }

                    else -> {
                        null
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
        if (v.startsWith("rgb(") && v.endsWith(")")) {
            return try {
                val parts = v.removePrefix("rgb(").removeSuffix(")").split(",")
                if (parts.size == 3) {
                    Color(parts[0].trim().toInt(), parts[1].trim().toInt(), parts[2].trim().toInt())
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
        return NAMED_COLORS[v]
    }

    private fun parseFontWeight(value: String): FontWeight? =
        when (value.trim().lowercase()) {
            "bold" -> FontWeight.Bold
            "normal" -> FontWeight.Normal
            "lighter" -> FontWeight.Light
            "bolder" -> FontWeight.ExtraBold
            else -> value.trim().toIntOrNull()?.let { FontWeight(it) }
        }

    private fun parseFontStyle(value: String): FontStyle? =
        when (value.trim().lowercase()) {
            "italic" -> FontStyle.Italic
            "normal" -> FontStyle.Normal
            else -> null
        }

    private fun parseTextDecoration(value: String): TextDecoration? =
        when (value.trim().lowercase()) {
            "underline" -> TextDecoration.Underline
            "line-through" -> TextDecoration.LineThrough
            "none" -> TextDecoration.None
            else -> null
        }

    private fun parseFontSize(value: String): Float? {
        val v = value.trim().lowercase()
        return when {
            v.endsWith("px") -> v.removeSuffix("px").toFloatOrNull()
            v.endsWith("sp") -> v.removeSuffix("sp").toFloatOrNull()
            v.endsWith("em") -> v.removeSuffix("em").toFloatOrNull()?.let { it * 16f }
            v.endsWith("pt") -> v.removeSuffix("pt").toFloatOrNull()?.let { it * 1.333f }
            else -> v.toFloatOrNull()
        }
    }
}
