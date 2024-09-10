package screen.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.desktop.cache.Version
import eu.pretix.libpretixsync.api.DefaultHttpClientFactory
import eu.pretix.libpretixsync.setup.SetupManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SetupViewModel(appCache: AppCache, private val setupManager: SetupManager, private val configStore: AppConfig) :
    ViewModel() {
    private val _uiState = MutableStateFlow<SetupUiState<String>>(SetupUiState.Start)
    val uiState: StateFlow<SetupUiState<String>> = _uiState

    fun verifyToken(token: String, url: String) {
        run {
            _uiState.update { SetupUiState.Loading }

            try {
                val init = setupManager.initialize(
                    url,
                    token
                )
                configStore.setDeviceConfig(
                    init.url,
                    init.api_token,
                    init.organizer,
                    init.device_id,
                    init.unique_serial,
                    Version.versionCode
                )
                configStore.proxyMode = token.startsWith("proxy=")
                if (init.gate_name != null) {
                    configStore.deviceKnownGateName = init.gate_name!!
                    configStore.deviceKnownGateID = init.gate_id!!
                }
                if (init.security_profile == "pretixscan_online_kiosk") {
                    configStore.syncOrders = false
                }

                _uiState.update { SetupUiState.Success }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { SetupUiState.Error(e.message ?: "Unknown error") }
            }
        }
    }
}