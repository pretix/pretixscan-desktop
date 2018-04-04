package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixdesk.PretixDeskMain

class StatusController : BaseController() {
    fun retrieveInfo(): TicketCheckProvider.StatusResult {
        return (app as PretixDeskMain).provider.status()
    }
}