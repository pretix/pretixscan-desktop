package di

import eu.pretix.desktop.cache.*
import eu.pretix.libpretixsync.api.DefaultHttpClientFactory
import eu.pretix.libpretixsync.api.HttpClientFactory
import eu.pretix.libpretixsync.setup.SetupManager
import eu.pretix.libpretixsync.sync.FileStorage
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
        },
        module {
            factory<FileStorage> {
                DesktopFileStorage(getUserDataDir())
            }
        }
    )