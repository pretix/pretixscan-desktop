package eu.pretix.pretixscan.desktop.ui

import eu.pretix.libpretixsync.db.*
import eu.pretix.pretixscan.desktop.PretixScanMain
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup


class SettingsController : BaseController() {

    fun resetApp() {
        configStore.resetEventConfig()
        (app as PretixScanMain).data().delete(QueuedCheckIn::class.java).get()
        (app as PretixScanMain).data().delete(BadgeLayout::class.java).get()
        (app as PretixScanMain).data().delete(CheckInList_Item::class.java).get()
        (app as PretixScanMain).data().delete(CheckInList::class.java).get()
        (app as PretixScanMain).data().delete(Closing::class.java).get()
        (app as PretixScanMain).data().delete(Event::class.java).get()
        (app as PretixScanMain).data().delete(Question_Item::class.java).get()
        (app as PretixScanMain).data().delete(Question_Item::class.java).get()
        (app as PretixScanMain).data().delete(Item::class.java).get()
        (app as PretixScanMain).data().delete(ItemCategory::class.java).get()
        (app as PretixScanMain).data().delete(Order::class.java).get()
        (app as PretixScanMain).data().delete(OrderPosition::class.java).get()
        (app as PretixScanMain).data().delete(Question::class.java).get()
        (app as PretixScanMain).data().delete(Quota::class.java).get()
        (app as PretixScanMain).data().delete(Receipt::class.java).get()
        (app as PretixScanMain).data().delete(ReceiptLine::class.java).get()
        (app as PretixScanMain).data().delete(ResourceLastModified::class.java).get()
        (app as PretixScanMain).data().delete(SubEvent::class.java).get()
        (app as PretixScanMain).data().delete(TaxRule::class.java).get()
        (app as PretixScanMain).data().delete(TicketLayout::class.java).get()
    }

    fun hasLocalChanges(): Boolean {
        return (app as PretixScanMain).data().count(QueuedCheckIn::class.java).get().value() > 0
    }

    fun toggleLargeColor(value: Boolean) {
        configStore.largeColor = value
    }

    fun toggleSound(value: Boolean) {
        configStore.playSound = value
    }

    fun toggleSyncOrders(value: Boolean) {
        configStore.syncOrders = value
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