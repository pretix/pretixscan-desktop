package eu.pretix.scan.settings.presentation


import androidx.lifecycle.ViewModel
import eu.pretix.desktop.app.sync.SyncRootService
import eu.pretix.desktop.app.ui.SelectableValue
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.cache.Version
import eu.pretix.scan.settings.data.ConfigurableSettings
import eu.pretix.scan.settings.data.PrinterSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SettingsViewModel(
    private val appConfig: DataStoreConfigStore,
    private val printerSource: PrinterSource,
    private val appCache: AppCache,
    private val syncRootService: SyncRootService
) : ViewModel() {

    private val _form = MutableStateFlow(ConfigurableSettings())
    val form = _form.asStateFlow()

    private val _uiState = MutableStateFlow<SettingsUiState<String>>(SettingsUiState.Start)
    val uiState: StateFlow<SettingsUiState<String>> = _uiState

    fun dismissError() {
        _uiState.update { SettingsUiState.Start }
    }

    suspend fun loadSettings() {
        val badgePrinterWasSelected = appConfig.printBadges && appConfig.badgePrinterName != null
        _form.value = _form.value.copy(
            version = "${Version.version} (${Version.versionCode})",
            printers = printerSource.listPrinters(),
            badgePrinter = printerSource.selectOption(appConfig.badgePrinterName),
            badgeLayout = printerSource.selectPrinterOrientation(appConfig.badgePrinterOrientation),
            layouts = printerSource.listPrinterOrientations(),
            printBadges = appConfig.printBadges,
            syncAuto = appConfig.syncAuto,
            playSounds = appConfig.playSound,
            offlineMode = appConfig.offlineMode,
            uiReduceMotion = appConfig.uiReduceMotion,
            uiHideNames = appConfig.uiHideNames,
        )

        // check if printer setup is correct
        if (_form.value.printBadges && badgePrinterWasSelected && _form.value.badgePrinter == null) {
            _uiState.update { SettingsUiState.ErrorSelectedPrinterNotAvailable }
        }
    }

    suspend fun setBadgePrinter(option: SelectableValue?) {
        if (option == null) {
            return
        }
        appConfig.badgePrinterName = option.value
        loadSettings()
    }

    suspend fun setBadgePrinterLayout(option: SelectableValue?) {
        if (option == null) {
            return
        }
        appConfig.badgePrinterOrientation = option.value
        loadSettings()
    }

    suspend fun setPrintBadges(value: Boolean) {
        if (value) {
            // turning badge printing on, check that we have at least one selectable printer
            loadSettings()
            val firstPrinter = _form.value.printers.firstOrNull()
            if (firstPrinter == null) {
                // sorry
                appConfig.printBadges = false
                loadSettings()
                _uiState.update { SettingsUiState.ErrorNoAvailablePrinters }
                return
            }

            // turn printing on, preselect the first printer
            appConfig.printBadges = true
            setBadgePrinter(firstPrinter)

        } else {
            // turning printing off
            appConfig.printBadges = false
            loadSettings()
        }
    }

    suspend fun setSyncAuto(value: Boolean) {
        appConfig.syncAuto = value
        loadSettings()
    }

    suspend fun setPlaySounds(value: Boolean) {
        appConfig.playSound = value
        loadSettings()
    }

    suspend fun setUiReduceMotion(value: Boolean) {
        appConfig.uiReduceMotion = value
        loadSettings()
    }

    suspend fun setUiHideNames(value: Boolean) {
        appConfig.uiHideNames = value
        loadSettings()
    }

    suspend fun setOfflineMode(value: Boolean) {
        appConfig.offlineMode = value
        loadSettings()
    }

    fun logout() {
        syncRootService.skipFutureSyncs()
        appCache.reset()
        appConfig.resetEventConfig()
    }
}