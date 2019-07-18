package eu.pretix.pretixscan.desktop.ui

import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixscan.desktop.PretixScanMain

class StatusController : BaseController() {
    fun retrieveInfo(): TicketCheckProvider.StatusResult {
        return (app as PretixScanMain).provider.status()
    }
}