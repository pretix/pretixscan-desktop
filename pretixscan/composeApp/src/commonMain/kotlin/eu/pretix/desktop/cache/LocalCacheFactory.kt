package eu.pretix.desktop.cache

import io.requery.Persistable
import io.requery.sql.EntityDataStore

interface LocalCacheFactory {
    fun createDataSource(): EntityDataStore<Persistable>
}
