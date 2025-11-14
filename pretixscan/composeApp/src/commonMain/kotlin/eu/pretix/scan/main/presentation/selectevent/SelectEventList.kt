package eu.pretix.scan.main.presentation.selectevent

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.SelectListRow
import eu.pretix.libpretixsync.setup.RemoteEvent
import eu.pretix.scan.tickets.presentation.formatDateForDisplay
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
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(list) { item ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .then(
                                    if (!advancedMode) {
                                        Modifier.selectableGroup()  // Accessibility for radio group
                                    } else {
                                        Modifier
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            // Conditional rendering: RadioButton (single) or Checkbox (multi)
                            if (advancedMode) {
                                Checkbox(
                                    checked = item.slug in selectedEventSlugs,
                                    onCheckedChange = { onSelectEvent(item) }
                                )
                            } else {
                                RadioButton(
                                    selected = item.slug == selectedEvent?.slug,
                                    onClick = { onSelectEvent(item) },
                                )
                            }

                            Column {
                                Text(item.name, fontWeight = FontWeight.Bold)
                                val startDate = formatEventDate(item.date_from)
                                val endDate = if (item.date_to != null) formatEventDate(item.date_to) else null
                                val dateText = if (endDate != null && endDate.isNotEmpty()) {
                                    "$startDate - $endDate"
                                } else {
                                    startDate
                                }
                                Text(dateText)
                            }
                        }
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


/**
 * Safely formats a Joda Time DateTime for display using locale-aware formatting.
 * Falls back to raw date string if formatting fails.
 */
private fun formatEventDate(dateTime: org.joda.time.DateTime?): String {
    if (dateTime == null) return ""

    return try {
        // Convert to LocalDate and get yyyy-MM-dd format
        val dateString = dateTime.toLocalDate().toString()  // This produces "yyyy-MM-dd"
        // Use existing formatter which handles locale-aware display
        val formatted = formatDateForDisplay(dateString)
        if (formatted.isNotEmpty() && formatted != dateString) {
            formatted
        } else {
            dateString  // Fallback to ISO format if locale formatting failed
        }
    } catch (e: Exception) {
        // Ultimate fallback
        try {
            dateTime.toLocalDate().toString()
        } catch (e2: Exception) {
            dateTime.toString()
        }
    }
}
