package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.DummySentryImplementation
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.sync.SyncManager
import eu.pretix.pretixdesk.PretixDeskMain
import tornadofx.Controller
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.format.PeriodFormatter





class MainController : Controller() {
    private var provider = (app as PretixDeskMain).newCheckProvider()
    private var configStore = (app as PretixDeskMain).configStore
    private var syncStarted = -1L

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

    fun syncStatusText(): String {
        if (configStore.lastDownload == 0L) {
            return "NOT SYNCHRONIZED"
        } else {
            val period = Period(configStore.lastDownload, System.currentTimeMillis())
            var formatter: PeriodFormatter

            if (period.days > 0) {
                formatter = PeriodFormatterBuilder()
                        .appendDays().appendSuffix(" DAY, ", " DAYS, ")
                        .appendHours().appendSuffix(" HOUR", " HOURS, ")
                        .printZeroNever()
                        .toFormatter()
            } else if (period.toStandardDuration().millis < 60 * 1000) {
                return "SYNCED JUST NOW"
            } else {
                formatter = PeriodFormatterBuilder()
                        .appendHours().appendSuffix(" HOUR, ", " HOURS, ")
                        .appendMinutes().appendSuffix(" MINUTE", " MINUTES")
                        .printZeroNever()
                        .toFormatter()
            }
            return "SYNCED " + formatter.print(period) + " AGO"
        }
    }

    fun triggerSync() {
        if (syncStarted > 0 && System.currentTimeMillis() - syncStarted < 1000 * 60) {
            return
        }
        syncStarted = System.currentTimeMillis()

        val upload_interval: Long = 1000
        var download_interval: Long = 30000
        if (!configStore.getAsyncModeEnabled()) {
            download_interval = 120000
        }

        val syncManager = SyncManager(
                configStore,
                (app as PretixDeskMain).api(),
                DummySentryImplementation(),
                (app as PretixDeskMain).data(),
                upload_interval,
                download_interval
        )
        syncManager.sync()

        syncStarted = -1L
    }
}