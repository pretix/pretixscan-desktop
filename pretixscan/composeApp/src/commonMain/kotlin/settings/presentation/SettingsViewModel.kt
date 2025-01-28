package settings.presentation


import androidx.lifecycle.ViewModel
import app.ui.KeyValueOption
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.desktop.cache.Version
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import settings.data.PrinterSource
import settings.data.ConfigurableSettings
import java.util.logging.Logger


class SettingsViewModel(
    private val appConfig: AppConfig,
    private val printerSource: PrinterSource
) : ViewModel() {
    private val log = Logger.getLogger("SettingsViewModel")

    private val _form = MutableStateFlow(ConfigurableSettings())
    val form = _form.asStateFlow()

    suspend fun loadSettings() {
        _form.value = _form.value.copy(
            version = Version.version,
            printers = printerSource.listPrinters(),
            badgePrinter = printerSource.selectOption(appConfig.badgePrinterName),
            badgeLayout = printerSource.selectPrinterOrientation(appConfig.badgePrinterOrientation),
            layouts = printerSource.listPrinterOrientations()
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
}