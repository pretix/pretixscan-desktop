package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixdesk.PretixDeskMain
import tornadofx.Controller

class MainController: Controller() {
    private var provider = (app as PretixDeskMain).newCheckProvider()
    private var configStore = (app as PretixDeskMain).configStore

    fun reloadCheckProvider() {
        provider = (app as PretixDeskMain).newCheckProvider()
    }

    fun toggleAsync(value: Boolean) {
        configStore.setAsyncModeEnabled(value)
        reloadCheckProvider()
    }

    fun handleScanInput(value: String): TicketCheckProvider.CheckResult? {
        return provider.check(value)
    }
}