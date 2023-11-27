package eu.pretix.pretixscan.desktop.ui

import eu.pretix.libpretixsync.db.*
import eu.pretix.pretixscan.desktop.PretixScanMain
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.attribute.standard.QueuedJobCount


class SettingsController : BaseController() {

    fun toggleLargeColor(value: Boolean) {
        configStore.largeColor = value
    }

    fun toggleSound(value: Boolean) {
        configStore.playSound = value
    }

    fun toggleSyncOrders(value: Boolean) {
        configStore.syncOrders = value
    }

    fun toggleAutoSwitch(value: Boolean) {
        configStore.autoSwitchRequested = value
    }

    fun toggleAutoPrintBadges(value: Boolean) {
        configStore.autoPrintBadges = value
    }

    fun getPrintOrientation(): String? {
        return configStore.badgePrinterOrientation
    }

    fun setPrintOrientation(o: String) {
        configStore.badgePrinterOrientation = o
    }

    fun getCurrentPrinterName(): String? {
        return configStore.badgePrinterName
    }

    fun setBadgePrinter(p: PrintService?) {
        if (p is FakePrintService) {
            configStore.badgePrinterName = null
        } else {
            configStore.badgePrinterName = p?.name
        }
    }

    fun getPrinters(): Array<PrintService> {
        return PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null)
    }
}