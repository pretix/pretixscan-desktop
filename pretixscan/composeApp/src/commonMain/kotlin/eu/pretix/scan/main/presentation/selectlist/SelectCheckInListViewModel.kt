package eu.pretix.scan.main.presentation.selectlist


import androidx.lifecycle.ViewModel
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.libpretixsync.sqldelight.CheckInList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.logging.Logger

class SelectCheckInListViewModel(
    private val appCache: AppCache,
    private val appConfig: DataStoreConfigStore,
    private val eventSlugOverride: String? = null,
    private val subEventIdOverride: Long? = null
) : ViewModel() {
    private val log = Logger.getLogger("SelectCheckInListViewModel")

    private val _uiState =
        MutableStateFlow<SelectCheckInListUiState<List<CheckInList>>>(SelectCheckInListUiState.Loading)
    val uiState: StateFlow<SelectCheckInListUiState<List<CheckInList>>> = _uiState


    init {
        loadSelectableCheckInLists()
    }

    private fun loadSelectableCheckInLists() {
        run {
            _uiState.value = SelectCheckInListUiState.Loading
            try {
                val eventSlug = eventSlugOverride ?: appConfig.eventSlug!!
                val subEventId = subEventIdOverride ?: appConfig.subEventId

                val lists: List<CheckInList> = if (subEventId != null && subEventId > 0) {
                    // Filter by subevent if specified
                    appCache.db.checkInListQueries
                        .selectByEventSlug(eventSlug)
                        .executeAsList()
                        .filter { it.subevent_id == null || it.subevent_id == subEventId }
                } else {
                    appCache.db.checkInListQueries.selectByEventSlug(eventSlug).executeAsList()
                }

                log.info("Found ${lists.size} available lists for event $eventSlug (subevent: $subEventId)")
                if (lists.isEmpty()) {
                    _uiState.update { SelectCheckInListUiState.Empty }
                } else {
                    _uiState.update { SelectCheckInListUiState.Selecting(lists) }
                }
            } catch (e: Exception) {
                log.severe("Failed to load lists: ${e.message}")
                e.printStackTrace()
                _uiState.update { SelectCheckInListUiState.Error(e.message ?: "Unknown error") }
            }
        }
    }
}