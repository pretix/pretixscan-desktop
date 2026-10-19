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
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var stateFlow: StateFlow<MainUiState<MainUiStateData>>? = null
    private var onHandleDirectScan: (suspend (String) -> Unit)? = null
    private var isRegistered = false
    private var isSwallowingEnter = false

    private val keyEventDispatcher = KeyEventDispatcher { event ->
        handleKeyEvent(event)
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

    internal fun handleKeyEvent(event: KeyEvent): Boolean =
        when (event.id) {
            KeyEvent.KEY_PRESSED -> handleKeyPressed(event.keyCode)
            KeyEvent.KEY_TYPED -> handleKeyTyped(event.keyChar)
            KeyEvent.KEY_RELEASED -> handleKeyReleased(event.keyCode)
            else -> false
        }

    private fun handleKeyPressed(keyCode: Int): Boolean {
        if (keyCode == KeyEvent.VK_ENTER) {
            isSwallowingEnter = false
        }
        return false
    }

    private fun handleKeyTyped(char: Char): Boolean {
        when (char) {
            '\n', '\r' -> {
                val scanned = scanBuffer.toString().trim()
                if (scanned.matches(Regex("[a-zA-Z0-9=+/]{5,}"))) {
                    log.info("GlobalScan: Enter detected, triggering scan with: $scanned")
                    scope.launch {
                        onHandleDirectScan?.invoke(scanned)
                    }
                    discardTypedInput()
                    isSwallowingEnter = true
                    return true
                } else {
                    log.info("GlobalScan: Enter detected but buffer doesn't match barcode pattern: '$scanned'")
                    discardTypedInput()
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

    private fun handleKeyReleased(keyCode: Int): Boolean {
        if (keyCode != KeyEvent.VK_ENTER || !isSwallowingEnter) {
            return false
        }
        isSwallowingEnter = false
        return true
    }

    fun discardTypedInput() {
        scanBuffer.clear()
        timeoutJob?.cancel()
        timeoutJob = null
    }

    fun dispose() {
        if (isRegistered) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyEventDispatcher)
            isRegistered = false
        }
        timeoutJob?.cancel()
        timeoutJob = null
        scanBuffer.clear()
        isSwallowingEnter = false
        onHandleDirectScan = null
        stateFlow = null
        log.info("GlobalScanHandler unregistered")
    }
}
