package eu.pretix.desktop.cache.di

import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.desktop.cache.getUserDataFolder
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val cacheModules
    get() = listOf(
        module {
            singleOf(::AppCache)
            singleOf<AppConfig>({ AppConfig(getUserDataFolder()) })
        },
    )