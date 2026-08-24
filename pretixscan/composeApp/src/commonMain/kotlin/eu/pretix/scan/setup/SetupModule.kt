package eu.pretix.scan.setup


import org.koin.dsl.module

internal val setupModule
    get() = module {
        factory {
            SetupViewModel(get(), get(), get(), get())
        }
    }