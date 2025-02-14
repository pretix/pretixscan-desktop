package app.sync


import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal val syncModule
    get() = module {
        factoryOf(::SyncRootService)
    }