package eu.pretix

import eu.pretix.desktop.app.DesktopSentryImpl
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.libpretixsync.SentryInterface
import eu.pretix.libpretixsync.api.HttpClientFactory
import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.check.AsyncCheckProvider
import eu.pretix.libpretixsync.check.OnlineCheckProvider
import eu.pretix.libpretixsync.check.ProxyCheckProvider
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.setup.EventManager
import eu.pretix.libpretixsync.sync.FileStorage
import eu.pretix.scan.tickets.data.PrintLayoutFetcher
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.logging.Logger

private val log = Logger.getLogger("PretixModules")


val pretixModules: List<Module>
    get() = listOf(
        module {
            factory<SentryInterface> {
                DesktopSentryImpl()
            }
            factory<EventManager> {
                EventManager(get<PretixApi>(), get<DataStoreConfigStore>(), false)
            }
            factory<PretixApi> {
                val config = get<DataStoreConfigStore>()
                if (!config.isConfigured) {
                    throw UnsupportedOperationException("Invalid operation: PretixApi can only be used once the device has been initialised using SetupManager.")
                }

                val httpFactory = get<HttpClientFactory>()
                PretixApi(
                    config.apiUrl, config.apiKey, config.organizerSlug,
                    config.apiVersion,
                    httpFactory
                )
            }
            factory<TicketCheckProvider> {
                val config = get<DataStoreConfigStore>()
                if (!config.isConfigured) {
                    throw UnsupportedOperationException("Invalid operation: TicketCheckProvider can only be used once the device has been initialised.")
                }

                val appCache = get<AppCache>()

                if (config.proxyMode) {
                    log.info("Resolving TicketCheckProvider in proxy mode")
                    ProxyCheckProvider(config, get<HttpClientFactory>())
                } else if (config.offlineMode) {
                    log.info("Resolving TicketCheckProvider in offline mode")
                    AsyncCheckProvider(config, appCache.db)
                } else {
                    log.info("Resolving TicketCheckProvider in online mode")
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
