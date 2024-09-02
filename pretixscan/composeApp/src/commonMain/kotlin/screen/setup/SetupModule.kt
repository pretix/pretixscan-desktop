package screen.setup


import eu.pretix.desktop.cache.AppCache
import org.koin.dsl.module

internal val setupModule
    get() = module {
        factory {
            SetupViewModel(get<AppCache>())
        }
    }