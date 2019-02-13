package eu.pretix.pretixdesk

import eu.pretix.libpretixprint.templating.ContentProvider
import eu.pretix.libpretixprint.templating.FontRegistry
import eu.pretix.libpretixprint.templating.FontSpecification
import eu.pretix.libpretixsync.db.BadgeLayout
import eu.pretix.libpretixsync.db.Item
import eu.pretix.libpretixprint.templating.Layout
import eu.pretix.pretixdesk.DesktopFileStorage
import eu.pretix.pretixdesk.PretixDeskMain
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.Orientation
import org.apache.pdfbox.printing.PDFPageable
import org.apache.pdfbox.printing.PDFPrintable
import org.apache.pdfbox.printing.Scaling
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.awt.print.PrinterJob
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import javax.print.PrintServiceLookup
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.standard.OrientationRequested


fun getDefaultBadgeLayout(): BadgeLayout {
    val tl = BadgeLayout()
    tl.setJson_data("{\"layout\": [{\"type\":\"textarea\",\"left\":\"13.09\",\"bottom\":\"49.73\",\"fontsize\":\"23.6\",\"color\":[0,0,0,1],\"fontfamily\":\"Open Sans\",\"bold\":true,\"italic\":false,\"width\":\"121.83\",\"content\":\"attendee_name\",\"text\":\"Max Mustermann\",\"align\":\"center\"}]}")
    return tl
}

fun getBadgeLayout(application: PretixDeskMain, position: JSONObject): BadgeLayout {
    val itemid = position.getLong("item")
    val item = application.data().select(Item::class.java)
            .where(Item.SERVER_ID.eq(itemid))
            .get().firstOrNull() ?: return getDefaultBadgeLayout()
    if (item.getBadge_layout_id() != null) {
        return application.data().select(BadgeLayout::class.java)
                .where(BadgeLayout.SERVER_ID.eq(item.getBadge_layout_id()))
                .get().firstOrNull() ?: getDefaultBadgeLayout()
    } else {
        return application.data().select(BadgeLayout::class.java)
                .where(BadgeLayout.IS_DEFAULT.eq(true))
                .get().firstOrNull() ?: getDefaultBadgeLayout()
    }
}

fun printBadge(application: PretixDeskMain, position: JSONObject) {
    val pdffile = File(application.cacheDir, "print.pdf")
    if (!pdffile.parentFile.exists()) {
        pdffile.parentFile.mkdirs();
    }
    val fs = DesktopFileStorage(File(application.dataDir))

    val layout = getBadgeLayout(application, position)
    if (layout.getBackground_filename() != null) {
        fs.getFile(layout.getBackground_filename()).inputStream().use {
            Renderer(layout.json.getJSONArray("layout"), position, it, application).writePDF(pdffile)
        }
    } else {
        Renderer(layout.json.getJSONArray("layout"), position, null, application).writePDF(pdffile)
    }
    val document = PDDocument.load(pdffile)
    val printServices = PrintServiceLookup.lookupPrintServices(null, null)
    for (printService in printServices) {
        if (printService.name.trim().equals(application.configStore.badgePrinterName)) {
            val job = PrinterJob.getPrinterJob()
            job.setPageable(PDFPageable(document, Orientation.AUTO, false, 0f))
            job.printService = printService
            val attributes = HashPrintRequestAttributeSet()
            attributes.add(OrientationRequested.PORTRAIT)
            job.print(attributes)
            break
        }
    }
}


class OrderPositionContentProvider(private val op: JSONObject) : ContentProvider {
    override fun getTextContent(content: String?, text: String?): String {
        if (content == "other") {
            return text ?: ""
        } else if (op.has("pdf_data") && op.getJSONObject("pdf_data").has(content)) {
            return op.getJSONObject("pdf_data").getString(content)
        } else {
            return "???"
        }
    }

    override fun getBarcodeContent(content: String?): String {
        return when(content) {
            "pseudonymization_id" -> op.getString("pseudonymization_id")
            "secret" -> op.getString("secret")
            else -> op.getString("secret")  // Backwards compatibility
        }
    }

}

class Renderer(private val layout: JSONArray, private val position: JSONObject, private val background: InputStream?, private val application: PretixDeskMain) {
    fun writePDF(outFile: File) {
        val posList = emptyList<ContentProvider>().toMutableList()
        posList.add(OrderPositionContentProvider(position))
        try {
            val l = Layout(
                    layout,
                    background,
                    posList.listIterator()
            )
            // Default to A6, like pretix
            l.defaultWidth = 5.8f * 72f
            l.defaultHeight = 4.1f * 72f
            l.render(outFile.absolutePath)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    companion object {
        fun registerFonts(application: PretixDeskMain) {
            registerFontFamily(application, "Open Sans", "fonts/OpenSans-%s.ttf")
            registerFontFamily(application, "Noto Sans", "fonts/NotoSans-%s-webfont.ttf")
            registerFontFamily(application, "Roboto", "fonts/Roboto-%s.ttf")
            registerFontFamily(application, "Droid Serif", "fonts/DroidSerif-%s-webfont.ttf")
            registerFontFamily(application, "Fira Sans", "fonts/firasans-%s-webfont.ttf")
            registerFontFamily(application, "Lato", "fonts/Lato-%s.ttf")
            registerFontFamily(application, "Vollkorn", "fonts/Vollkorn-%s.ttf")
        }

        fun storeFont(application: PretixDeskMain, path: String): String {
            val file = File(application.dataDir, path)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            PretixDeskMain::class.java.getResourceAsStream(path).use {
                val inputStream = it
                file.outputStream().use {
                    val buffer = ByteArray(1024) // Adjust if you want
                    var bytesRead: Int = 0
                    while (bytesRead != -1) {
                        it.write(buffer, 0, bytesRead)
                        bytesRead = inputStream.read(buffer)
                    }
                }
            }
            return file.absolutePath
        }

        fun registerFontFamily(application: PretixDeskMain, name: String, pattern: String) {
            FontRegistry.getInstance().add(
                    name,
                    FontSpecification.Style.REGULAR,
                    storeFont(application, String.format(pattern, "Regular")))
            FontRegistry.getInstance().add(
                    name,
                    FontSpecification.Style.BOLDITALIC,
                    storeFont(application, String.format(pattern, "BoldItalic")))
            FontRegistry.getInstance().add(
                    name,
                    FontSpecification.Style.BOLD,
                    storeFont(application, String.format(pattern, "Bold")))
            FontRegistry.getInstance().add(
                    name,
                    FontSpecification.Style.ITALIC,
                    storeFont(application, String.format(pattern, "Italic")))
        }
    }
}
