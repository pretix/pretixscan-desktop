package settings


import org.koin.dsl.module
import settings.presentation.SettingsViewModel

internal val settingsModule
    get() = module {
        factory {
            SettingsViewModel(get(), get(), get())
        }
    }