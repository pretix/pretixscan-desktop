package eu.pretix.pretixscan.desktop

import eu.pretix.libpretixprint.templating.ContentProvider
import eu.pretix.libpretixprint.templating.FontRegistry
import eu.pretix.libpretixprint.templating.FontSpecification
import eu.pretix.libpretixsync.db.BadgeLayout
import eu.pretix.libpretixsync.db.Item
import eu.pretix.libpretixprint.templating.Layout
import eu.pretix.libpretixsync.db.BadgeLayoutItem
import eu.pretix.libpretixsync.db.CachedPdfImage
import io.requery.BlockingEntityStore
import io.requery.Persistable
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.Orientation
import org.apache.pdfbox.printing.PDFPageable
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.awt.print.PrinterJob
import java.io.File
import java.io.InputStream
import java.util.*
import javax.print.PrintServiceLookup
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.standard.OrientationRequested


fun getDefaultBadgeLayout(): BadgeLayout {
    val tl = BadgeLayout()
    tl.setJson_data("{\"layout\": [{\"type\":\"textarea\",\"left\":\"13.09\",\"bottom\":\"49.73\",\"fontsize\":\"23.6\",\"color\":[0,0,0,1],\"fontfamily\":\"Open Sans\",\"bold\":true,\"italic\":false,\"width\":\"121.83\",\"content\":\"attendee_name\",\"text\":\"Max Mustermann\",\"align\":\"center\"}]}")
    return tl
}

fun getBadgeLayout(application: PretixScanMain, position: JSONObject, eventSlug: String): BadgeLayout? {
    val itemid_server = position.getLong("item")
    val itemid_local = application.data().select(Item::class.java)
        .where(Item.SERVER_ID.eq(itemid_server))
        .get().firstOrNull().getId()

    val litem = application.data().select(BadgeLayoutItem::class.java)
            .where(BadgeLayoutItem.ITEM_ID.eq(itemid_local))
            .get().firstOrNull()
    if (litem != null) {
        if (litem.getLayout() == null) { // "Do not print badges" is configured for this product
            return null
        } else { // A non-default badge layout is set for this product
            return litem.getLayout()
        }
    }

    /* Legacy mechanism: Keep around until pretix 2.5 is end of life */
    val item = application.data().select(Item::class.java)
            .where(Item.SERVER_ID.eq(itemid_server))
            .get().firstOrNull() ?: return getDefaultBadgeLayout()
    if (item.getBadge_layout_id() != null) {
        return application.data().select(BadgeLayout::class.java)
                .where(BadgeLayout.SERVER_ID.eq(item.getBadge_layout_id()))
                .and(BadgeLayout.EVENT_SLUG.eq(eventSlug))
                .get().firstOrNull() ?: getDefaultBadgeLayout()
    } else { // Also used for current pretix versions for obtaining the event's default badge layout
        return application.data().select(BadgeLayout::class.java)
                .where(BadgeLayout.IS_DEFAULT.eq(true))
                .and(BadgeLayout.EVENT_SLUG.eq(eventSlug))
                .get().firstOrNull() ?: getDefaultBadgeLayout()
    }
}

fun printBadge(application: PretixScanMain, position: JSONObject, eventSlug: String) {
    val pdffile = File(PretixScanMain.cacheDir, "print.pdf")
    if (!pdffile.parentFile.exists()) {
        pdffile.parentFile.mkdirs();
    }
    val fs = DesktopFileStorage(File(PretixScanMain.dataDir))

    val layout = getBadgeLayout(application, position, eventSlug) ?: return
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
            var o = Orientation.PORTRAIT
            if (application.configStore.badgePrinterOrientation == "Landscape") {
                o = Orientation.LANDSCAPE
            } else if (application.configStore.badgePrinterOrientation == "Auto") {
                o = Orientation.AUTO
            }
            job.setPageable(PDFPageable(document, o, false, 0f))
            job.printService = printService
            val attributes = HashPrintRequestAttributeSet()
            if (application.configStore.badgePrinterOrientation == "Landscape") {
                attributes.add(OrientationRequested.LANDSCAPE)
            } else {
                attributes.add(OrientationRequested.PORTRAIT)
            }
            job.print(attributes)
            break
        }
    }
}


class OrderPositionContentProvider(private val application: PretixScanMain, private val op: JSONObject) : ContentProvider {
    fun i18nToString(str: JSONObject): String? {
        val lng = Locale.getDefault().language
        val lngparts = lng.split("[-_]".toRegex()).toTypedArray()
        try {
            if (str.has(lng) && str.getString(lng) != "") {
                return str.getString(lng)
            } else {
                val it: Iterator<*> = str.keys()
                while (it.hasNext()) {
                    val key = it.next() as String
                    val parts = key.split("[-_]".toRegex()).toTypedArray()
                    if (parts[0] == lngparts[0] && str.getString(key) != "") {
                        return str.getString(key)
                    }
                }
                if (str.has("en") && str.getString("en") != "") {
                    return str.getString("en")
                } else if (str.length() > 0) {
                    return str.getString(str.keys().next() as String)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    private fun interpolate(str: String): String {
        return str.replace(Regex("\\{([a-zA-Z0-9_:]+)\\}")) { match ->
            getTextContent(match.groups[1]!!.value, null, null)
        }
    }

    override fun getTextContent(content: String?, text: String?, textI18n: JSONObject?): String {
        if (content == "other") {
            return interpolate(text ?: "")
        } else if (content == "other_i18n") {
            return if (textI18n != null) interpolate(i18nToString(textI18n) ?: "") else ""
        } else if (op.has("pdf_data") && op.getJSONObject("pdf_data").has(content)) {
            return op.getJSONObject("pdf_data").getString(content)
        } else {
            return "???"
        }
    }

    override fun getImageContent(content: String?): InputStream? {
        val file = application.data().select(CachedPdfImage::class.java).where(CachedPdfImage.ORDERPOSITION_ID.eq(op.getLong("id"))).and(CachedPdfImage.KEY.eq(content)).get().firstOrNull()
                ?: return null

        return DesktopFileStorage(File(PretixScanMain.dataDir)).getFile("pdfimage_${file.getEtag()}.bin").inputStream()
    }

    override fun getBarcodeContent(content: String?, text: String?, textI18n: JSONObject?): String {
        return when (content) {
            "secret" -> op.getString("secret")  // the one in textcontent might be shortened
            "pseudonymization_id" -> op.getString("pseudonymization_id")  // required for backwards compatibility
            else -> getTextContent(content, text, textI18n)
        }
    }
}

class Renderer(private val layout: JSONArray, private val position: JSONObject, private val background: InputStream?, private val application: PretixScanMain) {
    fun writePDF(outFile: File) {
        val posList = emptyList<ContentProvider>().toMutableList()
        posList.add(OrderPositionContentProvider(application, position))
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
        fun registerFonts(application: PretixScanMain) {
            registerFontFamily(application, "Open Sans", "fonts/OpenSans-%s.ttf")
            registerFontFamily(application, "Noto Sans", "fonts/NotoSans-%s-webfont.ttf")
            registerFontFamily(application, "Roboto", "fonts/Roboto-%s.ttf")
            registerFontFamily(application, "Droid Serif", "fonts/DroidSerif-%s-webfont.ttf")
            registerFontFamily(application, "Fira Sans", "fonts/firasans-%s-webfont.ttf")
            registerFontFamily(application, "Lato", "fonts/Lato-%s.ttf")
            registerFontFamily(application, "Vollkorn", "fonts/Vollkorn-%s.ttf")
            registerFontFamily(application, "Montserrat", "fonts/montserrat-%s-webfont.ttf")
            registerFontFamily(application, "Webfont", "fonts/oswald-%s-webfont.ttf")
            registerFontFamily(application, "Roboto Condensed", "fonts/RobotoCondensed-%s-webfont.ttf")
            registerFontFamily(application, "Titillium", "fonts/titillium-%s-webfont.ttf")
            registerFontFamily(application, "Titillium Upright", "fonts/titillium-%s-webfont.ttf", "RegularUpright", "BoldUpright", "BoldUpright", "RegularUpright")
            registerFontFamily(application, "Titillium Semibold Upright", "fonts/titillium-%s-webfont.ttf", "SemiboldUpright", "BoldUpright", "BoldUpright", "SemiboldUpright")
            registerFontFamily(application, "DejaVu Sans", "fonts/DejaVuSans-%s-webfont.ttf")
            registerFontFamily(application, "Poppins", "fonts/Poppins-%s-webfont.ttf")
            registerFontFamily(application, "Space Mono", "fonts/Space-Mono-%s.ttf")

        }

        fun storeFont(application: PretixScanMain, path: String): String {
            val file = File(PretixScanMain.dataDir, path)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            PretixScanMain::class.java.getResourceAsStream(path).use {
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

        fun registerFontFamily(application: PretixScanMain, name: String, pattern: String, regularName: String = "Regular", boldName: String = "Bold", boldItalicName: String = "BoldItalic", italicName: String = "Italic") {
            FontRegistry.getInstance().add(
                    name,
                    FontSpecification.Style.REGULAR,
                    storeFont(application, String.format(pattern, regularName)))
            FontRegistry.getInstance().add(
                    name,
                    FontSpecification.Style.BOLDITALIC,
                    storeFont(application, String.format(pattern, boldItalicName)))
            FontRegistry.getInstance().add(
                    name,
                    FontSpecification.Style.BOLD,
                    storeFont(application, String.format(pattern, boldName)))
            FontRegistry.getInstance().add(
                    name,
                    FontSpecification.Style.ITALIC,
                    storeFont(application, String.format(pattern, italicName)))
        }
    }
}
