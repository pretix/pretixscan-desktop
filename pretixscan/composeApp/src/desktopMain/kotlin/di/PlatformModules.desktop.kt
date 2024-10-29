package di

import eu.pretix.desktop.cache.*
import eu.pretix.libpretixsync.SentryInterface
import eu.pretix.libpretixsync.api.DefaultHttpClientFactory
import eu.pretix.libpretixsync.api.HttpClientFactory
import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.setup.SetupManager
import eu.pretix.libpretixsync.sync.FileStorage
import eu.pretix.libpretixsync.sync.SyncManager
import org.json.JSONObject
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModules: List<Module>
    get() = listOf(
        module {
            single<LocalCacheFactory> { JvmLocalCacheFactory() }
        },
        module {
            factory<HttpClientFactory> {
                DefaultHttpClientFactory()
            }
        },
        module {
            factory {
                // SetupManager requires access to system information obtaining which is platform specific
                SetupManager(
                    System.getProperty("os.name"), System.getProperty("os.version"),
                    System.getProperty("os.name"), System.getProperty("os.version"),
                    "pretixSCAN", Version.version,
                    get<HttpClientFactory>()
                )
            }
            factory<SyncManager> {
                val config = get<AppConfig>()

                val uploadInterval: Long = 1000
                var downloadInterval: Long = 30000
                if (!config.asyncModeEnabled) {
                    downloadInterval = 120000
                }


                SyncManager(
                    get<AppConfig>(),
                    get<PretixApi>(),
                    get<SentryInterface>(),
                    get<AppCache>().dataStore,
                    get<FileStorage>(),
                    uploadInterval,
                    downloadInterval,
                    if (config.syncOrders) SyncManager.Profile.PRETIXSCAN else SyncManager.Profile.PRETIXSCAN_ONLINE,
                    config.badgePrinterName != null,
                    Version.versionCode,
                    JSONObject(),
                    System.getProperty("os.name"),
                    System.getProperty("os.version"),
                    System.getProperty("os.name"),
                    System.getProperty("os.version"),
                    "pretixSCAN Desktop",
                    Version.version,
                    null,
                    null
                )
            }
        },
        module {
            factory<FileStorage> {
                DesktopFileStorage(getUserDataDir())
            }
        }
    )