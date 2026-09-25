package eu.pretix.desktop.cache

import eu.pretix.libpretixsync.sqldelight.SyncDatabase

/**
Local application cache for offline data persistence.

Note: the naming of the fields `data` and `db` is aligned with the android implementation [eu.pretix.pretixscan.droid.PretixScan] to facilitate re-use of code snippets.
 */
class AppCache(val cacheFactory: LocalCacheFactory) {


    private var _db: SyncDatabase? = null
    val db: SyncDatabase
        @Synchronized get() = _db ?: cacheFactory.getSyncDataSource().also { _db = it }

    @Synchronized
    fun reset() {
        _db = null
        cacheFactory.deleteDataSource()
    }
}

