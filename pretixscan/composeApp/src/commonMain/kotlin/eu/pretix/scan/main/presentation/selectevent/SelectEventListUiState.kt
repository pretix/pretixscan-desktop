package eu.pretix.scan.main.presentation.selectevent

import eu.pretix.libpretixsync.setup.RemoteEvent

sealed class SelectEventListUiState<out T> {
    data object Loading : SelectEventListUiState<Nothing>()
    data class Selecting(val events: List<RemoteEvent>) : SelectEventListUiState<Nothing>()
    data object Empty : SelectEventListUiState<Nothing>()
    data class Error(val exception: String) : SelectEventListUiState<Nothing>()
}