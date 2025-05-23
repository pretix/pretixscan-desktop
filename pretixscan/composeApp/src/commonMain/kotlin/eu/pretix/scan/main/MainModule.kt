package eu.pretix.scan.main

import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.setup.EventManager
import eu.pretix.scan.main.presentation.MainViewModel
import eu.pretix.scan.main.presentation.selectevent.SelectEventListViewModel
import eu.pretix.scan.main.presentation.selectlist.SelectCheckInListViewModel
import org.koin.dsl.module

internal val mainModule
    get() = module {
        factory {
            MainViewModel(get(), get())
        }
        factory {
            SelectEventListViewModel(get<AppCache>(), get<AppConfig>(), get<EventManager>())
        }
        factory {
            SelectCheckInListViewModel(get<AppCache>(), get<AppConfig>())
        }

    }