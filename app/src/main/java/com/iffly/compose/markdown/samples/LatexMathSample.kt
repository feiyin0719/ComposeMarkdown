package com.iffly.compose.markdown.samples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.latex.MarkdownMathPlugin

@Composable
fun LatexMathExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val mathConfig =
        MarkdownRenderConfig
            .Builder()
            .addPlugin(
                MarkdownMathPlugin(
                    mathStyle =
                        TextStyle(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                        ),
                ),
            ).build()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                ),
        ) {
            Text(
                text =
                    "∑ LaTeX Inline Math Demo\n" +
                        "• Uses flexmark-ext-math extension\n" +
                        "• Currently renders raw LaTeX source text (no visual formula layout)\n" +
                        "• You can customize style via MarkdownMathPlugin()\n" +
                        "• Future enhancement: integrate a formula layout engine",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        MarkdownView(
            content =
                """
                # Inline LaTeX Math Examples
                
                Basic formula: ${'$'}`a^2 + b^2 = c^2`$ shows the Pythagorean theorem.
                
                Physics relation: The famous equation ${'$'}E=mc^2$ connects mass and energy.
                
                Fractions & roots: $\frac{1}{2}$, $\sqrt{2}$, and $\sqrt{a^2 + b^2}$.
                
                Greek symbols: $\alpha + \beta = \gamma$.
                
                Combined example:
                The quadratic formula is 
                ${'$'}x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}$ 
                which solves ${'$'}ax^2 + bx + c = 0$.
                
                Multiple math segments in one line: Speed is ${'$'}v=\frac{d}{t}$, force is ${'$'}F=ma$, energy is ${'$'}E=mc^2$.
                
                ---
                ${"$$"} a^2 + b^2 = c^2 ${"$$"}
                
                **Note**: Current implementation displays raw LaTeX source (without typesetting). You can integrate
                a rendering engine (e.g. KaTeX via WebView or a native math layout) in a future extension to display
                properly typeset formulas.
                """.trimIndent(),
            markdownRenderConfig = mathConfig,
            modifier = Modifier.padding(16.dp),
        )
    }
}
