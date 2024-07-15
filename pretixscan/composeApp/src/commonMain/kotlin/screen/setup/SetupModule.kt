package screen.setup


import org.koin.dsl.module

internal val setupModule
    get() = module {
        factory {
            SetupViewModel()
        }
    }