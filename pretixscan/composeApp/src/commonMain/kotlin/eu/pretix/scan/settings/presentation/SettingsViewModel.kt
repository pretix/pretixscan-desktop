package eu.pretix.scan.settings.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.pretix.desktop.app.sync.SyncRootService
import eu.pretix.desktop.app.ui.SelectableValue
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.cache.Version
import eu.pretix.desktop.webcam.data.VideoSource
import eu.pretix.scan.settings.data.ConfigurableSettings
import eu.pretix.scan.settings.data.PrinterSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val appConfig: DataStoreConfigStore,
    private val printerSource: PrinterSource,
    private val videoSource: VideoSource,
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

    private fun loadCameras() {
        viewModelScope.launch(Dispatchers.IO) {
            videoSource.getAvailableWebcam().collectLatest { webcams ->
                val cameraNames = webcams.map { it.name }
                _form.update { it.copy(cameras = cameraNames) }
            }
        }
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
            autoPrintBadges = appConfig.autoPrintBadges,
            syncAuto = appConfig.syncAuto,
            playSounds = appConfig.playSound,
            offlineMode = appConfig.offlineMode,
            unpaidAsk = appConfig.unpaidAsk,
            uiReduceMotion = appConfig.uiReduceMotion,
            uiHideNames = appConfig.uiHideNames,
            preferredCamera = appConfig.preferredCameraName,
        )
        loadCameras()

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

    suspend fun setAutoPrintBadges(value: Boolean) {
        appConfig.autoPrintBadges = value
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

    suspend fun setUnpaidAsk(value: Boolean) {
        appConfig.unpaidAsk = value
        loadSettings()
    }

    suspend fun setPreferredCamera(name: String?) {
        appConfig.preferredCameraName = name ?: VideoSource.NO_CAMERA_NAME
        loadSettings()
    }

    fun logout() {
        syncRootService.skipFutureSyncs()
        appCache.reset()
        appConfig.resetEventConfig()
    }
}