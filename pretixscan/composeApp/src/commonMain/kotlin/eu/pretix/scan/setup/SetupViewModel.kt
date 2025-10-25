package eu.pretix.scan.setup

import androidx.lifecycle.ViewModel
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.cache.Version
import eu.pretix.desktop.migration.MigrationCoordinator
import eu.pretix.desktop.migration.MigrationResult
import eu.pretix.desktop.printing.BadgeFactory
import eu.pretix.libpretixsync.setup.SetupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class SetupViewModel(
    private val setupManager: SetupManager,
    private val configStore: DataStoreConfigStore,
    private val badgeFactory: BadgeFactory,
    private val migrationCoordinator: MigrationCoordinator
) :
    ViewModel() {
    private val _uiState = MutableStateFlow<SetupUiState<String>>(SetupUiState.Start)
    val uiState: StateFlow<SetupUiState<String>> = _uiState

    fun dismissLoginError() {
        _uiState.update { SetupUiState.Start }
    }

    /**
     * Check if migration is needed and execute it if so.
     * Uses the same UI states as the normal login flow.
     */
    suspend fun checkAndExecuteMigration() {
        if (!migrationCoordinator.needsMigration()) {
            return
        }

        _uiState.update { SetupUiState.Loading }

        when (val result = migrationCoordinator.executeMigration()) {
            is MigrationResult.Success -> {
                _uiState.update { SetupUiState.Success }
            }
            is MigrationResult.Failure -> {
                _uiState.update { SetupUiState.Error(result.error) }
            }
        }
    }

    suspend fun verifyTokenAndSetup(token: String, url: String) {
        _uiState.update { SetupUiState.Loading }

        try {
            withContext(Dispatchers.IO) {
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

                badgeFactory.setup()
            }

            _uiState.update { SetupUiState.Success }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { SetupUiState.Error(e.message ?: "Unknown error") }
        }
    }
}