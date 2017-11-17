package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixdesk.PretixDeskMain

class StatusController : BaseController() {

    fun reloadCheckProvider() {
        provider = (app as PretixDeskMain).newCheckProvider()
    }

    fun retrieveInfo(): TicketCheckProvider.StatusResult {
        reloadCheckProvider()
        return provider.status()
    }
}