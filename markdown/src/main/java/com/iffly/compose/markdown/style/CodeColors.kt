package com.iffly.compose.markdown.style

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Color configuration for syntax highlighting in code blocks.
 *
 * Default values are calibrated for dark-themed code block backgrounds. Adjust any
 * color to match your app's light or dark theme.
 *
 * @param keyword Color for language keywords (`fun`, `class`, `if`, …).
 * @param string Color for string and character literals.
 * @param comment Color for single-line and block comments.
 * @param number Color for numeric literals (decimal, hex, float, …).
 * @param annotation Color for annotations and decorators (`@Override`, `@property`, …).
 * @param type Color for type names — identifiers that start with an uppercase letter.
 * @see com.iffly.compose.markdown.core.renders.BasicSyntaxHighlighter
 */
@Immutable
data class CodeColors(
    val keyword: Color = Color(0xFFCC7832),
    val string: Color = Color(0xFF6A8759),
    val comment: Color = Color(0xFF9E9E9E),
    val number: Color = Color(0xFF6897BB),
    val annotation: Color = Color(0xFFBBB529),
    val type: Color = Color(0xFFFFC66D),
)
