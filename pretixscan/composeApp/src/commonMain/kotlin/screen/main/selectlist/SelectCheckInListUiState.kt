package screen.main.selectlist

import eu.pretix.libpretixsync.sqldelight.CheckInList

sealed class SelectCheckInListUiState<out T> {
    data object Loading : SelectCheckInListUiState<Nothing>()
    data class Selecting(val lists: List<CheckInList>) : SelectCheckInListUiState<Nothing>()
    data object Empty : SelectCheckInListUiState<Nothing>()
    data class Error(val exception: String) : SelectCheckInListUiState<Nothing>()
}