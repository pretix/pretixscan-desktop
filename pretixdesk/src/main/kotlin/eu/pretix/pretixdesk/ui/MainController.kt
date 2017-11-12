package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixdesk.PretixDeskMain
import tornadofx.Controller

class MainController: Controller() {
    var provider = (app as PretixDeskMain).newCheckProvider()

    fun handleScanInput(value: String): TicketCheckProvider.CheckResult? {
        return provider.check(value)
    }
}