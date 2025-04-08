package eu.pretix.desktop.cache

import eu.pretix.libpretixsync.sqldelight.SyncDatabase
import io.requery.Persistable
import io.requery.sql.EntityDataStore

/**
Local application cache for offline data persistence.

Note: the naming of the fields `data` and `db` is aligned with the android implementation [eu.pretix.pretixscan.droid.PretixScan] to facilitate re-use of code snippets.
 */
class AppCache(val cacheFactory: LocalCacheFactory) {
    private var _data: EntityDataStore<Persistable>? = null

    val data: EntityDataStore<Persistable>
        get() {
            if (_data == null) {
                _data = cacheFactory.createDataSource()
            }
            return _data!!
        }

    private var _db: SyncDatabase? = null
    val db: SyncDatabase
        get() {
            if (_db == null) {
                // Access data to init schema through requery if it hasn't been created already
                data.raw("PRAGMA user_version;").first()
                _db = cacheFactory.getSyncDataSource()
            }
            return _db!!
        }

    fun reset() {
        data.close()
        cacheFactory.deleteDataSource()
        _db = null
        _data = null
    }
}

