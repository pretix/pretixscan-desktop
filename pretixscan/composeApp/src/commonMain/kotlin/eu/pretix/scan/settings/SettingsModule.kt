package eu.pretix.scan.settings


import eu.pretix.scan.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal val settingsModule
    get() = module {
        factoryOf(::SettingsViewModel)
    }