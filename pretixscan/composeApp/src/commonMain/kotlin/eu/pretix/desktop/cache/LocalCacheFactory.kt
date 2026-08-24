package eu.pretix.desktop.cache

import eu.pretix.libpretixsync.sqldelight.SyncDatabase

/**
Helper class providing access to a local SQLite store via libpretixsync.
Presently, it exposes both the requery and SQLDelight stores during migration.

The implementation of the factory is platform specific as it requires a dedicated driver implementation.
 */
interface LocalCacheFactory {
    fun deleteDataSource()
    fun getSyncDataSource(): SyncDatabase
}
