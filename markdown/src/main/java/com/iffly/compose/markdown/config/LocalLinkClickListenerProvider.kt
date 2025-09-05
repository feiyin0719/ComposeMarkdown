package com.iffly.compose.markdown.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.LinkInteractionListener

internal val LocalLinkClickListenerProvider =
    staticCompositionLocalOf<LinkInteractionListener?> {
        null
    }

@Composable
internal fun currentLinkClickListener(): LinkInteractionListener? =
    LocalLinkClickListenerProvider.current