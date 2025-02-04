package tickets.data

import eu.pretix.desktop.cache.AppCache
import eu.pretix.libpretixsync.models.db.toModel
import eu.pretix.libpretixsync.sqldelight.BadgeLayout
import eu.pretix.libpretixsync.sqldelight.BadgeLayoutItem
import java.util.logging.Logger

class PrintLayoutFetcher(private val appCache: AppCache) {
    private val log = Logger.getLogger("PrintLayoutFetcher")

    fun getForItemAtEvent(serverId: Long, slug: String): BadgeLayout? {
        val event = appCache.db.eventQueries.selectBySlug(slug).executeAsOneOrNull()
        if (event == null) {
            log.info("Print layout not found: unknown event slug.")
            return null
        }

        if (!event.toModel().plugins.contains("pretix.plugins.badges")) {
            log.info("Print layout not found: badges plugin not enabled.")
            return null
        }

        val localItem = appCache.db.itemQueries.selectByServerId(serverId).executeAsOneOrNull()
        if (localItem == null) {
            log.info("Print layout not found: local item position not found for $serverId.")
            return null
        }

        val badgeLayoutItem = appCache.db.badgeLayoutItemQueries.selectByItemId(localItem.id).executeAsOneOrNull()

        if (badgeLayoutItem == null) {
            log.info("Print layout not found: no badge layout found for item ${localItem.id} (serverid: $serverId).")
            return null
        }

        if (badgeLayoutItem.layout == null) {
            // "Do not print badges" is configured for this product
            log.info("Print layout not found: do not print badges for this product.")
            return null
        }

        // FIXME: latest pretixsync version required
        // return badgeLayoutItem.toModel().layout
        return null
    }
}