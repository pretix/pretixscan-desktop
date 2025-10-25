package eu.pretix.desktop.app.ui

import androidx.compose.ui.Modifier

/**
 * Modifier that listens for alphanumeric key events globally and triggers a callback.
 * Used to automatically focus the search field when a handheld scanner inputs data.
 */
expect fun Modifier.autoScanEventListener(
    onAlphanumericKey: (String) -> Unit
): Modifier
