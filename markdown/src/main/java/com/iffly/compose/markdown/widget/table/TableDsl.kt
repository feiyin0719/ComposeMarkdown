/*
 * Copyright (c) 2025.
 * Microsoft Corporation. All rights reserved.
 */

package com.iffly.compose.markdown.widget.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// DSL marker annotation to prevent implicit receiver access
@DslMarker
annotation class TableDslMarker

/**
 * DSL scope for defining table structure
 */
@TableDslMarker
interface TableScope {
    /**
     * Define a table header row
     * @param modifier Modifier to be applied to the header row
     * @param cellAlignment Default alignment for cells in this header row
     * @param content Lambda to define the header cells
     */
    fun header(
        modifier: Modifier = Modifier,
        cellAlignment: Alignment? = null,
        content: RowScope.() -> Unit
    )

    /**
     * Define the table body containing multiple rows
     * @param content Lambda to define the body rows
     */
    fun body(content: BodyScope.() -> Unit)
}

/**
 * DSL scope for defining table body with multiple rows
 */
@TableDslMarker
interface BodyScope {
    /**
     * Define a table row within the body
     * @param modifier Modifier to be applied to this row
     * @param cellAlignment Default alignment for cells in this row
     * @param content Lambda to define the row cells
     */
    fun row(
        modifier: Modifier = Modifier,
        cellAlignment: Alignment? = null,
        content: RowScope.() -> Unit
    )
}

/**
 * DSL scope for defining cells within a row
 */
@TableDslMarker
interface RowScope {
    /**
     * Define a table cell
     * @param modifier Modifier to be applied to the cell content (not background)
     * @param cellBackground Modifier to be applied to the cell background (fills entire cell)
     * @param alignment Alignment for this specific cell
     * @param content Composable content of the cell
     */
    fun cell(
        modifier: Modifier = Modifier,
        cellBackground: Modifier = Modifier,
        alignment: Alignment? = null,
        content: @Composable () -> Unit
    )
}
