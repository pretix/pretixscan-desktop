package screen.main

import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.setup.EventManager
import org.koin.dsl.module
import screen.main.selectevent.SelectEventListViewModel
import screen.main.selectlist.SelectCheckInListViewModel

internal val mainModule
    get() = module {
        factory {
            MainViewModel(get<AppCache>(), get<AppConfig>(), get<TicketCheckProvider>())
        }
        factory {
            SelectEventListViewModel(get<AppCache>(), get<AppConfig>(), get<EventManager>())
        }
        factory {
            SelectCheckInListViewModel(get<AppCache>(), get<AppConfig>())
        }
    }