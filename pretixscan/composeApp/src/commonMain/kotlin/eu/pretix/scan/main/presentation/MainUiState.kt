package eu.pretix.scan.main.presentation

import eu.pretix.desktop.cache.EventSelection
import eu.pretix.scan.tickets.data.ResultState
import org.joda.time.DateTime

// Presentation model for multi-event selection
data class EventForSelection(
    val slug: String,
    val name: String,
    val subEventId: Long?,
    val dateFrom: DateTime?,
    val dateTo: DateTime?
)

sealed class MainUiState<out T> {
    data class ReadyToScan<out T>(val data: T) : MainUiState<T>()

    data class HandlingTicket<out T>(val data: T) : MainUiState<T>()
    data object Loading : MainUiState<Nothing>()
    data object SelectEvent : MainUiState<Nothing>()
    data class SelectCheckInList(val event: EventForSelection) : MainUiState<Nothing>()

    // State for sequential per-event check-in list selection in advanced mode
    data class SelectCheckInListsForMultipleEvents(
        val events: List<EventForSelection>,
        val currentEventIndex: Int = 0,
        val completedSelections: Map<String, Long> = emptyMap() // Map<eventSlug, checkInListId>
    ) : MainUiState<Nothing>()
}

data class MainUiStateData(
    val eventSelection: EventSelection,
    val secret: String? = null,
    val scanTimestamp: Long = 0L,
    val resultState: ResultState? = null
)

fun MainUiStateData.secret(secret: String?): MainUiStateData {
    return this.copy(secret = secret)
}