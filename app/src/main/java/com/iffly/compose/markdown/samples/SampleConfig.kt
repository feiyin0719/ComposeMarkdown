package com.iffly.compose.markdown.samples

val markdownExamples = listOf(
    MarkdownExample(
        title = "Basic Syntax Example",
        description = "Demonstrates basic usage of standard Markdown syntax"
    ) { paddingValues ->
        BasicSyntaxExample(paddingValues)
    },

    MarkdownExample(
        title = "Custom Style Example",
        description = "Shows how to customize typography styles and themes"
    ) { paddingValues ->
        CustomStyleExample(paddingValues)
    },

    MarkdownExample(
        title = "Async Loading Example",
        description = "Demonstrates async parsing and loading state handling"
    ) { paddingValues ->
        AsyncLoadingExample(paddingValues)
    },

    MarkdownExample(
        title = "Error Handling Example",
        description = "Shows graceful handling of error states"
    ) { paddingValues ->
        ErrorHandlingExample(paddingValues)
    },

    MarkdownExample(
        title = "Link Interaction Example",
        description = "Demonstrates custom link click handling"
    ) { paddingValues ->
        LinkInteractionExample(paddingValues)
    },

    MarkdownExample(
        title = "Table and Code Example",
        description = "Shows rendering effects of tables and code blocks"
    ) { paddingValues ->
        TableAndCodeExample(paddingValues)
    },

    MarkdownExample(
        title = "Task List Example",
        description = "Demonstrates GitHub-style task lists with checkboxes"
    ) { paddingValues ->
        TaskListExample(paddingValues)
    },

    MarkdownExample(
        title = "Image and Media Example",
        description = "Shows image rendering and media content handling"
    ) { paddingValues ->
        ImageAndMediaExample(paddingValues)
    },

    MarkdownExample(
        title = "Custom Plugin Example",
        description = "Demonstrates custom Block and Inline node parsing and rendering"
    ) { paddingValues ->
        CustomPluginExample(paddingValues)
    },

    MarkdownExample(
        title = "Dark Theme Example",
        description = "Demonstrates Markdown rendering effects in dark mode"
    ) { paddingValues ->
        DarkThemeExample(paddingValues)
    },

    MarkdownExample(
        title = "LazyMarkdownView Example",
        description = "Demonstrates LazyMarkdownView for large content with optimized performance"
    ) { paddingValues ->
        LazyMarkdownExample(paddingValues)
    },

    MarkdownExample(
        title = "LaTeX Math Example",
        description = "Demonstrates inline LaTeX math rendering using MarkdownMathPlugin"
    ) { paddingValues ->
        LatexMathExample(paddingValues)
    }
)
