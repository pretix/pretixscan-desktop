package main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sync.SyncRootService
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.desktop.cache.Version
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.setup.RemoteEvent
import eu.pretix.libpretixsync.sqldelight.CheckInList
import kotlinx.coroutines.flow.*
import java.util.logging.Logger


class MainViewModel(
    private val appConfig: AppConfig,
    private val syncViewModel: SyncRootService
) : ViewModel() {
    private val log = Logger.getLogger("MainViewModel")

    private val _uiState = MutableStateFlow<MainUiState<MainUiStateData>>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState<MainUiStateData>> = _uiState

    init {
        println("Welcome to app version ${Version.version}. Current scan type is ${appConfig.scanType}.")

        uiState
            .onEach { state ->
                if (state is MainUiState.ReadyToScan) {
                    syncViewModel.resumeSync()
                } else {
                    syncViewModel.skipFutureSyncs()
                }
            }
            .launchIn(viewModelScope)

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
            MainUiState.ReadyToScan(
                MainUiStateData(eventSelection = selection)
            )
        }
    }

    fun beginEventSelection() {
        _uiState.update { MainUiState.SelectEvent }
    }

    suspend fun selectEvent(event: RemoteEvent?) {
        if (event == null) {
            // nothing to do
            return
        }
        appConfig.eventSlug = event.slug
        appConfig.subEventId = event.subevent_id ?: 0L
        appConfig.eventName = event.name
        appConfig.checkInListId = 0

        syncViewModel.minimalSync()

        _uiState.update { MainUiState.SelectCheckInList }
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

    suspend fun onHandleSearchResult(searchResult: TicketCheckProvider.SearchResult) {
        val currentState = _uiState.value
        if (currentState is MainUiState.ReadyToScan) {
            _uiState.update {
                MainUiState.HandlingTicket(currentState.data.secret(searchResult.secret))
            }
        }
    }

    fun onHandleTicketHandlingDismissed() {
        val currentState = _uiState.value
        if (currentState is MainUiState.HandlingTicket) {
            _uiState.update {
                MainUiState.ReadyToScan(currentState.data.secret(null))
            }
        }
    }
}