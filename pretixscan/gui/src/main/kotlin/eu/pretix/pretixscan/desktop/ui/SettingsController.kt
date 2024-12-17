package eu.pretix.pretixscan.desktop.ui

import java.util.logging.Logger
import java.lang.SecurityManager
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup
import kotlin.reflect.jvm.jvmName


class SettingsController : BaseController() {
    private val logger: Logger = Logger.getLogger(this::class.jvmName)

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
        logger.info("Looking up print services")
        try {
            System.getSecurityManager()?.checkPrintJobAccess()
        } catch (e: SecurityException) {
            logger.warning("SecurityException on checkPrintJobAccess: $e")
        }

        val services = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null)
        logger.info("Found ${services.size} print services")

        val otherServices1 = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PRINTABLE, null)
        val otherServices2 = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.RENDERABLE_IMAGE, null)
        logger.info("Found ${otherServices1.size} print services which support PRINTABLE and ${otherServices2.size} which support RENDERABLE_IMAGE (ignored)")
        return services
    }
}