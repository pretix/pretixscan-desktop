package screen.main.selectlist


import androidx.lifecycle.ViewModel
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.db.CheckInList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.logging.Logger

class SelectCheckInListViewModel(
    val appCache: AppCache,
    val appConfig: AppConfig,
) : ViewModel() {
    private val log = Logger.getLogger("SelectCheckInListViewModel")

    private val _uiState =
        MutableStateFlow<SelectCheckInListUiState<List<CheckInList>>>(SelectCheckInListUiState.Loading)
    val uiState: StateFlow<SelectCheckInListUiState<List<CheckInList>>> = _uiState


    init {
        loadSelectableEvents()
    }

    private fun loadSelectableEvents() {
        run {
            _uiState.value = SelectCheckInListUiState.Loading
            try {
                val eventSlug = appConfig.eventSlug!!
                val lists: List<CheckInList> = appCache.dataStore
                    .select(CheckInList::class.java)
                    .where(CheckInList.EVENT_SLUG.eq(eventSlug))
                    .get().toList()

                log.info("Found ${lists.size} available lists for selection.")
                if (lists.isEmpty()) {
                    _uiState.update { SelectCheckInListUiState.Empty }
                } else {
                    _uiState.update { SelectCheckInListUiState.Selecting(lists) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { SelectCheckInListUiState.Error(e.message ?: "Unknown error") }
            }
        }
    }
}