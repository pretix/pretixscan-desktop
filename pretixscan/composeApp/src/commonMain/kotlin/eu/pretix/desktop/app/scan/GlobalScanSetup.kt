package eu.pretix.desktop.app.scan

import androidx.compose.runtime.Composable
import eu.pretix.scan.main.presentation.MainUiState
import eu.pretix.scan.main.presentation.MainUiStateData
import kotlinx.coroutines.flow.StateFlow

@Composable
expect fun GlobalScanSetup(
    stateFlow: StateFlow<MainUiState<MainUiStateData>>,
    onHandleDirectScan: suspend (String) -> Unit
)
