package eu.pretix.scan.setup

sealed class SetupUiState<out T> {
    object Start : SetupUiState<Nothing>()
    object Loading : SetupUiState<Nothing>()
    object Success : SetupUiState<Nothing>()
    data class Error(val exception: String) : SetupUiState<Nothing>()
}