package eu.pretix.desktop.printing

import eu.pretix.libpretixsync.models.BadgeLayout
import org.json.JSONObject

interface BadgeFactory {

    suspend fun setup()

    fun printBadges(layout: BadgeLayout?, position: JSONObject)
}

sealed class BadgePrinterUnavailableException(message: String) : Exception(message) {
    class NotSelected : BadgePrinterUnavailableException("No badge printer selected in settings.")
    class NotFound(printerName: String) : BadgePrinterUnavailableException("Badge printer $printerName is not available on this system.")
}