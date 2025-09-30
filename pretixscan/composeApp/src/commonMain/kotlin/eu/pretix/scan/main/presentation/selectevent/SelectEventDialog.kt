package eu.pretix.scan.main.presentation.selectevent

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
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
import eu.pretix.desktop.app.ui.CheckboxWithLabel
import eu.pretix.desktop.app.ui.Tooltip
import eu.pretix.libpretixsync.setup.RemoteEvent
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.action_reload_events
import pretixscan.composeapp.generated.resources.advanced_mode
import pretixscan.composeapp.generated.resources.advanced_mode_description
import pretixscan.composeapp.generated.resources.ic_refresh_white_24dp
import pretixscan.composeapp.generated.resources.ok
import pretixscan.composeapp.generated.resources.operation_select_event

@Composable
@Preview
fun SelectEventDialog(
    onSelectEvent: (RemoteEvent?) -> Unit,
    onSelectMultipleEvents: ((List<RemoteEvent>) -> Unit)? = null
) {
    val syncRootService = LocalSyncRootService.current
    val syncState by syncRootService.minimumSyncState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Reload trigger - increment to reload
    var reloadTrigger by remember { mutableStateOf(0) }

    // State: Single-select mode
    var selectedEvent by remember { mutableStateOf<RemoteEvent?>(null) }

    // State: Multi-select mode
    var advancedMode by remember { mutableStateOf(false) }
    var selectedEventSlugs by remember { mutableStateOf<Set<String>>(emptySet()) }
    var allEvents by remember { mutableStateOf<List<RemoteEvent>>(emptyList()) }

    Dialog(onDismissRequest = { onSelectEvent(null) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header with title and reload button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.operation_select_event))

                    // Reload button
                    Tooltip(stringResource(Res.string.action_reload_events)) {
                        IconButton(
                            onClick = {
                                reloadTrigger++  // Increment to trigger reload
                            },
                            enabled = syncState !is SyncState.InProgress
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.ic_refresh_white_24dp),
                                contentDescription = stringResource(Res.string.action_reload_events),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Advanced Mode Toggle
                if (onSelectMultipleEvents != null) {
                    CheckboxWithLabel(
                        label = stringResource(Res.string.advanced_mode),
                        description = stringResource(Res.string.advanced_mode_description),
                        checked = advancedMode,
                        onCheckedChange = {
                            advancedMode = it
                            // Reset selections when switching modes
                            if (it) {
                                selectedEvent = null
                                selectedEventSlugs = emptySet()
                            } else {
                                selectedEventSlugs = emptySet()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Event List - now supports both modes
                SelectEventList(
                    advancedMode = advancedMode,
                    selectedEvent = selectedEvent,
                    selectedEventSlugs = selectedEventSlugs,
                    onSelectEvent = { event ->
                        if (advancedMode) {
                            // Multi-select: toggle event in set
                            selectedEventSlugs = if (event.slug in selectedEventSlugs) {
                                selectedEventSlugs - event.slug
                            } else {
                                selectedEventSlugs + event.slug
                            }
                        } else {
                            // Single-select: replace
                            selectedEvent = event
                        }
                    },
                    onEventsLoaded = { events ->
                        allEvents = events
                    },
                    reloadTrigger = reloadTrigger
                )

                when (syncState) {
                    is SyncState.Error -> {
                        SyncError(syncState as SyncState.Error)
                    }

                    SyncState.Idle, is SyncState.Success -> {
                        Button(
                            onClick = {
                                if (advancedMode) {
                                    // Multi-select: return all selected events
                                    val selected = allEvents.filter { it.slug in selectedEventSlugs }
                                    coroutineScope.launch {
                                        onSelectMultipleEvents?.invoke(selected)
                                    }
                                } else {
                                    // Single-select: return one event
                                    onSelectEvent(selectedEvent)
                                }
                            },
                            modifier = Modifier.padding(top = 16.dp),
                            enabled = if (advancedMode) {
                                selectedEventSlugs.isNotEmpty()
                            } else {
                                selectedEvent != null
                            },
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