package eu.pretix.scan.status.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.check.TicketCheckProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class StatusScreenViewModel(
    private val appConfig: AppConfig,
    private val provider: TicketCheckProvider): ViewModel() {
    private val log = Logger.getLogger("StatusScreenViewModel")

    fun loadStats() {
        viewModelScope.launch {
            val result: TicketCheckProvider.StatusResult? = refreshStats()
            if (result != null) {
                _status.value = StatusUiState.Success(data = result)
            } else {
                _status.value = StatusUiState.FailedToLoadStats
            }
        }
    }
    
    private val _status = MutableStateFlow<StatusUiState<TicketCheckProvider.StatusResult>>(StatusUiState.Loading)
    val status: StateFlow<StatusUiState<TicketCheckProvider.StatusResult>> = _status.asStateFlow()

    suspend fun refreshStats(): TicketCheckProvider.StatusResult? {
        try {
            val checkInListId = appConfig.checkInListId
            val eventSlug = appConfig.eventSlug
            if (checkInListId == 0L || eventSlug == null) {
                log.warning("No check-in list or event selected, not refreshing stats")
                return null
            }

            val result = withContext(Dispatchers.IO) {
                provider.status(eventSlug, checkInListId)
            }

            return result
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