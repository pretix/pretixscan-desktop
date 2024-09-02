package di

import eu.pretix.desktop.cache.di.cacheModules
import org.koin.core.KoinApplication
import screen.screenModules

fun KoinApplication.initModules() {
    modules(platformModules)
    modules(screenModules)
    modules(cacheModules)
}