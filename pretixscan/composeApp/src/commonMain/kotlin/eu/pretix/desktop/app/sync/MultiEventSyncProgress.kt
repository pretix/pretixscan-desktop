package eu.pretix.desktop.app.sync

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.cache.EventSelection
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.sync_completed
import pretixscan.composeapp.generated.resources.sync_error

@Composable
fun MultiEventSyncProgress(
    syncState: SyncState,
    eventSyncStates: Map<String, EventSyncState>,
    events: List<EventSelection>
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = when (syncState) {
                is SyncState.InProgress -> syncState.message
                is SyncState.Success -> stringResource(Res.string.sync_completed)
                is SyncState.Error -> stringResource(Res.string.sync_error)
                else -> ""
            },
            style = MaterialTheme.typography.titleMedium
        )

        // Show per-event progress
        events.forEach { event ->
            val state = eventSyncStates[event.eventSlug] ?: EventSyncState.Pending
            EventSyncProgressItem(event = event, state = state)
        }
    }
}

@Composable
private fun EventSyncProgressItem(
    event: EventSelection,
    state: EventSyncState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (state) {
            EventSyncState.Pending -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            is EventSyncState.InProgress -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            EventSyncState.Success -> Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            is EventSyncState.Error -> Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.eventName,
                style = MaterialTheme.typography.bodyMedium
            )
            when (state) {
                is EventSyncState.InProgress -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is EventSyncState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }
        }
    }
}