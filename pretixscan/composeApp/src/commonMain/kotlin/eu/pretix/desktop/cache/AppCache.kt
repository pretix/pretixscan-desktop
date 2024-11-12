package eu.pretix.desktop.cache

import eu.pretix.libpretixsync.sqldelight.SyncDatabase
import kotlinx.coroutines.Dispatchers

/**
 Local application cache for offline data persistence.

 Note: the naming of the fields `data` and `db` is aligned with the android implementation [eu.pretix.pretixscan.droid.PretixScan] to facilitate re-use of code snippets.
 */
class AppCache(val cacheFactory: LocalCacheFactory) {
    val data = cacheFactory.createDataSource()
    val db: SyncDatabase by lazy {
            // Access data to init schema through requery if it hasn't been created already
            data.raw("PRAGMA user_version;").first()
            cacheFactory.getSyncDataSource()
        }
}

