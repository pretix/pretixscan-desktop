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
import kotlinx.coroutines.flow.asStateFlow


class SettingsViewModel(
    private val appConfig: DataStoreConfigStore,
    private val printerSource: PrinterSource,
    private val appCache: AppCache,
    private val syncRootService: SyncRootService
) : ViewModel() {

    private val _form = MutableStateFlow(ConfigurableSettings())
    val form = _form.asStateFlow()

    suspend fun loadSettings() {
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
        appConfig.printBadges = value
        loadSettings()
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