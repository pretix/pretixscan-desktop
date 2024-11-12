package screen.main

import androidx.lifecycle.ViewModel
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.desktop.cache.Version
import eu.pretix.libpretixsync.sqldelight.CheckInList
import eu.pretix.libpretixsync.setup.RemoteEvent
import eu.pretix.libpretixsync.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.logging.Logger


class MainViewModel(
    private val appConfig: AppConfig,
    private val syncManager: SyncManager
) : ViewModel() {
    private val log = Logger.getLogger("MainViewModel")

    private val _uiState = MutableStateFlow<MainUiState<MainUiStateData>>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState<MainUiStateData>> = _uiState

    init {
        print("Welcome to app version ${Version.version}")

        if (appConfig.synchronizedEvents.isEmpty()) {
            log.info("No events configured, showing select event dialog")
            beginEventSelection()
        } else if (appConfig.eventSelection.isEmpty()) {
            log.info("Selected event, showing select event dialog")
            beginEventSelection()
        } else {
            loadViewModel()
        }
    }

    private fun loadViewModel() {
        val selection = appConfig.eventSelection.first()
        _uiState.update {
            MainUiState.Success(
                MainUiStateData(eventSelection = selection)
            )
        }
    }

    fun beginEventSelection() {
        _uiState.update { MainUiState.SelectEvent }
    }

    fun selectEvent(event: RemoteEvent?) {
        if (event == null) {
            // nothing to do
            return
        }
        appConfig.eventSlug = event.slug
        appConfig.subEventId = event.subevent_id ?: 0L
        appConfig.eventName = event.name
        appConfig.checkInListId = 0

        minimalSyncForEvent()

        _uiState.update { MainUiState.SelectCheckInList }
    }

    private fun minimalSyncForEvent() {
        syncManager.syncMinimalEventSet(appConfig.eventSlug, appConfig.subEventId ?: 0L, null)
    }

    fun selectCheckInList(list: CheckInList?) {
        if (list == null) {
            // nothing to do
            return
        }

        appConfig.checkInListId = list.server_id!!
        appConfig.checkInListName = list.name!!

        loadViewModel()
    }
}