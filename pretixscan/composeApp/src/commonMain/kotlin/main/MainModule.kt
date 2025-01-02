package main

import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.setup.EventManager
import main.presentation.MainViewModel
import org.koin.dsl.module
import main.presentation.selectevent.SelectEventListViewModel
import main.presentation.selectlist.SelectCheckInListViewModel

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