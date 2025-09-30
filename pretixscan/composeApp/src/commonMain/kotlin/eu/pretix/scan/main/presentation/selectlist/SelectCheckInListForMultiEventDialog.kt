package eu.pretix.scan.main.presentation.selectlist

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eu.pretix.scan.main.presentation.EventForSelection
import eu.pretix.scan.tickets.presentation.formatDateForDisplay
import eu.pretix.scan.tickets.presentation.formatTimeForDisplay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.cancel
import pretixscan.composeapp.generated.resources.error_no_available_events
import pretixscan.composeapp.generated.resources.ok

/**
 * Safely formats a Joda Time DateTime for display using locale-aware formatting.
 * Falls back to raw date string if formatting fails.
 */
private fun formatEventDateTime(dateTime: org.joda.time.DateTime?): String {
    if (dateTime == null) return ""

    return try {
        // Convert to date and time strings
        val dateString = dateTime.toLocalDate().toString()  // yyyy-MM-dd
        val timeString = dateTime.toLocalTime().toString("HH:mm")  // HH:mm

        // Use existing formatters which handle locale-aware display
        val formattedDate = formatDateForDisplay(dateString)
        val formattedTime = formatTimeForDisplay(timeString)

        "$formattedDate $formattedTime"
    } catch (e: Exception) {
        // Fallback
        try {
            dateTime.toLocalDate().toString()
        } catch (e2: Exception) {
            dateTime.toString()
        }
    }
}

@Composable
@Preview
fun SelectCheckInListForMultiEventDialog(
    currentEvent: EventForSelection,
    currentEventIndex: Int,
    totalEvents: Int,
    onSelectCheckInList: (Long?) -> Unit
) {
    var selectedListId by remember(currentEvent.slug) { mutableStateOf<Long?>(null) }

    Dialog(onDismissRequest = { onSelectCheckInList(null) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header showing event context
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Event ${currentEventIndex + 1} of $totalEvents",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = currentEvent.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (currentEvent.dateFrom != null) {
                        Spacer(Modifier.height(4.dp))
                        val startDateTime = formatEventDateTime(currentEvent.dateFrom)
                        val endDateTime = formatEventDateTime(currentEvent.dateTo)
                        val dateText = if (endDateTime.isNotEmpty()) {
                            "$startDateTime - $endDateTime"
                        } else {
                            startDateTime
                        }
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                }

                // Check-in list selection using parameterized existing ViewModel
                val viewModel: SelectCheckInListViewModel = koinViewModel(
                    key = currentEvent.slug,
                    parameters = { parametersOf(currentEvent.slug, currentEvent.subEventId) }
                )
                val uiState by viewModel.uiState.collectAsState()

                when (uiState) {
                    SelectCheckInListUiState.Empty -> {
                        Text(stringResource(Res.string.error_no_available_events))
                    }
                    is SelectCheckInListUiState.Error -> {
                        Text((uiState as SelectCheckInListUiState.Error).exception)
                    }
                    SelectCheckInListUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is SelectCheckInListUiState.Selecting -> {
                        val lists = (uiState as SelectCheckInListUiState.Selecting).lists
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(lists) { item ->
                                Row(
                                    Modifier.fillMaxWidth().padding(16.dp).selectableGroup(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                ) {
                                    RadioButton(
                                        selected = item.server_id == selectedListId,
                                        onClick = { selectedListId = item.server_id },
                                    )
                                    Column {
                                        Text(item.name ?: "", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSelectCheckInList(null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }

                    Button(
                        onClick = { onSelectCheckInList(selectedListId) },
                        modifier = Modifier.weight(1f),
                        enabled = selectedListId != null
                    ) {
                        Text(stringResource(Res.string.ok))
                    }
                }
            }
        }
    }
}