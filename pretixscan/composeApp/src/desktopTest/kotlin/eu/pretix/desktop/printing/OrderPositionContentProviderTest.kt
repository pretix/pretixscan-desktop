package eu.pretix.desktop.printing

import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DesktopFileStorage
import eu.pretix.desktop.cache.JvmLocalCacheFactory
import org.json.JSONObject
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderPositionContentProviderTest {

    private val sut = OrderPositionContentProvider(
        AppCache(JvmLocalCacheFactory()),
        DesktopFileStorage(File(System.getProperty("java.io.tmpdir"))),
        JSONObject("""{"pdf_data":{"attendee_name":null,"seat":"A1"}}""")
    )

    @Test
    fun missing_attendee_value_renders_as_empty_text() {
        assertEquals("", sut.getTextContent("attendee_name", null, null))
    }

    @Test
    fun present_value_renders_as_is() {
        assertEquals("A1", sut.getTextContent("seat", null, null))
    }

    @Test
    fun unknown_key_renders_as_placeholder() {
        assertEquals("???", sut.getTextContent("unknown", null, null))
    }
}
