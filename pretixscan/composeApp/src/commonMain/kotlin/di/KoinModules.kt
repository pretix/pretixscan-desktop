package di

import app.sync.syncModule
import eu.pretix.desktop.cache.di.cacheModules
import main.mainModule
import org.koin.core.KoinApplication
import settings.settingsModule
import setup.setupModule
import tickets.ticketsModule
import webcam.webCamModule

fun KoinApplication.initModules() {
    modules(platformModules)
    modules(cacheModules)
    modules(pretixModules)
    modules(ticketsModule)
    modules(syncModule)
    modules(setupModule)
    modules(mainModule)
    modules(webCamModule)
    modules(settingsModule)
}