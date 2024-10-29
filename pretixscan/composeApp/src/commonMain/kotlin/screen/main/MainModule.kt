package screen.main

import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.setup.EventManager
import eu.pretix.libpretixsync.sync.SyncManager
import org.koin.dsl.module
import screen.main.selectevent.SelectEventListViewModel
import screen.main.selectlist.SelectCheckInListViewModel

internal val mainModule
    get() = module {
        factory {
            MainViewModel(get<AppConfig>(), get<SyncManager>())
        }
        factory {
            SelectEventListViewModel(get<AppCache>(), get<AppConfig>(), get<EventManager>())
        }
        factory {
            SelectCheckInListViewModel(get<AppCache>(), get<AppConfig>())
        }
    }