package com.iffly.compose.markdown.render

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.CoroutineScope

/**
 * Context for building strings from markdown nodes.
 * @property layoutContext The context for text layout, including density, text measurer, text alignment, and size constraints.
 * @property designContext The context for text design, including colors, text style, font family resolver, and layout direction.
 * @property systemContext The context for system interactions, including context, clipboard, haptic feedback, etc.
 */
data class NodeStringBuilderContext(
    val layoutContext: TextLayoutContext,
    val designContext: TextStyleContext,
    val systemContext: SystemContext,
)

/**
 * Context for text layout calculation.
 * @property density The screen density used for dp/sp and pixel conversions.
 * @property textMeasurer The text measurer used to pre-measure or estimate text rendering sizes.
 * @property textAlign The target text alignment used during markdown rendering.
 * @property sizeConstraints The text container constraints (min/max width/height).
 */
data class TextLayoutContext(
    val density: Density,
    val textMeasurer: TextMeasurer,
    val textAlign: TextAlign,
    val sizeConstraints: TextSizeConstraints,
)

/**
 * Context for text appearance and style.
 * @property contentColor The current content color from Compose ambient.
 * @property textSelectionColors The colors used by text selection handles/background.
 * @property textStyle The merged text style used by markdown text rendering.
 * @property fontFamilyResolver The resolver for font family lookup and loading.
 * @property layoutDirection The current layout direction (LTR/RTL).
 */
data class TextStyleContext(
    val contentColor: Color,
    val textSelectionColors: TextSelectionColors,
    val textStyle: TextStyle,
    val fontFamilyResolver: FontFamily.Resolver,
    val layoutDirection: LayoutDirection,
)

/**
 * Context for platform/system-dependent interactions.
 * @property context Android context bound to current composition.
 * @property rootView The current Compose hosting view.
 * @property configuration The current configuration (locale, orientation, etc.).
 * @property resources Android resources bound to current configuration.
 * @property clipboard Clipboard service used for copy/paste behaviors.
 * @property uriHandler URI opener used for link navigation.
 * @property hapticFeedback Haptic feedback trigger for interaction hints.
 * @property softwareKeyboardController Software keyboard controller, nullable when unavailable.
 * @property focusManager Focus manager used for focus transitions.
 * @property coroutineScope Composition-aware coroutine scope for async side work.
 */
data class SystemContext(
    val context: Context,
    val rootView: View,
    val configuration: Configuration,
    val resources: Resources,
    val clipboard: Clipboard,
    val uriHandler: UriHandler,
    val hapticFeedback: HapticFeedback,
    val softwareKeyboardController: SoftwareKeyboardController?,
    val focusManager: FocusManager,
    val coroutineScope: CoroutineScope,
)

/**
 * Size constraints for markdown text rendering.
 * @property maxWidth Maximum available width for text layout.
 * @property maxHeight Maximum available height for text layout.
 * @property minHeight Minimum required height for text layout.
 * @property minWidth Minimum required width for text layout.
 */
data class TextSizeConstraints(
    val maxWidth: Dp = Dp.Unspecified,
    val maxHeight: Dp = Dp.Unspecified,
    val minHeight: Dp = Dp.Unspecified,
    val minWidth: Dp = Dp.Unspecified,
)

/**
 * Creates and remembers [NodeStringBuilderContext] for markdown node string building.
 *
 * @param textSizeConstraints The min/max width and height constraints of the text container.
 * @param textAlign The text alignment used by markdown rendering.
 * @param textStyle Optional external text style, merged over [LocalTextStyle].
 */
@Composable
fun rememberNodeStringBuilderContext(
    textSizeConstraints: TextSizeConstraints,
    textAlign: TextAlign,
    textStyle: TextStyle? = null,
): NodeStringBuilderContext {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val contentColor = LocalContentColor.current
    val textSelectionColors = LocalTextSelectionColors.current
    val systemTextStyle = LocalTextStyle.current
    val layoutDirection = LocalLayoutDirection.current
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val rootView = LocalView.current
    val clipboard = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    val hapticFeedback = LocalHapticFeedback.current
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val resource = LocalResources.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val mergedTextStyle = systemTextStyle.merge(textStyle)

    return remember(
        density,
        textMeasurer,
        textSizeConstraints,
        textAlign,
        contentColor,
        textSelectionColors,
        mergedTextStyle,
        fontFamilyResolver,
        layoutDirection,
        context,
        rootView,
        configuration,
        resource,
        clipboard,
        uriHandler,
        hapticFeedback,
        softwareKeyboardController,
        focusManager,
        scope,
    ) {
        NodeStringBuilderContext(
            layoutContext =
                TextLayoutContext(
                    density = density,
                    textMeasurer = textMeasurer,
                    textAlign = textAlign,
                    sizeConstraints = textSizeConstraints,
                ),
            designContext =
                TextStyleContext(
                    contentColor = contentColor,
                    textSelectionColors = textSelectionColors,
                    textStyle = mergedTextStyle,
                    fontFamilyResolver = fontFamilyResolver,
                    layoutDirection = layoutDirection,
                ),
            systemContext =
                SystemContext(
                    context = context,
                    rootView = rootView,
                    configuration = configuration,
                    resources = resource,
                    clipboard = clipboard,
                    uriHandler = uriHandler,
                    hapticFeedback = hapticFeedback,
                    softwareKeyboardController = softwareKeyboardController,
                    focusManager = focusManager,
                    coroutineScope = scope,
                ),
        )
    }
}
