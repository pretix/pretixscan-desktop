package eu.pretix.scan.main.presentation.selectevent

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.sync.LocalSyncRootService
import eu.pretix.desktop.app.sync.SyncError
import eu.pretix.desktop.app.sync.SyncProgressView
import eu.pretix.desktop.app.sync.SyncState
import eu.pretix.desktop.app.ui.*
import eu.pretix.libpretixsync.setup.RemoteEvent
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.*

@Composable
@Preview
fun SelectEventDialog(
    onSelectEvent: (RemoteEvent?) -> Unit,
    onSelectMultipleEvents: (List<RemoteEvent>) -> Unit
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


    FocusedRoundedDialog(onDismiss = { onSelectEvent(null) }, content = {
        Column(
            modifier = Modifier
                .background(CustomColor.BrandDark.asColor())
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            // Header with title and reload button
            Row(
                modifier = Modifier.padding(PaddingValues(horizontal = 16.dp)),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.operation_select_event), color = Color.White)

                Spacer(Modifier.weight(1f))


                // Advanced mode button
                Tooltip(stringResource(Res.string.advanced_mode_description)) {
                    IconButton(
                        colors = if (advancedMode) IconButtonDefaults.filledTonalIconButtonColors() else IconButtonDefaults.iconButtonColors(),
                        onClick = {
                            val nextMode = !advancedMode
                            advancedMode = nextMode
                            if (nextMode) {
                                selectedEvent = null
                                selectedEventSlugs = emptySet()
                            } else {
                                selectedEventSlugs = emptySet()
                            }
                        },
                        enabled = syncState !is SyncState.InProgress
                    ) {
                        Image(
                            painter = if (advancedMode) painterResource(Res.drawable.ic_shuffle_dark_24dp) else painterResource(
                                Res.drawable.ic_shuffle_24dp
                            ),
                            contentDescription = stringResource(Res.string.advanced_mode),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }



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
    }, bottomAccessory = {

        when (syncState) {
            is SyncState.Error -> {
                DialogBottomBarContent(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    SyncError(syncState as SyncState.Error)
                }
            }

            SyncState.Idle, is SyncState.Success -> {
                DialogBottomBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
                    primaryLabel = stringResource(Res.string.text_action_select),
                    cancelLabel = stringResource(Res.string.cancel),
                    onCancel = { onSelectEvent(null) },
                    onPrimary = {
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
                    primaryEnabled = if (advancedMode) {
                        selectedEventSlugs.isNotEmpty()
                    } else {
                        selectedEvent != null
                    }
                )
            }

            is SyncState.InProgress -> {
                DialogBottomBarContent(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    SyncProgressView(syncState)
                }
            }
        }
    })
}