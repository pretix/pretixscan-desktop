package settings.presentation


import androidx.lifecycle.ViewModel
import app.sync.SyncRootService
import app.ui.KeyValueOption
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.desktop.cache.Version
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import settings.data.ConfigurableSettings
import settings.data.PrinterSource


class SettingsViewModel(
    private val appConfig: AppConfig,
    private val printerSource: PrinterSource,
    private val appCache: AppCache,
    private val syncRootService: SyncRootService
) : ViewModel() {

    private val _form = MutableStateFlow(ConfigurableSettings())
    val form = _form.asStateFlow()

    suspend fun loadSettings() {
        _form.value = _form.value.copy(
            version = Version.version,
            printers = printerSource.listPrinters(),
            badgePrinter = printerSource.selectOption(appConfig.badgePrinterName),
            badgeLayout = printerSource.selectPrinterOrientation(appConfig.badgePrinterOrientation),
            layouts = printerSource.listPrinterOrientations(),
            printBadges = appConfig.printBadges,
            syncAuto = appConfig.syncAuto
        )
    }

    suspend fun setBadgePrinter(option: KeyValueOption?) {
        if (option == null) {
            return
        }
        appConfig.badgePrinterName = option.value
        loadSettings()
    }

    suspend fun setBadgePrinterLayout(option: KeyValueOption?) {
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

    fun logout() {
        syncRootService.skipFutureSyncs()
        appCache.reset()
        appConfig.resetEventConfig()
    }
}