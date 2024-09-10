package screen.main

import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.check.TicketCheckProvider
import org.koin.dsl.module

internal val mainModule
    get() = module {
        factory {
            MainViewModel(get<AppCache>(), get<AppConfig>(), get<TicketCheckProvider>())
        }
    }