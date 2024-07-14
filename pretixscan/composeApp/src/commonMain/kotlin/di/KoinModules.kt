package di

import org.koin.core.KoinApplication

fun KoinApplication.initModules() {
    modules(platformModules)
}