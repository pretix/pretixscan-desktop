package screen.main

sealed class MainUiState<out T> {
    object ReadyToScan : MainUiState<Nothing>()
    object SelectEvent : MainUiState<Nothing>()
    object SelectCheckInList : MainUiState<Nothing>()
}