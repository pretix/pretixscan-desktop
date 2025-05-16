package eu.pretix.desktop.printing

import eu.pretix.libpretixsync.models.BadgeLayout
import org.json.JSONObject

interface BadgeFactory {

    suspend fun setup()

    fun printBadges(layout: BadgeLayout?, position: JSONObject)
}