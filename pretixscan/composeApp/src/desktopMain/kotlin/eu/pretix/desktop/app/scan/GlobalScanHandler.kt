package eu.pretix.desktop.app.scan

import eu.pretix.scan.main.presentation.MainUiState
import eu.pretix.scan.main.presentation.MainUiStateData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import java.util.logging.Logger

class GlobalScanHandler {
    private val log = Logger.getLogger("GlobalScanHandler")
    private val scanBuffer = StringBuilder()
    private var timeoutJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var stateFlow: StateFlow<MainUiState<MainUiStateData>>? = null
    private var onHandleDirectScan: (suspend (String) -> Unit)? = null
    private var isRegistered = false

    private val keyEventDispatcher = KeyEventDispatcher { event ->
        if (event.id == KeyEvent.KEY_TYPED) {
            handleKeyEvent(event)
        } else {
            false
        }
    }

    fun setHandlers(
        stateFlow: StateFlow<MainUiState<MainUiStateData>>,
        onHandleDirectScan: suspend (String) -> Unit
    ) {
        this.stateFlow = stateFlow
        this.onHandleDirectScan = onHandleDirectScan

        if (!isRegistered) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher)
            isRegistered = true
            log.info("GlobalScanHandler registered")
        }
    }

    private fun handleKeyEvent(event: KeyEvent): Boolean {
        val char = event.keyChar

        when (char) {
            '\n', '\r' -> {
                val scanned = scanBuffer.toString().trim()
                if (scanned.matches(Regex("[a-zA-Z0-9=+/]{5,}"))) {
                    log.info("GlobalScan: Enter detected, triggering scan with: $scanned")
                    scope.launch {
                        onHandleDirectScan?.invoke(scanned)
                    }
                    scanBuffer.clear()
                    timeoutJob?.cancel()
                    timeoutJob = null
                    return true
                } else {
                    log.info("GlobalScan: Enter detected but buffer doesn't match barcode pattern: '$scanned'")
                    scanBuffer.clear()
                    timeoutJob?.cancel()
                    timeoutJob = null
                    return false
                }
            }
            else -> {
                if (char.isLetterOrDigit() || char in "=+/") {
                    scanBuffer.append(char)

                    timeoutJob?.cancel()
                    timeoutJob = scope.launch {
                        delay(2000)
                        log.info("GlobalScan: Buffer timeout, clearing")
                        scanBuffer.clear()
                    }

                    return false
                } else {
                    return false
                }
            }
        }
    }

    fun dispose() {
        if (isRegistered) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyEventDispatcher)
            isRegistered = false
        }
        timeoutJob?.cancel()
        timeoutJob = null
        scanBuffer.clear()
        onHandleDirectScan = null
        stateFlow = null
        log.info("GlobalScanHandler unregistered")
    }
}
