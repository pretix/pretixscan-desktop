package eu.pretix.scan.main.presentation.selectevent

import androidx.lifecycle.ViewModel
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.setup.EventManager
import eu.pretix.libpretixsync.setup.RemoteEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.logging.Logger

class SelectEventListViewModel(
    appCache: AppCache,
    appConfig: AppConfig,
    private val eventManager: EventManager
) : ViewModel() {
    private val log = Logger.getLogger("SelectEventListViewModel")

    private val _uiState = MutableStateFlow<SelectEventListUiState<List<RemoteEvent>>>(SelectEventListUiState.Loading)
    val uiState: StateFlow<SelectEventListUiState<List<RemoteEvent>>> = _uiState


    init {
        loadSelectableEvents()
    }

    private fun loadSelectableEvents() {
        run {
            _uiState.value = SelectEventListUiState.Loading
            try {
                val events = eventManager.getAvailableEvents()
                log.info("Found ${events.size} available events for selection.")
                if (events.isEmpty()) {
                    _uiState.update { SelectEventListUiState.Empty }
                } else {
                    _uiState.update { SelectEventListUiState.Selecting(events) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { SelectEventListUiState.Error(e.message ?: "Unknown error") }
            }
        }
    }
}