package eu.pretix.scan.main.presentation.selectevent

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eu.pretix.desktop.app.sync.LocalSyncRootService
import eu.pretix.desktop.app.sync.SyncError
import eu.pretix.desktop.app.sync.SyncProgressView
import eu.pretix.desktop.app.sync.SyncState
import eu.pretix.libpretixsync.setup.RemoteEvent
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.ok
import pretixscan.composeapp.generated.resources.operation_select_event

@Composable
@Preview
fun SelectEventDialog(
    onSelectEvent: (RemoteEvent?) -> Unit
) {
    val syncRootService = LocalSyncRootService.current
    val syncState by syncRootService.minimumSyncState.collectAsState()
    var selectedEvent by remember { mutableStateOf<RemoteEvent?>(null) }

    Dialog(onDismissRequest = { onSelectEvent(null) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(Res.string.operation_select_event))
                SelectEventList(
                    selectedEvent = selectedEvent,
                    onSelectEvent = { selectedEvent = it },
                )
                when (syncState) {
                    is SyncState.Error -> {
                        SyncError(syncState as SyncState.Error)
                    }

                    SyncState.Idle, is SyncState.Success -> {
                        Button(
                            onClick = { onSelectEvent(selectedEvent) },
                            modifier = Modifier.padding(top = 16.dp),
                            enabled = selectedEvent != null,
                        ) {
                            Text(stringResource(Res.string.ok))
                        }
                    }

                    is SyncState.InProgress -> {
                        SyncProgressView(syncState)
                    }
                }
            }
        }
    }
}