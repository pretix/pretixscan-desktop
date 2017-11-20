package eu.pretix.pretixdesk.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import eu.pretix.libpretixsync.DummySentryImplementation
import eu.pretix.libpretixsync.db.QueuedCheckIn
import eu.pretix.libpretixsync.sync.SyncManager
import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.ui.helpers.jfxButton
import eu.pretix.pretixdesk.ui.helpers.jfxDialog
import javafx.scene.layout.StackPane
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import tornadofx.*
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*


open class BaseController : Controller() {
    protected var provider = (app as PretixDeskMain).newCheckProvider()
    protected var configStore = (app as PretixDeskMain).configStore
    private var syncStarted = -1L

    fun syncStatusText(): String {
        if (configStore.lastDownload == 0L) {
            return messages.getString("sync_status_no")
        } else {
            val period = Period(configStore.lastDownload, System.currentTimeMillis())
            var formatter: PeriodFormatter

            if (period.days > 0) {
                formatter = PeriodFormatterBuilder()
                        .appendDays().appendSuffix(" " + messages.getString("day_singular") + ", ", " " + messages.getString("day_plural") + ", ")
                        .appendHours().appendSuffix(" " + messages.getString("hour_singular"), " " + messages.getString("hour_plural"))
                        .printZeroNever()
                        .toFormatter()
            } else if (period.toStandardDuration().millis < 60 * 1000) {
                return messages.getString("sync_status_now")
            } else {
                formatter = PeriodFormatterBuilder()
                        .appendHours().appendSuffix(" " + messages.getString("hour_singular") + ", ", " " + messages.getString("hour_plural") + ", ")
                        .appendMinutes().appendSuffix(" " + messages.getString("minute_singular"), " " + messages.getString("minute_plural"))
                        .printZeroNever()
                        .toFormatter()
            }
            return MessageFormat.format(
                    messages.getString("sync_status_ago"),
                    formatter.print(period)
            );
        }
    }

    fun syncStatusLongText(): String {
        val lastSync = Calendar.getInstance()
        lastSync.timeInMillis = configStore.lastSync
        val lastSyncFailed = Calendar.getInstance()
        lastSyncFailed.timeInMillis = configStore.lastFailedSync
        val cnt = (app as PretixDeskMain).data().count(QueuedCheckIn::class.java).get().value()

        val formatter = SimpleDateFormat(messages.getString("date_format"))

        var res = messages.getString("sync_status_last") + "\n" +
                formatter.format(lastSync.time) + "\n\n" +
                messages.getString("sync_status_queue") + " " + cnt;
        if (configStore.lastFailedSync > 0) {
            res += "\n\n" + messages.getString("sync_status_last_failed") + "\n"
            res += formatter.format(lastSyncFailed.time)
            res += "\n"
            res += configStore.lastFailedSyncMsg
        }
        return res
    }

    fun triggerSync() {
        if (syncStarted > 0 && System.currentTimeMillis() - syncStarted < 1000 * 60) {
            return
        }
        syncStarted = System.currentTimeMillis()

        val upload_interval: Long = 1000
        var download_interval: Long = 30000
        if (!configStore.asyncModeEnabled) {
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


fun View.displaySyncStatus(controller: BaseController, root: StackPane) {
    val closeButton: JFXButton = this.jfxButton(messages.getString("dialog_close"))
    val dialog = this.jfxDialog(transitionType = JFXDialog.DialogTransition.BOTTOM) {
        setHeading(label(messages.getString("sync_status_head")))
        setBody(label(controller.syncStatusLongText()))
        setActions(closeButton)
    }
    closeButton.action {
        dialog.close()
    }
    dialog.show(root)
}