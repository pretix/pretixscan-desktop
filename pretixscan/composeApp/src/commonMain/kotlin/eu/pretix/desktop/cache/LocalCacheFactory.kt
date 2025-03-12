package eu.pretix.desktop.cache

import eu.pretix.libpretixsync.sqldelight.SyncDatabase
import io.requery.Persistable
import io.requery.sql.EntityDataStore

/**
 Helper class providing access to a local SQLite store via libpretixsync.
 Presently, it exposes both the requery and SQLDelight stores during migration.

 The implementation of the factory is platform specific as it requires a dedicated driver implementation.
 */
interface LocalCacheFactory {
    fun createDataSource(): EntityDataStore<Persistable>
    fun deleteDataSource()
    fun getSyncDataSource(): SyncDatabase
}
