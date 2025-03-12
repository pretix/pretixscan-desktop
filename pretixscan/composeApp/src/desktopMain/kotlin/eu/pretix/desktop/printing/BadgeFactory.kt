package eu.pretix.desktop.printing

import eu.pretix.desktop.cache.AppConfig
import eu.pretix.desktop.cache.DesktopFileStorage
import eu.pretix.desktop.cache.getUserCacheFolder
import eu.pretix.desktop.cache.getUserDataFolder
import eu.pretix.libpretixsync.models.BadgeLayout
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.Orientation
import org.apache.pdfbox.printing.PDFPageable
import org.apache.pdfbox.printing.PDFPrintable
import org.apache.pdfbox.printing.Scaling

import org.json.JSONObject
import settings.data.PrinterSource
import java.awt.print.Book
import java.awt.print.PageFormat
import java.awt.print.PrinterJob
import java.io.File
import java.util.logging.Logger
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.standard.OrientationRequested
import kotlin.math.abs

/**
 * Generates badge PDFs which can be printed
 */
class DesktopBadgeFactory(
    private val appConfig: AppConfig,
    private val printerSource: PrinterSource,
    private val fileStorage: DesktopFileStorage,
    private val renderer: Renderer,
    private val fontRegistrar: FontRegistrar
) : BadgeFactory {

    private val log = Logger.getLogger("DesktopBadgeFactory")

    override suspend fun setup() {
        fontRegistrar.exportAndRegisterAllFonts(getUserDataFolder())
        log.info("finished deploying fonts")
    }

    override fun printBadges(layout: BadgeLayout?, position: JSONObject) {
        if (layout == null) {
            throw IllegalStateException("BadgeLayout can't be null")
        }

        val outputFile = File(getUserCacheFolder(), "print.pdf")
        if (!outputFile.parentFile.exists()) {
            outputFile.parentFile.mkdirs()
        }

        val backgroundFileName = layout.backgroundFilename
        if (backgroundFileName != null) {
            log.info("Rendering PDF with background filename: $backgroundFileName")
            fileStorage.getFile(backgroundFileName).inputStream().use { backgroundStream ->
                renderer.writePDF(layout.layout, position, backgroundStream, outputFile)
            }
        } else {
            log.info("Rendering PDF with no background filename")
            renderer.writePDF(layout.layout, position, null, outputFile)
        }

        val service = getPreferredPrinterService()
        val document: PDDocument = Loader.loadPDF(outputFile)

        val orientation = appConfig.badgePrinterOrientation
        val job = PrinterJob.getPrinterJob()
        job.printService = service

        val attributes = HashPrintRequestAttributeSet()

        if (orientation == "Auto") {
            log.info("Printing with automatic orientation")
            printNewWay(document, attributes, job)
        } else {
            log.info("Printing with user selected orientation (old way)")
            printOldWay(orientation, job, document, attributes)
        }

        log.info("Sending to printer...")
        try {
            job.print(attributes)
        } catch (e: java.lang.ClassCastException) {
            // FIXME: perhaps we can avoid this?
            log.warning("We're probably not using the correct coroutine magic for JVM Compose ${e.stackTraceToString()}")
        }
        log.info("Printing done.")
    }

    private fun printOldWay(
        orientation: String,
        job: PrinterJob,
        document: PDDocument,
        attributes: HashPrintRequestAttributeSet
    ): PrinterJob {
        var o = Orientation.PORTRAIT
        if (orientation == "Landscape") {
            o = Orientation.LANDSCAPE
        }
        job.setPageable(PDFPageable(document, o, false, 0f))
        if (orientation == "Landscape") {
            attributes.add(OrientationRequested.LANDSCAPE)
        } else {
            attributes.add(OrientationRequested.PORTRAIT)
        }

        return job
    }

    private fun printNewWay(
        document: PDDocument,
        attributes: HashPrintRequestAttributeSet,
        job: PrinterJob
    ): PrinterJob {
        val book = Book()
        for (p in document.pages) {
            val rect = document.getPage(0).mediaBox
            val widthPoints = abs(rect.upperRightX - rect.lowerLeftX)
            val heightPoints = abs(rect.lowerLeftY - rect.upperRightY)

            if (widthPoints > heightPoints) {
                attributes.add(OrientationRequested.LANDSCAPE)
            } else {
                attributes.add(OrientationRequested.PORTRAIT)
            }

            val pf = job.defaultPage().clone() as PageFormat
            val paper = pf.paper
            paper.setSize(widthPoints.toDouble(), heightPoints.toDouble())
            paper.setImageableArea(0.0, 0.0, widthPoints.toDouble(), heightPoints.toDouble())
            pf.paper = paper
            job.validatePage(pf)

            book.append(PDFPrintable(document, Scaling.SCALE_TO_FIT), pf)
        }
        job.setPageable(book)
        return job
    }

    private fun getPreferredPrinterService(): PrintService {
        val choice = printerSource.selectOption(appConfig.badgePrinterName)
            ?: throw IllegalStateException("No printer service selected in settings.")

        val service = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null)
            .firstOrNull {
                it.name == choice.value
            }
        if (service == null) {
            throw IllegalStateException("Couldn't find print service ${choice.value}.")
        }

        return service
    }
}

