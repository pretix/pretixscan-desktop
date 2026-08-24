package eu.pretix.desktop.printing

import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DesktopFileStorage
import eu.pretix.libpretixprint.templating.ContentProvider
import eu.pretix.libpretixprint.templating.Layout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.InputStream


class Renderer(
    private val appCache: AppCache,
    private val fileStorage: DesktopFileStorage
) {
    fun writePDF(layout: JSONArray, position: JSONObject, background: InputStream?, outFile: File) {
        val posList = emptyList<ContentProvider>().toMutableList()
        posList.add(OrderPositionContentProvider(appCache, fileStorage, position))
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
}