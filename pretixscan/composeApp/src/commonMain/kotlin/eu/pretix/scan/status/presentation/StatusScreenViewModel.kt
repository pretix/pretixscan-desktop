package eu.pretix.scan.status.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.libpretixsync.check.TicketCheckProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class StatusScreenViewModel(
    private val appConfig: DataStoreConfigStore,
    private val provider: TicketCheckProvider): ViewModel() {
    private val log = Logger.getLogger("StatusScreenViewModel")

    fun loadStats() {
        viewModelScope.launch {
            val results: List<TicketCheckProvider.StatusResult>? = refreshStats()
            if (results != null) {
                _status.value = StatusUiState.Success(data = results)
            } else {
                _status.value = StatusUiState.FailedToLoadStats
            }
        }
    }

    private val _status = MutableStateFlow<StatusUiState<List<TicketCheckProvider.StatusResult>>>(StatusUiState.Loading)
    val status: StateFlow<StatusUiState<List<TicketCheckProvider.StatusResult>>> = _status.asStateFlow()

    suspend fun refreshStats(): List<TicketCheckProvider.StatusResult>? {
        try {
            val eventSelections = appConfig.eventSelections
            if (eventSelections.isEmpty()) {
                log.warning("No events selected, not refreshing stats")
                return null
            }

            val results = mutableListOf<TicketCheckProvider.StatusResult>()

            for (selection in eventSelections) {
                val result = withContext(Dispatchers.IO) {
                    provider.status(selection.eventSlug, selection.checkInListId)
                }

                if (result == null) {
                    log.warning("Failed to load stats for event ${selection.eventSlug}")
                    return null // Any failure means total failure
                }

                results.add(result)
            }

            return results
        } catch (e: Exception) {
            log.throwing(StatusScreenViewModel::class.java.simpleName, "refreshStats", e)
            return null
        }
    }
}

sealed class StatusUiState<out T> {
    object Loading : StatusUiState<Nothing>()
    data class Success<out T>(val data: T) : StatusUiState<T>()
    object FailedToLoadStats : StatusUiState<Nothing>()
}