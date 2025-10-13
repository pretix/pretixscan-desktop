package eu.pretix.desktop.cache.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import eu.pretix.desktop.cache.*
import eu.pretix.desktop.migration.V1DirectoryLocator
import eu.pretix.libpretixsync.config.ConfigStore
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString


val cacheModules
    get() = listOf(
        module {
            // DataStore instance
            single<DataStore<Preferences>> {
                createDataStore {
                    val path = Path(getUserDataFolder())
                    path.resolve(DATA_STORE_FILE_NAME).absolutePathString()
                }
            }

            // DataStoreConfig singleton
            single<DataStoreConfig> {
                DataStoreConfig(dataStore = get())
            }

            // ConfigMigration singleton (creates its own AppConfig instance)
            single<ConfigMigration> {
                ConfigMigration(
                    oldConfig = AppConfig(V1DirectoryLocator.getV1DataDir().path),
                    newConfig = get()
                )
            }

            // ConfigStore adapter for libpretixsync
            single<DataStoreConfigStore> {
                DataStoreConfigStore(dataStoreConfig = get())
            }

            // Bind ConfigStore interface to DataStoreConfigStore
            single<ConfigStore> { get<DataStoreConfigStore>() }

            // Keep existing
            singleOf(::AppCache)
        },
    )