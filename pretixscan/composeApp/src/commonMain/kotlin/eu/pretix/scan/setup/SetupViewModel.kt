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
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

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

    companion object {
        fun parseHandshakeQR(scannedText: String): Pair<String, String>? {
            return try {
                if (!isStrictJson(scannedText)) {
                    return null
                }

                val json = JSONObject(JSONTokener(scannedText))

                if (json.has("version")) {
                    return null
                }

                if (!json.has("handshake_version")) {
                    return null
                }

                val handshakeVersionValue = json.get("handshake_version")
                if (handshakeVersionValue !is Int) {
                    return null
                }

                val handshakeVersion = handshakeVersionValue as Int
                if (handshakeVersion < 0 || handshakeVersion > 1) {
                    return null
                }

                if (!json.has("url") || !json.has("token")) {
                    return null
                }

                if (json.isNull("url") || json.isNull("token")) {
                    return null
                }

                val url = json.getString("url")
                val token = json.getString("token")

                Pair(url, token)
            } catch (e: Exception) {
                null
            }
        }

        private fun isStrictJson(text: String): Boolean {
            val trimmed = text.trim()
            if (!trimmed.startsWith("{")) return false

            if (trimmed.contains(Regex(",\\s*}"))) return false

            if (trimmed.contains(Regex("\\{\\s*[a-zA-Z]"))) return false

            return true
        }
    }
}