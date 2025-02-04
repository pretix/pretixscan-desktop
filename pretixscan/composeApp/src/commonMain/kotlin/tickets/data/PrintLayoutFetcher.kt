package tickets.data

import eu.pretix.desktop.cache.AppCache
import eu.pretix.libpretixsync.models.db.toModel
import eu.pretix.libpretixsync.models.BadgeLayout
import eu.pretix.libpretixsync.sqldelight.BadgeLayoutItem
import java.util.logging.Logger

class PrintLayoutFetcher(private val appCache: AppCache) {
    private val log = Logger.getLogger("PrintLayoutFetcher")

    fun getForItemAtEvent(serverId: Long?, slug: String?): BadgeLayout? {
        if (serverId == null || slug == null) {
            return null
        }
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

        if (badgeLayoutItem != null) {
            val layoudId = badgeLayoutItem.layout
            if (layoudId == null) {
                // "Do not print badges" is configured for this product
                log.info("Print layout not found: do not print badges for this product.")
                return null
            }
            // A non-default badge layout is set for this product
            return appCache.db.badgeLayoutQueries.selectById(layoudId).executeAsOneOrNull()?.toModel()
        }

        return appCache.db.badgeLayoutQueries.selectDefaultForEventSlug(slug).executeAsOneOrNull()?.toModel() ?:
        BadgeLayout.defaultWithLayout("{\"layout\": [{\"type\":\"textarea\",\"left\":\"13.09\",\"bottom\":\"49.73\",\"fontsize\":\"23.6\",\"color\":[0,0,0,1],\"fontfamily\":\"Open Sans\",\"bold\":true,\"italic\":false,\"width\":\"121.83\",\"content\":\"attendee_name\",\"text\":\"Max Mustermann\",\"align\":\"center\"}]}")
    }
}