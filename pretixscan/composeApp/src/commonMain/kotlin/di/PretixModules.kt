package di

import app.DesktopSentryImpl
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.SentryInterface
import eu.pretix.libpretixsync.api.HttpClientFactory
import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.check.AsyncCheckProvider
import eu.pretix.libpretixsync.check.OnlineCheckProvider
import eu.pretix.libpretixsync.check.ProxyCheckProvider
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.setup.EventManager
import eu.pretix.libpretixsync.sync.FileStorage
import org.koin.core.module.Module
import org.koin.dsl.module
import tickets.data.PrintLayoutFetcher


val pretixModules: List<Module>
    get() = listOf(
        module {
            factory<SentryInterface> {
                DesktopSentryImpl()
            }
            factory<EventManager> {
                val store = get<AppCache>().data
                EventManager(store, get<PretixApi>(), get<AppConfig>(), false)
            }
            factory<PretixApi> {
                val config = get<AppConfig>()
                if (!config.isConfigured) {
                    throw UnsupportedOperationException("Invalid operation: PretixApi can only be used once the device has been initialised.")
                }

                val httpFactory = get<HttpClientFactory>()
                PretixApi(
                    config.apiUrl, config.apiKey, config.organizerSlug,
                    config.apiVersion,
                    httpFactory
                )
            }
            factory<TicketCheckProvider> {
                val config = get<AppConfig>()
                if (!config.isConfigured) {
                    throw UnsupportedOperationException("Invalid operation: TicketCheckProvider can only be used once the device has been initialised.")
                }

                val appCache = get<AppCache>()

                if (config.proxyMode) {
                    ProxyCheckProvider(config, get<HttpClientFactory>())
                } else if (config.asyncModeEnabled) {
                    AsyncCheckProvider(config, appCache.db)
                } else {
                    OnlineCheckProvider(
                        config,
                        get<HttpClientFactory>(),
                        appCache.db,
                        get<FileStorage>()
                    )
                }
            }
            factory {
                PrintLayoutFetcher(get())
            }
        }
    )
