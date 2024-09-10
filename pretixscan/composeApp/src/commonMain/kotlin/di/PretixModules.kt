package di

import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.api.HttpClientFactory
import eu.pretix.libpretixsync.check.AsyncCheckProvider
import eu.pretix.libpretixsync.check.OnlineCheckProvider
import eu.pretix.libpretixsync.check.ProxyCheckProvider
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.sync.FileStorage
import org.koin.core.module.Module
import org.koin.dsl.module


val pretixModules: List<Module>
    get() = listOf(
        module {
            single<TicketCheckProvider> {
                val appConfig = get<AppConfig>()
                if (!appConfig.isConfigured) {
                    throw UnsupportedOperationException("Invalid operation: TicketCheckProvider can only be used after configuration has been completed.")
                }

                val appCache = get<AppCache>()

                if (appConfig.proxyMode) {
                    ProxyCheckProvider(appConfig, get<HttpClientFactory>())
                } else if (appConfig.asyncModeEnabled) {
                    AsyncCheckProvider(appConfig, appCache.dataStore)
                } else {
                    OnlineCheckProvider(
                        appConfig,
                        get<HttpClientFactory>(),
                        appCache.dataStore,
                        get<FileStorage>()
                    )
                }
            }
        }
    )
