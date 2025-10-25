package eu.pretix.desktop.app.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import java.util.logging.Logger

private val log = Logger.getLogger("AutoScanEventListener")

/**
 * Desktop implementation of auto-scan event listener.
 * Monitors for alphanumeric key presses to ensure search field can capture scanner input.
 * Does not consume events, allowing them to propagate to focused elements.
 */
actual fun Modifier.autoScanEventListener(
    onAlphanumericKey: (String) -> Unit
): Modifier = this.onPreviewKeyEvent { keyEvent ->
    // Only handle key down events
    if (keyEvent.type != KeyEventType.KeyDown) {
        return@onPreviewKeyEvent false
    }

    // Check if the key is alphanumeric
    val char = keyEvent.utf16CodePoint.toChar()
    if (char.isLetterOrDigit()) {
        log.info("AutoScan: Alphanumeric key detected")
        // Notify callback but don't consume - let event propagate to search field
        onAlphanumericKey(char.toString())
        false // Don't consume, allow normal event handling
    } else {
        false
    }
}
