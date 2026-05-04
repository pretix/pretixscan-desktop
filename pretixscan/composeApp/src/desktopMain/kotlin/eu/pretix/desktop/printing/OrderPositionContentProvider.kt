package eu.pretix.desktop.printing

import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DesktopFileStorage
import eu.pretix.libpretixprint.templating.ContentProvider
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.util.*


class OrderPositionContentProvider(
    private val appCache: AppCache,
    private val fileStorage: DesktopFileStorage,
    private val op: JSONObject
) :
    ContentProvider {
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
        return if (content == "other") {
            interpolate(text ?: "")
        } else if (content == "other_i18n") {
            if (textI18n != null) interpolate(i18nToString(textI18n) ?: "") else ""
        } else if (op.has("pdf_data") && op.getJSONObject("pdf_data").has(content)) {
            op.getJSONObject("pdf_data").getString(content)
        } else {
            "???"
        }
    }

    override fun getImageContent(content: String?): InputStream? {
        val cachedPdfImage = appCache.db.cachedPdfImageQueries.selectForOrderPositionAndKey(op.getLong("id"), content)
            .executeAsOneOrNull()

        if (cachedPdfImage != null) {
            val fileName = "pdfimage_${cachedPdfImage.etag}.bin"
            val file = fileStorage.getFile(fileName)
            return file.inputStream()
        }

        return null
    }

    override fun getBarcodeContent(content: String?, text: String?, textI18n: JSONObject?): String {
        return when (content) {
            "secret" -> op.getString("secret")  // the one in textcontent might be shortened
            "pseudonymization_id" -> op.getString("pseudonymization_id")  // required for backwards compatibility
            else -> getTextContent(content, text, textI18n)
        }
    }
}