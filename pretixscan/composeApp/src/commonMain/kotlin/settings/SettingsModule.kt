package settings


import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import settings.presentation.SettingsViewModel

internal val settingsModule
    get() = module {
        factoryOf(::SettingsViewModel)
    }