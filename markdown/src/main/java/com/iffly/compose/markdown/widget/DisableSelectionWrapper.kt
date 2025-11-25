/*
 * Copyright (c) 2025.
 * Microsoft Corporation. All rights reserved.
 */

package com.iffly.compose.markdown.widget

import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

/**
 * A composable that conditionally disables text selection for its content based on the given [disabled] flag.
 * If [disabled] is true, text selection is disabled; otherwise, text selection is enabled.
 *
 * This implementation is optimized to minimize recomposition when the condition changes by using
 * a key to maintain content stability.
 *
 * @param disabled Whether to disable text selection.
 * @param content The content for which text selection may be disabled.
 */
@Composable
fun DisableSelectionWrapper(
    disabled: Boolean,
    content: @Composable () -> Unit,
) {
    // Use key to maintain content identity across condition changes
    // This prevents unnecessary recomposition of content when only the wrapper changes
    key(disabled) {
        if (disabled) {
            DisableSelection(content = content)
        } else {
            content()
        }
    }
}
