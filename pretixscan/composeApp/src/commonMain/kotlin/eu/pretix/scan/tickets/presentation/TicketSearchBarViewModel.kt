package eu.pretix.scan.tickets.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.check.TicketCheckProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class)
class TicketSearchBarViewModel(
    private val ticketProvider: TicketCheckProvider,
    private val appConfig: AppConfig
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    val searchSuggestions = searchText
        .debounce(1000L)
        .onEach {
            _isSearching.update { true }
        }
        .map {
            if(it.isBlank()) {
                emptyList()
            } else {
                withContext(Dispatchers.IO) {
                    try {
                        val result = ticketProvider.search(appConfig.eventSelectionToMap(), it, 1)
                        result
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        emptyList()
                    }
                }
            }
        }
        .onEach { _isSearching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
}


