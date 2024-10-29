package screen.main

import eu.pretix.desktop.cache.EventSelection

sealed class MainUiState<out T> {
    data class Success<out T>(val data: T) : MainUiState<T>()
    data object Loading : MainUiState<Nothing>()
    data object SelectEvent : MainUiState<Nothing>()
    data object SelectCheckInList : MainUiState<Nothing>()
}

data class MainUiStateData(val eventSelection: EventSelection)