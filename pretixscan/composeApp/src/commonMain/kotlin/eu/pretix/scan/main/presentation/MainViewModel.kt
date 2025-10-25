package eu.pretix.scan.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.pretix.desktop.app.sync.SyncRootService
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.cache.EventSelection
import eu.pretix.desktop.cache.Version
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.setup.RemoteEvent
import eu.pretix.libpretixsync.sqldelight.CheckInList
import kotlinx.coroutines.flow.*
import java.util.logging.Logger


class MainViewModel(
    private val appConfig: DataStoreConfigStore,
    private val syncViewModel: SyncRootService,
    private val appCache: eu.pretix.desktop.cache.AppCache
) : ViewModel() {
    private val log = Logger.getLogger("MainViewModel")

    private val _uiState = MutableStateFlow<MainUiState<MainUiStateData>>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState<MainUiStateData>> = _uiState

    private val _scanType = MutableStateFlow(appConfig.scanType)
    val scanType: StateFlow<String> = _scanType

    // Event button display
    private val _eventButtonLabel = MutableStateFlow("")
    val eventButtonLabel = _eventButtonLabel.asStateFlow()

    private val _eventButtonTooltip = MutableStateFlow("")
    val eventButtonTooltip = _eventButtonTooltip.asStateFlow()

    init {
        log.info("Welcome to app version ${Version.version}. Current scan type is ${appConfig.scanType}.")

        uiState
            .onEach { state ->
                if (state is MainUiState.ReadyToScan) {
                    syncViewModel.resumeSync()
                } else {
                    syncViewModel.skipFutureSyncs()
                }
            }
            .launchIn(viewModelScope)

        loadViewModel()
    }

    fun loadViewModel() {
        val activeEvent = appConfig.activeEvent
        if (activeEvent == null) {
            log.info("No active event, showing event selection")
            beginEventSelection()
        } else {
            _uiState.update {
                MainUiState.ReadyToScan(
                    MainUiStateData(eventSelection = activeEvent)
                )
            }
        }
        _scanType.update { appConfig.scanType }
        updateEventButtonDisplay()
    }

    fun beginEventSelection() {
        _uiState.update { MainUiState.SelectEvent }
    }

    fun performFullSync() {
        syncViewModel.forceSync()
    }

    fun changeScanType(type: String) {
        appConfig.scanType = type
        _scanType.update {
            type
        }
    }

    fun updateEventButtonDisplay() {
        val selections = appConfig.eventSelections

        if (selections.size <= 1) {
            // Single event or no event
            val eventName = selections.firstOrNull()?.eventName ?: ""
            val listName = selections.firstOrNull()?.checkInListName ?: ""
            _eventButtonLabel.value = eventName
            _eventButtonTooltip.value = "$eventName - $listName"
        } else {
            // Multiple events
            _eventButtonLabel.value = "${selections.size} selected events"
            _eventButtonTooltip.value = selections.joinToString("\n") { event ->
                "${event.eventName} - ${event.checkInListName}"
            }
        }
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

        val eventSlug = appConfig.eventSlug!!
        val eventName = appConfig.eventName!!
        val subEventId = appConfig.subEventId

        val newSelection = EventSelection(
            eventSlug = eventSlug,
            eventName = eventName,
            subEventId = subEventId,
            checkInListId = list.server_id!!,
            checkInListName = list.name!!,
            dateFrom = null,
            dateTo = null
        )

        // Always replace entire selection with this single event
        appConfig.eventSelections = listOf(newSelection)
        appConfig.activeEventIndex = 0

        // Update legacy fields for backward compatibility
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

    suspend fun onHandleDirectScan(secret: String) {
        log.info("AutoScan: Handling direct scan for ticket")
        val currentState = _uiState.value
        if (currentState is MainUiState.ReadyToScan) {
            _uiState.update {
                MainUiState.HandlingTicket(currentState.data.secret(secret))
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

    suspend fun selectMultipleEvents(events: List<RemoteEvent>) {
        if (events.isEmpty()) return

        log.info("Starting check-in list selection for ${events.size} events in advanced mode")

        // Convert to presentation models
        val eventsForSelection = events.map { event ->
            EventForSelection(
                slug = event.slug,
                name = event.name,
                subEventId = event.subevent_id,
                dateFrom = event.date_from,
                dateTo = event.date_to
            )
        }

        // Sync all events first with proper error handling
        for (event in events) {
            try {
                appConfig.eventSlug = event.slug
                appConfig.subEventId = event.subevent_id ?: 0L
                appConfig.eventName = event.name

                // Ensure sync completes before proceeding
                syncViewModel.minimalSync()

                log.info("Successfully synced event ${event.slug}")
            } catch (e: Exception) {
                log.warning("Failed to sync event ${event.slug}: ${e.message}")
                // TODO: Show error dialog and abort or allow retry
                return
            }
        }

        // Transition to multi-event check-in list selection state
        _uiState.update {
            MainUiState.SelectCheckInListsForMultipleEvents(
                events = eventsForSelection,
                currentEventIndex = 0,
                completedSelections = emptyMap()
            )
        }
    }

    fun selectCheckInListForCurrentEvent(listId: Long?) {
        val currentState = _uiState.value
        if (currentState !is MainUiState.SelectCheckInListsForMultipleEvents) {
            log.warning("selectCheckInListForCurrentEvent called in wrong state")
            return
        }

        if (listId == null) {
            // User cancelled - abort entire multi-event flow, no changes to config
            log.info("Multi-event selection cancelled by user")
            _uiState.update { MainUiState.SelectEvent }
            return
        }

        val currentEvent = currentState.events[currentState.currentEventIndex]
        val updatedSelections = currentState.completedSelections + (currentEvent.slug to listId)

        // Check if more events need list selection
        if (currentState.currentEventIndex + 1 < currentState.events.size) {
            // Move to next event
            _uiState.update {
                MainUiState.SelectCheckInListsForMultipleEvents(
                    events = currentState.events,
                    currentEventIndex = currentState.currentEventIndex + 1,
                    completedSelections = updatedSelections
                )
            }
        } else {
            // All events processed - save configurations
            finalizeMultiEventSelection(currentState.events, updatedSelections)
        }
    }

    private fun finalizeMultiEventSelection(
        events: List<EventForSelection>,
        selections: Map<String, Long>
    ) {
        val eventSelections = events.mapNotNull { event ->
            val listId = selections[event.slug] ?: return@mapNotNull null

            // Fetch the actual CheckInList to get its name
            val list = appCache.db.checkInListQueries
                .selectByEventSlug(event.slug)
                .executeAsList()
                .find { it.server_id == listId }
                ?: return@mapNotNull null

            EventSelection(
                eventSlug = event.slug,
                eventName = event.name,
                subEventId = event.subEventId,
                checkInListId = listId,
                checkInListName = list.name!!,
                dateFrom = null,
                dateTo = null
            )
        }

        // Replace all event selections
        appConfig.eventSelections = eventSelections

        // Set first event as active
        if (eventSelections.isNotEmpty()) {
            appConfig.activeEventIndex = 0
        }

        log.info("Completed multi-event selection: ${eventSelections.size} events configured")

        updateEventButtonDisplay()
        loadViewModel()
    }
}