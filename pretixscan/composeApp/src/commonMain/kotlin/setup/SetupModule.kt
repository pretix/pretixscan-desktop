package setup


import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.setup.SetupManager
import org.koin.dsl.module

internal val setupModule
    get() = module {
        factory {
            SetupViewModel(get<AppCache>(), get<SetupManager>(), get<AppConfig>())
        }
    }