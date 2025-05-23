package eu.pretix

import di.platformModules
import eu.pretix.desktop.app.sync.syncModule
import eu.pretix.desktop.cache.di.cacheModules
import eu.pretix.desktop.webcam.webCamModule
import eu.pretix.scan.main.mainModule
import eu.pretix.scan.settings.settingsModule
import eu.pretix.scan.setup.setupModule
import eu.pretix.scan.tickets.ticketsModule
import org.koin.core.KoinApplication

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