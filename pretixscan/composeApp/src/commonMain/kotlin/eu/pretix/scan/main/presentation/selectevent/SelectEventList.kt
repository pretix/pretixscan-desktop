package eu.pretix.scan.main.presentation.selectevent

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.ListDivider
import eu.pretix.desktop.app.ui.SelectListRow
import eu.pretix.libpretixsync.setup.RemoteEvent
import eu.pretix.scan.main.utils.formatEventDateTimeRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.error_no_available_events

@Composable
@Preview
fun SelectEventList(
    advancedMode: Boolean = false,
    selectedEvent: RemoteEvent? = null,
    selectedEventSlugs: Set<String> = emptySet(),
    onSelectEvent: (RemoteEvent) -> Unit = {},
    onEventsLoaded: (List<RemoteEvent>) -> Unit = {},
    reloadTrigger: Int = 0
) {
    val viewModel = koinViewModel<SelectEventListViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val state = rememberLazyListState()

    // Initial load and reload handling
    LaunchedEffect(reloadTrigger) {
        withContext(Dispatchers.IO) {
            viewModel.reloadEvents()
        }
    }

    when (uiState) {
        SelectEventListUiState.Empty -> {
            SelectListRow {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(stringResource(Res.string.error_no_available_events))
                }
            }
        }

        is SelectEventListUiState.Error -> {
            SelectListRow {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text((uiState as SelectEventListUiState.Error).exception)
                }
            }
        }

        SelectEventListUiState.Loading -> {
            SelectListRow {
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        is SelectEventListUiState.Selecting -> {
            val list = (uiState as SelectEventListUiState.Selecting).events

            // Notify parent of loaded events
            LaunchedEffect(list) {
                onEventsLoaded(list)
            }

            Box {
                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    itemsIndexed(list) { index, item ->
                        Row(
                            Modifier
                                .selectable(
                                    selected = isEventSelected(item, selectedEvent, selectedEventSlugs, advancedMode),
                                    onClick = { onSelectEvent(item) },
                                    indication = LocalIndication.current,
                                    interactionSource = null,
                                    role = Role.Switch
                                )
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .then(
                                    if (!advancedMode) {
                                        Modifier.selectableGroup()
                                    } else {
                                        Modifier
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {

                            if (advancedMode) {
                                Checkbox(
                                    checked = isEventSelected(item, selectedEvent, selectedEventSlugs, true),
                                    onCheckedChange = { onSelectEvent(item) }
                                )
                            } else {
                                RadioButton(
                                    selected = isEventSelected(item, selectedEvent, selectedEventSlugs, false),
                                    onClick = { onSelectEvent(item) }
                                )
                            }
                            Column {
                                Text(item.name, fontWeight = FontWeight.Bold)
                                Text(formatEventDateTimeRange(item.date_from, item.date_to))
                            }
                        }
                        ListDivider(index, list.lastIndex)
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = state
                    )
                )
            }
        }
    }
}


internal fun isEventSelected(
    item: RemoteEvent,
    selectedEvent: RemoteEvent?,
    selectedSlugs: Set<String>,
    advancedMode: Boolean
): Boolean {
    return if (advancedMode) {
        item.slug in selectedSlugs
    } else {
        item.slug == selectedEvent?.slug &&
                item.subevent_id == selectedEvent.subevent_id
    }
}
