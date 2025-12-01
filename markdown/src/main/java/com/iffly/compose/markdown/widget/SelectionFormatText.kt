package com.iffly.compose.markdown.widget

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/**
 * A Composable that renders invisible text for selection purposes.
 * This text does not take up any space in the layout and is hidden from accessibility services.
 * @param text The text to be rendered for selection.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun SelectionFormatText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Layout(
        content = {
            Text(
                text = text,
                modifier =
                    Modifier
                        .invisible()
                        .disableAccessibility(),
                style =
                    TextStyle(
                        color = Color.Transparent,
                    ),
            )
        },
        modifier =
            modifier
                .invisible()
                .disableAccessibility(),
    ) { measurables, constraints ->
        // Measure the text with unconstrained size but don't use the result
        // This ensures the text is laid out for selection purposes
        measurables.map { measurable ->
            measurable.measure(Constraints())
        }

        // Return zero size layout so it doesn't take up any space
        layout(0, 0) {
        }
    }
}

private fun Modifier.invisible(): Modifier =
    this
        .alpha(0f)
        .size(0.dp)

private fun Modifier.disableAccessibility(): Modifier =
    this.semantics {
        hideFromAccessibility()
        text = AnnotatedString("")
        contentDescription = ""
        disabled()
        focused = false
    }
