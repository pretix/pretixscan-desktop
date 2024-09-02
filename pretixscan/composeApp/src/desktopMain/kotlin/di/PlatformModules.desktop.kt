package di

import eu.pretix.desktop.cache.JvmLocalCacheFactory
import eu.pretix.desktop.cache.LocalCacheFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModules: List<Module>
    get() = listOf(
        module {
            single<LocalCacheFactory> { JvmLocalCacheFactory() }
        },
    )