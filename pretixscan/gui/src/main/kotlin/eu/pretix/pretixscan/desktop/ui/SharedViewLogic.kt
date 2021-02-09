package eu.pretix.pretixscan.desktop.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import eu.pretix.libpretixsync.DummySentryImplementation
import eu.pretix.libpretixsync.db.QueuedCheckIn
import eu.pretix.libpretixsync.sync.SyncManager
import eu.pretix.pretixscan.desktop.DesktopFileStorage
import eu.pretix.pretixscan.desktop.PretixScanMain
import eu.pretix.pretixscan.desktop.VERSION
import eu.pretix.pretixscan.desktop.VERSION_CODE
import eu.pretix.pretixscan.desktop.ui.helpers.jfxAdvancedProgressDialog
import eu.pretix.pretixscan.desktop.ui.helpers.jfxButton
import eu.pretix.pretixscan.desktop.ui.helpers.jfxDialog
import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*


open class BaseController : Controller() {
    protected var configStore = (app as PretixScanMain).configStore
    protected var syncManager: SyncManager? = null

    fun soundEnabled(): Boolean {
        return configStore.playSound
    }

    fun largeColorEnabled(): Boolean {
        return configStore.largeColor
    }

    fun syncStatusText(): String {
        if ((app as PretixScanMain).syncLock.isLocked) {
            return messages.getString("sync_status_progress");
        } else if (configStore.lastDownload == 0L) {
            return messages.getString("sync_status_no")
        } else {
            val period = Period(configStore.lastDownload, System.currentTimeMillis())
            val formatter: PeriodFormatter

            if (period.months > 0 || period.years > 0) {
                return messages.getString("sync_status_long_time")
            } else if (period.days > 0) {
                formatter = PeriodFormatterBuilder()
                        .appendDays().appendSuffix(" " + messages.getString("day_singular") + ", ", " " + messages.getString("day_plural") + ", ")
                        .appendHours().appendSuffix(" " + messages.getString("hour_singular"), " " + messages.getString("hour_plural"))
                        .printZeroNever()
                        .toFormatter()
            } else if (System.currentTimeMillis() - configStore.lastDownload < 60 * 1000) {
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
            )
        }
    }

    fun syncStatusLongText(): String {
        val lastSync = Calendar.getInstance()
        lastSync.timeInMillis = configStore.lastSync
        val lastSyncFailed = Calendar.getInstance()
        lastSyncFailed.timeInMillis = configStore.lastFailedSync
        val cnt = (app as PretixScanMain).data().count(QueuedCheckIn::class.java).get().value()

        val formatter = SimpleDateFormat(messages.getString("datetime_format"))

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

    fun close() {
        syncManager?.cancel()
        Platform.exit()
        System.exit(0)
    }

    fun triggerMinimalDownload(feedback: SyncManager.ProgressFeedback? = null) {
        if (!(app as PretixScanMain).syncLock.tryLock()) {
            // A sync is already running – let's not sync, but instead just block until the
            // sync is done and then continue :)
            (app as PretixScanMain).syncLock.lock()
            (app as PretixScanMain).syncLock.unlock()
            return
        }
        try {
            initSyncManager()
            syncManager!!.syncMinimalEventSet(feedback)
        } finally {
            (app as PretixScanMain).syncLock.unlock()
        }
    }

    fun initSyncManager() {
        val upload_interval: Long = 1000
        var download_interval: Long = 30000
        if (!configStore.asyncModeEnabled) {
            download_interval = 120000
        }
        syncManager = SyncManager(
                configStore,
                (app as PretixScanMain).api(),
                DummySentryImplementation(),
                (app as PretixScanMain).data(),
                DesktopFileStorage(File(PretixScanMain.dataDir)),
                upload_interval,
                download_interval,
                if (configStore.syncOrders) SyncManager.Profile.PRETIXSCAN else SyncManager.Profile.PRETIXSCAN_ONLINE,
                configStore.badgePrinterName != null,
                VERSION_CODE,
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                "pretixSCAN",
                VERSION
        )
    }

    fun triggerSync(eh: ((SyncManager.EventSwitchRequested) -> Unit), force: Boolean = false, feedback: SyncManager.ProgressFeedback? = null) {
        if (!(app as PretixScanMain).syncLock.tryLock()) {
            if (force) {
                // A sync is already running – let's not sync, but instead just block until the
                // sync is done and then continue :)
                (app as PretixScanMain).syncLock.lock()
                (app as PretixScanMain).syncLock.unlock()

            }
            return
        }
        try {
            initSyncManager()
            syncManager!!.sync(force, configStore.checkInListId, feedback)
        } catch (e: SyncManager.EventSwitchRequested) {
            eh(e)
        } finally {
            (app as PretixScanMain).syncLock.unlock()
        }
    }
}


fun View.displaySyncStatus(controller: BaseController, root: StackPane) {
    val closeButton: JFXButton = this.jfxButton(messages.getString("dialog_close"))
    val syncButton: JFXButton = this.jfxButton(messages.getString("dialog_sync_now"))
    val dialog = this.jfxDialog(transitionType = JFXDialog.DialogTransition.BOTTOM) {
        setHeading(label(messages.getString("sync_status_head")))
        setBody(label(controller.syncStatusLongText()))
        setActions(closeButton, syncButton)
    }
    closeButton.action {
        dialog.close()
    }
    syncButton.action {
        dialog.close()
        foregroundSync(controller, root)
    }
    dialog.show(root)
}

fun View.foregroundSync(controller: BaseController, root: StackPane) {
    val progressDialog = jfxAdvancedProgressDialog(root, heading = messages["sync_progress_running"]) {}
    progressDialog.show(root)
    runAsync {
        try {
            controller.triggerSync({}, true, SyncManager.ProgressFeedback { current_action ->
                runLater {
                    progressDialog.messageLabel?.text = current_action
                }
            })
        } finally {
            runLater {
                progressDialog.close()
            }
        }
    }
}

fun View.requestReset(root: StackPane) {
    if (isDocked) {
        val okButton: JFXButton = jfxButton(messages.getString("dialog_ok").toUpperCase())
        val dialog = jfxDialog(transitionType = JFXDialog.DialogTransition.BOTTOM) {
            setBody(label(messages.getString("setup_reset_first")))
            setActions(okButton)
        }
        okButton.action {
            dialog.close()
        }
        dialog.show(root)
    }
}

fun View.forceFocus(root: StackPane) {
    // Hacky way to *really* bring the window to the front on Windows
    if (root.scene.window is Stage) {
        val stage = root.scene.window as Stage
        stage.toFront()
    }
    root.scene.window.requestFocus()
    Platform.runLater({
        primaryStage.isIconified = true
        primaryStage.isIconified = false
    })
}