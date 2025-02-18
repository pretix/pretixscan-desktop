package app.sync


import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val syncModule
    get() = module {
        singleOf(::SyncRootService)
    }