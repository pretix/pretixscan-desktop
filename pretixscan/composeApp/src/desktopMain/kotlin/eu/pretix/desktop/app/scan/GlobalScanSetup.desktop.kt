package eu.pretix.desktop.app.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import eu.pretix.scan.main.presentation.MainUiState
import eu.pretix.scan.main.presentation.MainUiStateData
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject

@Composable
actual fun GlobalScanSetup(
    stateFlow: StateFlow<MainUiState<MainUiStateData>>,
    onHandleDirectScan: suspend (String) -> Unit
) {
    val globalScanHandler = koinInject<GlobalScanHandler>()

    DisposableEffect(Unit) {
        globalScanHandler.setHandlers(stateFlow, onHandleDirectScan)
        onDispose {
            globalScanHandler.dispose()
        }
    }
}
