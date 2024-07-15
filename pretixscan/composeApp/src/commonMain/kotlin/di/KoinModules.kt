package di

import org.koin.core.KoinApplication
import screen.screenModules

fun KoinApplication.initModules() {
    modules(platformModules)
    modules(screenModules)
}