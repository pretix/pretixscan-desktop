package eu.pretix.desktop.cache

import eu.pretix.libpretixsync.models.Settings
import eu.pretix.libpretixsync.models.db.toModel
import eu.pretix.libpretixsync.utils.SettingsManager

/**
 * Serves the organizer settings that were stored during the last settings sync.
 */
class CacheSettingsManager(private val appCache: AppCache) : SettingsManager {
    override fun getBySlug(eventSlug: String): Settings? =
        appCache.db.settingsQueries.selectBySlug(eventSlug).executeAsOneOrNull()?.toModel()
}
