package screen.main

sealed class MainUiState<out T> {
    object Start : MainUiState<Nothing>()
    object SelectEvent : MainUiState<Nothing>()
    object SelectCheckInList : MainUiState<Nothing>()
}