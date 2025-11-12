package eu.pretix.scan.main.presentation.selectevent

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.core.*
import eu.pretix.desktop.app.sync.LocalSyncRootService
import eu.pretix.desktop.app.sync.SyncError
import eu.pretix.desktop.app.sync.SyncProgressView
import eu.pretix.desktop.app.sync.SyncState
import eu.pretix.desktop.app.ui.CheckboxWithLabel
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.DialogBottomBar
import eu.pretix.desktop.app.ui.DialogBottomBarContent
import eu.pretix.desktop.app.ui.Tooltip
import eu.pretix.desktop.app.ui.asColor
import eu.pretix.libpretixsync.setup.RemoteEvent
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.*

@Composable
@Preview
fun SelectEventDialog(
    onSelectEvent: (RemoteEvent?) -> Unit,
    onSelectMultipleEvents: ((List<RemoteEvent>) -> Unit)? = null
) {
    val syncRootService = LocalSyncRootService.current
    val syncState by syncRootService.minimumSyncState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val dialogState = rememberDialogState(initiallyVisible = true)

    // Reload trigger - increment to reload
    var reloadTrigger by remember { mutableStateOf(0) }

    // State: Single-select mode
    var selectedEvent by remember { mutableStateOf<RemoteEvent?>(null) }

    // State: Multi-select mode
    var advancedMode by remember { mutableStateOf(false) }
    var selectedEventSlugs by remember { mutableStateOf<Set<String>>(emptySet()) }
    var allEvents by remember { mutableStateOf<List<RemoteEvent>>(emptyList()) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        state = dialogState,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        onDismiss = { onSelectEvent(null) })
    {
        Scrim()
        DialogPanel(
            modifier = Modifier
                .displayCutoutPadding()
                .systemBarsPadding()
                .widthIn(min = 280.dp, max = 560.dp)
                .padding(20.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE4E4E4), RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 120.dp)
                        .focusRequester(focusRequester)
                        .focusable()
                ) {
                    Column(
                        modifier = Modifier
                            .background(CustomColor.BrandOrange.asColor()),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Header with title and reload button
                        Row(
                            modifier = Modifier.padding(PaddingValues(horizontal = 16.dp)),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(Res.string.operation_select_event))

                            Spacer(Modifier.weight(1f))

                            // Reload button
                            Tooltip(stringResource(Res.string.action_reload_events)) {
                                IconButton(
                                    onClick = {
                                        reloadTrigger++  // Increment to trigger reload
                                    },
                                    enabled = syncState !is SyncState.InProgress
                                ) {
                                    Image(
                                        painter = painterResource(Res.drawable.ic_refresh_dark_24dp),
                                        contentDescription = stringResource(Res.string.action_reload_events),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        if (onSelectMultipleEvents != null && allEvents.isNotEmpty()) {
                            Row(
                                modifier = Modifier.padding(PaddingValues(horizontal = 16.dp)),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                }


                when (syncState) {
                    is SyncState.Error -> {
                        DialogBottomBarContent(modifier = Modifier
                            .align(Alignment.BottomCenter)) {
                            SyncError(syncState as SyncState.Error)
                        }
                    }

                    SyncState.Idle, is SyncState.Success -> {
                        DialogBottomBar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter),
                            primaryLabel = stringResource(Res.string.cont),
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
                        DialogBottomBarContent(modifier = Modifier
                            .align(Alignment.BottomCenter)) {
                            SyncProgressView(syncState)
                        }
                    }
                }
            }
        }
    }
}