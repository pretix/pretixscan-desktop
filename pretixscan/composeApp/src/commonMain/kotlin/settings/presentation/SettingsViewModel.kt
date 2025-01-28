package settings.presentation


import androidx.lifecycle.ViewModel
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.desktop.cache.Version
import eu.pretix.libpretixsync.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import settings.model.ConfigurableSettings
import settings.model.empty
import java.util.logging.Logger


class SettingsViewModel(
    private val appConfig: AppConfig,
    private val syncManager: SyncManager
) : ViewModel() {
    private val log = Logger.getLogger("SettingsViewModel")

    private val _form = MutableStateFlow(ConfigurableSettings.empty())
    val form = _form.asStateFlow()

    fun loadSettings() {
        _form.value = _form.value.copy(
            version = Version.version
        )
    }
}