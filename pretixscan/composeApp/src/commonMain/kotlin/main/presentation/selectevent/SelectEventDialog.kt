package main.presentation.selectevent

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
import androidx.compose.ui.window.DialogProperties
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
    var selectedEvent by remember { mutableStateOf<RemoteEvent?>(null) }

    Dialog(onDismissRequest = { onSelectEvent(null) }) {
        // Custom shape, background, and layout for the dialog
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
                Button(
                    onClick = { onSelectEvent(selectedEvent) },
                    modifier = Modifier.padding(top = 16.dp),
                    enabled = selectedEvent != null,
                ) {
                    Text(stringResource(Res.string.ok))
                }
            }
        }
    }
}