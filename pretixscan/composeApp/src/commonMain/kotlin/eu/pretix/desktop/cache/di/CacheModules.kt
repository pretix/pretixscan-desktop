package eu.pretix.desktop.cache.di

import eu.pretix.desktop.cache.AppCache
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val cacheModules
    get() = listOf(
        module {
            singleOf(::AppCache)
        },
    )