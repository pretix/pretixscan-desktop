package eu.pretix.desktop.cache

import eu.pretix.libpretixsync.db.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppCache(cacheFactory: LocalCacheFactory) {
    private val dataStore = cacheFactory.createDataSource()
    private val cacheDispatcher = Dispatchers.IO

    /// Returns the number of events in the database.
    suspend fun eventsCount(): Int {
        return withContext(cacheDispatcher) {
            dataStore.count(Event::class.java).get().value()
        }
    }
}

