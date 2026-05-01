package com.iffly.compose.markdown.core.renders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import com.iffly.compose.markdown.style.CodeColors
import com.vladsch.flexmark.util.ast.Block

/**
 * A [CodeAnnotator] that applies regex-based syntax highlighting for commonly used
 * programming languages.
 *
 * ### Supported languages
 * Pass the language name (case-insensitive) as the fenced-code info string.
 * The following values are recognized:
 *
 * | Family        | Accepted info strings                      |
 * |---------------|--------------------------------------------|
 * | Kotlin        | `kotlin`                                   |
 * | Java          | `java`                                     |
 * | JavaScript    | `javascript`, `js`                         |
 * | TypeScript    | `typescript`, `ts`                         |
 * | Python        | `python`                                   |
 * | Swift         | `swift`                                    |
 * | Rust          | `rust`                                     |
 * | Go            | `go`                                       |
 * | Dart          | `dart`                                     |
 * | C             | `c`                                        |
 * | C++           | `cpp`, `c++`                               |
 * | C#            | `cs`, `csharp`, `c#`                       |
 * | Ruby          | `ruby`                                     |
 * | PHP           | `php`                                      |
 * | SQL           | `sql`                                      |
 * | Bash / Shell  | `bash`, `sh`, `shell`, `zsh`               |
 * | CSS           | `css`                                      |
 * | HTML          | `html`                                     |
 * | XML           | `xml`                                      |
 * | YAML          | `yaml`, `yml`                              |
 * | TOML          | `toml`                                     |
 * | JSON          | `json`                                     |
 *
 * For any unrecognized language the code is returned as plain unstyled text.
 *
 * ### Token priority (highest → lowest)
 * 1. Comments — single-line and block
 * 2. String and character literals
 * 3. Annotations / decorators
 * 4. Numeric literals
 * 5. Keywords
 * 6. Type identifiers (uppercase-initial names, typed languages only)
 *
 * Each character position is claimed by the first matching token; later patterns
 * cannot overlap with already-styled ranges.
 *
 * @param colors Color configuration for each token category. Defaults to [CodeColors].
 * @see CodeAnnotator
 */
class BasicSyntaxHighlighter(
    val colors: CodeColors = CodeColors(),
) : CodeAnnotator {
    override fun annotate(
        code: String,
        language: String,
        node: Block,
    ): AnnotatedString =
        buildAnnotatedString {
            append(code)
            collectSpans(code, resolveLanguage(language)).forEach { (start, end, style) ->
                addStyle(style, start, end)
            }
        }

    // ── Language alias resolution ────────────────────────────────────────────

    private fun resolveLanguage(language: String): String =
        when (language.lowercase().trim()) {
            "js" -> "javascript"
            "ts" -> "typescript"
            "sh", "shell", "zsh" -> "bash"
            "c++" -> "cpp"
            "c#", "csharp" -> "cs"
            "yml" -> "yaml"
            else -> language.lowercase().trim()
        }

    // ── Span collection ──────────────────────────────────────────────────────

    private fun collectSpans(
        code: String,
        lang: String,
    ): List<Triple<Int, Int, SpanStyle>> {
        val covered = BooleanArray(code.length)
        val result = mutableListOf<Triple<Int, Int, SpanStyle>>()

        fun add(
            start: Int,
            end: Int,
            style: SpanStyle,
        ) {
            if (start >= end || end > code.length) return
            if ((start until end).any { covered[it] }) return
            for (i in start until end) covered[i] = true
            result += Triple(start, end, style)
        }

        val commentStyle = SpanStyle(color = colors.comment, fontStyle = FontStyle.Italic)
        val stringStyle = SpanStyle(color = colors.string)
        val numberStyle = SpanStyle(color = colors.number)
        val keywordStyle = SpanStyle(color = colors.keyword)
        val annotationStyle = SpanStyle(color = colors.annotation)
        val typeStyle = SpanStyle(color = colors.type)

        // 1. Comments
        when (lang) {
            "html", "xml" -> {
                HTML_COMMENT
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, commentStyle) }
            }

            "python", "ruby", "bash", "yaml", "toml", "r" -> {
                HASH_COMMENT
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, commentStyle) }
            }

            "sql" -> {
                BLOCK_COMMENT
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, commentStyle) }
                SQL_LINE_COMMENT
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, commentStyle) }
            }

            "css" -> {
                BLOCK_COMMENT
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, commentStyle) }
            }

            else -> {
                BLOCK_COMMENT
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, commentStyle) }
                LINE_COMMENT
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, commentStyle) }
            }
        }

        // 2. Strings / character literals
        when (lang) {
            "kotlin" -> {
                TRIPLE_DOUBLE_STRING
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
                DOUBLE_STRING
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
                CHAR_LITERAL
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
            }

            "python" -> {
                TRIPLE_DOUBLE_STRING
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
                TRIPLE_SINGLE_STRING
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
                DOUBLE_STRING
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
                SINGLE_STRING
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
            }

            "java", "c", "cpp", "cs", "go", "rust", "swift", "dart" -> {
                DOUBLE_STRING
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
                CHAR_LITERAL
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
            }

            "json" -> {
                DOUBLE_STRING
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
            }

            else -> {
                DOUBLE_STRING
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
                SINGLE_STRING
                    .findAll(code)
                    .forEach { add(it.range.first, it.range.last + 1, stringStyle) }
            }
        }

        // 3. Annotations / decorators
        if (lang in ANNOTATION_LANGS) {
            ANNOTATION
                .findAll(code)
                .forEach { add(it.range.first, it.range.last + 1, annotationStyle) }
        }

        // 4. Numbers
        NUMBER
            .findAll(code)
            .forEach { add(it.range.first, it.range.last + 1, numberStyle) }

        // 5. Keywords
        val keywords = KEYWORDS[lang]
        if (!keywords.isNullOrEmpty()) {
            keywordPattern(lang, keywords)
                .findAll(code)
                .forEach { add(it.range.first, it.range.last + 1, keywordStyle) }
        }

        // 6. Type identifiers (typed languages only)
        if (lang in TYPE_LANGS) {
            TYPE
                .findAll(code)
                .forEach { add(it.range.first, it.range.last + 1, typeStyle) }
        }

        return result
    }

    // ── Companion: shared regex patterns and keyword tables ──────────────────

    companion object {
        // Comments
        private val BLOCK_COMMENT = Regex("/\\*[\\s\\S]*?\\*/")
        private val HTML_COMMENT = Regex("<!--[\\s\\S]*?-->")
        private val LINE_COMMENT = Regex("//[^\\n]*")
        private val HASH_COMMENT = Regex("#[^\\n]*")
        private val SQL_LINE_COMMENT = Regex("--[^\\n]*")

        // Strings
        private val TRIPLE_DOUBLE_STRING = Regex("\"\"\"[\\s\\S]*?\"\"\"")
        private val TRIPLE_SINGLE_STRING = Regex("'''[\\s\\S]*?'''")
        private val DOUBLE_STRING = Regex("\"(?:\\\\.|[^\"\\\\])*\"")
        private val SINGLE_STRING = Regex("'(?:\\\\.|[^'\\\\])*'")
        private val CHAR_LITERAL = Regex("'(?:\\\\.|[^'\\\\])'")

        // Other tokens
        private val ANNOTATION = Regex("@[\\w.]+")
        private val NUMBER = Regex("\\b(0x[0-9a-fA-F]+[lLuU]*|0b[01]+[lL]?|\\d+\\.\\d+(?:[eE][+-]?\\d+)?[fFdD]?|\\d+[lLuU]?)\\b")
        private val TYPE = Regex("\\b[A-Z][a-zA-Z0-9]+\\b")

        private val ANNOTATION_LANGS =
            setOf("kotlin", "java", "swift", "dart", "typescript", "javascript", "python")

        private val TYPE_LANGS =
            setOf("kotlin", "java", "typescript", "dart", "swift", "rust", "go", "c", "cpp", "cs")

        private val keywordPatternCache = HashMap<String, Regex>()

        private fun keywordPattern(
            lang: String,
            keywords: Set<String>,
        ): Regex =
            keywordPatternCache.getOrPut(lang) {
                val alternation =
                    keywords
                        .sortedByDescending { it.length }
                        .joinToString("|") { Regex.escape(it) }
                Regex("\\b($alternation)\\b")
            }

        /** Keyword sets for each supported language. */
        val KEYWORDS: Map<String, Set<String>> =
            mapOf(
                "kotlin" to
                    setOf(
                        "abstract",
                        "actual",
                        "annotation",
                        "as",
                        "break",
                        "by",
                        "catch",
                        "class",
                        "companion",
                        "const",
                        "constructor",
                        "continue",
                        "crossinline",
                        "data",
                        "do",
                        "dynamic",
                        "else",
                        "enum",
                        "expect",
                        "external",
                        "false",
                        "final",
                        "finally",
                        "for",
                        "fun",
                        "if",
                        "import",
                        "in",
                        "infix",
                        "init",
                        "inline",
                        "inner",
                        "interface",
                        "internal",
                        "is",
                        "it",
                        "lateinit",
                        "noinline",
                        "null",
                        "object",
                        "open",
                        "operator",
                        "out",
                        "override",
                        "package",
                        "private",
                        "protected",
                        "public",
                        "reified",
                        "return",
                        "sealed",
                        "super",
                        "suspend",
                        "tailrec",
                        "this",
                        "throw",
                        "true",
                        "try",
                        "typealias",
                        "val",
                        "var",
                        "vararg",
                        "when",
                        "where",
                        "while",
                    ),
                "java" to
                    setOf(
                        "abstract",
                        "assert",
                        "boolean",
                        "break",
                        "byte",
                        "case",
                        "catch",
                        "char",
                        "class",
                        "const",
                        "continue",
                        "default",
                        "do",
                        "double",
                        "else",
                        "enum",
                        "extends",
                        "false",
                        "final",
                        "finally",
                        "float",
                        "for",
                        "goto",
                        "if",
                        "implements",
                        "import",
                        "instanceof",
                        "int",
                        "interface",
                        "long",
                        "native",
                        "new",
                        "null",
                        "package",
                        "private",
                        "protected",
                        "public",
                        "return",
                        "short",
                        "static",
                        "strictfp",
                        "super",
                        "switch",
                        "synchronized",
                        "this",
                        "throw",
                        "throws",
                        "transient",
                        "true",
                        "try",
                        "var",
                        "void",
                        "volatile",
                        "while",
                    ),
                "javascript" to
                    setOf(
                        "async",
                        "await",
                        "break",
                        "case",
                        "catch",
                        "class",
                        "const",
                        "continue",
                        "debugger",
                        "default",
                        "delete",
                        "do",
                        "else",
                        "export",
                        "extends",
                        "false",
                        "finally",
                        "for",
                        "from",
                        "function",
                        "if",
                        "import",
                        "in",
                        "instanceof",
                        "let",
                        "new",
                        "null",
                        "of",
                        "return",
                        "static",
                        "super",
                        "switch",
                        "this",
                        "throw",
                        "true",
                        "try",
                        "typeof",
                        "undefined",
                        "var",
                        "void",
                        "while",
                        "with",
                        "yield",
                    ),
                "typescript" to
                    setOf(
                        "abstract",
                        "any",
                        "as",
                        "async",
                        "await",
                        "boolean",
                        "break",
                        "case",
                        "catch",
                        "class",
                        "const",
                        "constructor",
                        "continue",
                        "declare",
                        "default",
                        "delete",
                        "do",
                        "else",
                        "enum",
                        "export",
                        "extends",
                        "false",
                        "finally",
                        "for",
                        "from",
                        "function",
                        "if",
                        "implements",
                        "import",
                        "in",
                        "instanceof",
                        "interface",
                        "keyof",
                        "let",
                        "namespace",
                        "never",
                        "new",
                        "null",
                        "number",
                        "of",
                        "override",
                        "private",
                        "protected",
                        "public",
                        "readonly",
                        "return",
                        "static",
                        "string",
                        "super",
                        "switch",
                        "this",
                        "throw",
                        "true",
                        "try",
                        "type",
                        "typeof",
                        "undefined",
                        "unknown",
                        "var",
                        "void",
                        "while",
                        "yield",
                    ),
                "python" to
                    setOf(
                        "False",
                        "None",
                        "True",
                        "and",
                        "as",
                        "assert",
                        "async",
                        "await",
                        "break",
                        "class",
                        "continue",
                        "def",
                        "del",
                        "elif",
                        "else",
                        "except",
                        "finally",
                        "for",
                        "from",
                        "global",
                        "if",
                        "import",
                        "in",
                        "is",
                        "lambda",
                        "nonlocal",
                        "not",
                        "or",
                        "pass",
                        "raise",
                        "return",
                        "try",
                        "while",
                        "with",
                        "yield",
                    ),
                "swift" to
                    setOf(
                        "actor",
                        "as",
                        "associatedtype",
                        "async",
                        "await",
                        "break",
                        "case",
                        "catch",
                        "class",
                        "continue",
                        "default",
                        "defer",
                        "deinit",
                        "do",
                        "else",
                        "enum",
                        "extension",
                        "fallthrough",
                        "false",
                        "fileprivate",
                        "final",
                        "for",
                        "func",
                        "guard",
                        "if",
                        "import",
                        "in",
                        "init",
                        "inout",
                        "internal",
                        "is",
                        "lazy",
                        "let",
                        "mutating",
                        "nil",
                        "open",
                        "operator",
                        "override",
                        "private",
                        "protocol",
                        "public",
                        "required",
                        "return",
                        "self",
                        "some",
                        "static",
                        "struct",
                        "subscript",
                        "super",
                        "switch",
                        "throw",
                        "throws",
                        "true",
                        "try",
                        "typealias",
                        "var",
                        "weak",
                        "where",
                        "while",
                    ),
                "rust" to
                    setOf(
                        "as",
                        "async",
                        "await",
                        "break",
                        "const",
                        "continue",
                        "crate",
                        "dyn",
                        "else",
                        "enum",
                        "extern",
                        "false",
                        "fn",
                        "for",
                        "if",
                        "impl",
                        "in",
                        "let",
                        "loop",
                        "match",
                        "mod",
                        "move",
                        "mut",
                        "pub",
                        "ref",
                        "return",
                        "self",
                        "Self",
                        "static",
                        "struct",
                        "super",
                        "trait",
                        "true",
                        "type",
                        "union",
                        "unsafe",
                        "use",
                        "where",
                        "while",
                        "bool",
                        "char",
                        "f32",
                        "f64",
                        "i8",
                        "i16",
                        "i32",
                        "i64",
                        "i128",
                        "isize",
                        "str",
                        "u8",
                        "u16",
                        "u32",
                        "u64",
                        "u128",
                        "usize",
                    ),
                "go" to
                    setOf(
                        "break",
                        "case",
                        "chan",
                        "const",
                        "continue",
                        "default",
                        "defer",
                        "else",
                        "fallthrough",
                        "false",
                        "for",
                        "func",
                        "go",
                        "goto",
                        "if",
                        "import",
                        "interface",
                        "map",
                        "nil",
                        "package",
                        "range",
                        "return",
                        "select",
                        "struct",
                        "switch",
                        "true",
                        "type",
                        "var",
                        "bool",
                        "byte",
                        "complex64",
                        "complex128",
                        "error",
                        "float32",
                        "float64",
                        "int",
                        "int8",
                        "int16",
                        "int32",
                        "int64",
                        "rune",
                        "string",
                        "uint",
                        "uint8",
                        "uint16",
                        "uint32",
                        "uint64",
                        "uintptr",
                    ),
                "dart" to
                    setOf(
                        "abstract",
                        "as",
                        "assert",
                        "async",
                        "await",
                        "break",
                        "case",
                        "catch",
                        "class",
                        "const",
                        "continue",
                        "covariant",
                        "default",
                        "do",
                        "dynamic",
                        "else",
                        "enum",
                        "export",
                        "extends",
                        "extension",
                        "external",
                        "factory",
                        "false",
                        "final",
                        "finally",
                        "for",
                        "get",
                        "hide",
                        "if",
                        "implements",
                        "import",
                        "in",
                        "interface",
                        "is",
                        "late",
                        "library",
                        "mixin",
                        "new",
                        "null",
                        "of",
                        "on",
                        "operator",
                        "part",
                        "required",
                        "rethrow",
                        "return",
                        "sealed",
                        "set",
                        "show",
                        "static",
                        "super",
                        "switch",
                        "this",
                        "throw",
                        "true",
                        "try",
                        "typedef",
                        "var",
                        "void",
                        "when",
                        "with",
                        "yield",
                    ),
                "c" to
                    setOf(
                        "auto",
                        "break",
                        "case",
                        "char",
                        "const",
                        "continue",
                        "default",
                        "do",
                        "double",
                        "else",
                        "enum",
                        "extern",
                        "float",
                        "for",
                        "goto",
                        "if",
                        "inline",
                        "int",
                        "long",
                        "register",
                        "return",
                        "short",
                        "signed",
                        "sizeof",
                        "static",
                        "struct",
                        "switch",
                        "typedef",
                        "union",
                        "unsigned",
                        "void",
                        "volatile",
                        "while",
                        "NULL",
                        "true",
                        "false",
                    ),
                "cpp" to
                    setOf(
                        "alignas",
                        "alignof",
                        "auto",
                        "bool",
                        "break",
                        "case",
                        "catch",
                        "char",
                        "class",
                        "const",
                        "constexpr",
                        "continue",
                        "decltype",
                        "default",
                        "delete",
                        "do",
                        "double",
                        "else",
                        "enum",
                        "explicit",
                        "extern",
                        "false",
                        "float",
                        "for",
                        "friend",
                        "goto",
                        "if",
                        "inline",
                        "int",
                        "long",
                        "mutable",
                        "namespace",
                        "new",
                        "nullptr",
                        "operator",
                        "private",
                        "protected",
                        "public",
                        "return",
                        "short",
                        "signed",
                        "sizeof",
                        "static",
                        "struct",
                        "switch",
                        "template",
                        "this",
                        "throw",
                        "true",
                        "try",
                        "typedef",
                        "typename",
                        "union",
                        "unsigned",
                        "using",
                        "virtual",
                        "void",
                        "volatile",
                        "wchar_t",
                        "while",
                    ),
                "cs" to
                    setOf(
                        "abstract",
                        "as",
                        "async",
                        "await",
                        "base",
                        "bool",
                        "break",
                        "byte",
                        "case",
                        "catch",
                        "char",
                        "class",
                        "const",
                        "continue",
                        "decimal",
                        "default",
                        "delegate",
                        "do",
                        "double",
                        "else",
                        "enum",
                        "event",
                        "explicit",
                        "extern",
                        "false",
                        "finally",
                        "float",
                        "for",
                        "foreach",
                        "get",
                        "goto",
                        "if",
                        "implicit",
                        "in",
                        "int",
                        "interface",
                        "internal",
                        "is",
                        "lock",
                        "long",
                        "namespace",
                        "new",
                        "null",
                        "object",
                        "operator",
                        "out",
                        "override",
                        "params",
                        "partial",
                        "private",
                        "protected",
                        "public",
                        "readonly",
                        "record",
                        "ref",
                        "return",
                        "sealed",
                        "set",
                        "short",
                        "static",
                        "string",
                        "struct",
                        "switch",
                        "this",
                        "throw",
                        "true",
                        "try",
                        "typeof",
                        "uint",
                        "ulong",
                        "ushort",
                        "using",
                        "var",
                        "virtual",
                        "void",
                        "volatile",
                        "when",
                        "where",
                        "while",
                        "yield",
                    ),
                "ruby" to
                    setOf(
                        "BEGIN",
                        "END",
                        "alias",
                        "and",
                        "begin",
                        "break",
                        "case",
                        "class",
                        "def",
                        "do",
                        "else",
                        "elsif",
                        "end",
                        "ensure",
                        "false",
                        "for",
                        "if",
                        "in",
                        "module",
                        "next",
                        "nil",
                        "not",
                        "or",
                        "redo",
                        "rescue",
                        "retry",
                        "return",
                        "self",
                        "super",
                        "then",
                        "true",
                        "undef",
                        "unless",
                        "until",
                        "when",
                        "while",
                        "yield",
                    ),
                "php" to
                    setOf(
                        "abstract",
                        "and",
                        "array",
                        "as",
                        "break",
                        "callable",
                        "case",
                        "catch",
                        "class",
                        "clone",
                        "const",
                        "continue",
                        "declare",
                        "default",
                        "do",
                        "echo",
                        "else",
                        "elseif",
                        "enum",
                        "extends",
                        "false",
                        "final",
                        "finally",
                        "fn",
                        "for",
                        "foreach",
                        "function",
                        "global",
                        "goto",
                        "if",
                        "implements",
                        "instanceof",
                        "interface",
                        "isset",
                        "list",
                        "match",
                        "namespace",
                        "new",
                        "null",
                        "or",
                        "print",
                        "private",
                        "protected",
                        "public",
                        "readonly",
                        "return",
                        "static",
                        "switch",
                        "throw",
                        "trait",
                        "true",
                        "try",
                        "unset",
                        "use",
                        "var",
                        "while",
                        "yield",
                    ),
                "sql" to
                    setOf(
                        "ADD",
                        "ALL",
                        "ALTER",
                        "AND",
                        "AS",
                        "ASC",
                        "BETWEEN",
                        "BY",
                        "CASE",
                        "CHECK",
                        "COLUMN",
                        "CONSTRAINT",
                        "CREATE",
                        "CROSS",
                        "DATABASE",
                        "DEFAULT",
                        "DELETE",
                        "DESC",
                        "DISTINCT",
                        "DROP",
                        "ELSE",
                        "END",
                        "EXISTS",
                        "FOREIGN",
                        "FROM",
                        "FULL",
                        "GROUP",
                        "HAVING",
                        "INNER",
                        "INSERT",
                        "INTO",
                        "IS",
                        "JOIN",
                        "KEY",
                        "LEFT",
                        "LIKE",
                        "LIMIT",
                        "NOT",
                        "NULL",
                        "ON",
                        "OR",
                        "ORDER",
                        "OUTER",
                        "PRIMARY",
                        "REFERENCES",
                        "RIGHT",
                        "SELECT",
                        "SET",
                        "TABLE",
                        "THEN",
                        "TOP",
                        "TRUNCATE",
                        "UNION",
                        "UNIQUE",
                        "UPDATE",
                        "VALUES",
                        "VIEW",
                        "WHERE",
                        "WITH",
                    ),
                "bash" to
                    setOf(
                        "break",
                        "case",
                        "cd",
                        "continue",
                        "do",
                        "done",
                        "echo",
                        "elif",
                        "else",
                        "esac",
                        "eval",
                        "exec",
                        "exit",
                        "export",
                        "fi",
                        "for",
                        "function",
                        "if",
                        "in",
                        "local",
                        "printf",
                        "read",
                        "readonly",
                        "return",
                        "select",
                        "set",
                        "shift",
                        "source",
                        "then",
                        "time",
                        "trap",
                        "true",
                        "type",
                        "typeset",
                        "ulimit",
                        "umask",
                        "unset",
                        "until",
                        "while",
                    ),
            )
    }
}
