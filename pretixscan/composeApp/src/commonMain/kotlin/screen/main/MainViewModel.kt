package screen.main

import androidx.lifecycle.ViewModel
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.desktop.cache.Version
import eu.pretix.libpretixsync.api.DefaultHttpClientFactory
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.setup.SetupManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

class MainViewModel(appCache: AppCache, appConfig: AppConfig, ticketCheker: TicketCheckProvider) : ViewModel() {

    init {
        print("Welcome to app version ${Version.version}")
    }
}