package eu.pretix.scan.main

import eu.pretix.libpretixsync.setup.EventManager
import eu.pretix.scan.main.presentation.MainViewModel
import eu.pretix.scan.main.presentation.selectevent.SelectEventListViewModel
import eu.pretix.scan.main.presentation.selectlist.SelectCheckInListViewModel
import org.koin.dsl.module

internal val mainModule
    get() = module {
        factory {
            MainViewModel(get(), get(), get())
        }
        factory {
            SelectEventListViewModel(get<EventManager>())
        }
        factory { (eventSlug: String?, subEventId: Long?) ->
            SelectCheckInListViewModel(
                appCache = get(),
                appConfig = get(),
                eventSlugOverride = eventSlug,
                subEventIdOverride = subEventId
            )
        }

    }