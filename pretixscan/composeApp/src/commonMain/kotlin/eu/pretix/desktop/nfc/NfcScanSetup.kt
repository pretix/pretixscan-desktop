package eu.pretix.desktop.nfc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import eu.pretix.desktop.app.sync.LocalSyncRootService
import eu.pretix.desktop.app.sync.SyncState
import org.koin.compose.koinInject

/**
 * Polls an NFC reader for as long as the calling screen is composed, and reports every chip read.
 */
@Composable
fun NfcScanSetup(onChipRead: (NfcReadEvent) -> Unit) {
    val nfcReaderService = koinInject<NfcReaderService>()
    val syncState by LocalSyncRootService.current.syncState.collectAsState()
    val currentOnChipRead by rememberUpdatedState(onChipRead)

    DisposableEffect(nfcReaderService) {
        nfcReaderService.start()
        onDispose {
            nfcReaderService.stop()
        }
    }

    LaunchedEffect(nfcReaderService, syncState) {
        if (syncState is SyncState.Success) {
            nfcReaderService.start()
        }
    }

    LaunchedEffect(nfcReaderService) {
        nfcReaderService.events.collect { currentOnChipRead(it) }
    }
}
