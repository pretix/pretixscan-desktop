package eu.pretix.scan.settings.presentation

sealed class SettingsUiState<out T> {
    object Start : SettingsUiState<Nothing>()
    object ErrorNoAvailablePrinters : SettingsUiState<Nothing>()
    object ErrorSelectedPrinterNotAvailable : SettingsUiState<Nothing>()
}