package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.DummySentryImplementation
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.QueuedCheckIn
import eu.pretix.libpretixsync.sync.SyncManager
import eu.pretix.pretixdesk.PretixDeskMain
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import tornadofx.Controller
import tornadofx.get
import java.text.SimpleDateFormat
import java.util.*


class MainController : BaseController() {

    fun reloadCheckProvider() {
        provider = (app as PretixDeskMain).newCheckProvider()
    }

    fun toggleAsync(value: Boolean) {
        configStore.asyncModeEnabled = value
        reloadCheckProvider()
    }

    fun handleSearchInput(value: String): List<TicketCheckProvider.SearchResult>? {
        return provider.search(value)
    }

    fun handleScanInput(value: String): TicketCheckProvider.CheckResult? {
        return provider.check(value)
    }
}