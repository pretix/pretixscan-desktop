package eu.pretix.scan.main.presentation.selectevent

import eu.pretix.libpretixsync.setup.RemoteEvent
import eu.pretix.scan.main.utils.formatEventDateTime
import eu.pretix.scan.main.utils.formatEventDateTimeRange
import eu.pretix.scan.main.utils.isMidnight
import org.joda.time.DateTime
import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EventSelectionTest {

    private fun createRemoteEvent(
        slug: String,
        subeventId: Long? = null,
        dateFrom: DateTime = DateTime.now(),
        dateTo: DateTime? = null
    ) = RemoteEvent(
        name_i18n = JSONObject().put("en", "Test Event"),
        slug = slug,
        date_from = dateFrom,
        date_to = dateTo,
        subevent_id = subeventId,
        best_availability_state = null,
        live = true
    )

    @Test
    fun `isMidnight should return true for midnight time`() {
        val midnight = DateTime(2024, 1, 15, 0, 0, 0, 0)
        assertTrue(midnight.isMidnight())
    }

    @Test
    fun `isMidnight should return false for non-midnight time`() {
        val nonMidnight = DateTime(2024, 1, 15, 14, 30, 0, 0)
        assertFalse(nonMidnight.isMidnight())
    }

    @Test
    fun `isMidnight should return false for time with seconds`() {
        val withSeconds = DateTime(2024, 1, 15, 0, 0, 30, 0)
        assertFalse(withSeconds.isMidnight())
    }

    @Test
    fun `isMidnight should return false for time with milliseconds`() {
        val withMillis = DateTime(2024, 1, 15, 0, 0, 0, 500)
        assertFalse(withMillis.isMidnight())
    }

    @Test
    fun `formatEventDateTime should not show time for midnight`() {
        val midnight = DateTime(2024, 1, 15, 0, 0, 0, 0)
        val formatted = formatEventDateTime(midnight)
        assertFalse(formatted.contains(":"), "Midnight times should not show time portion")
    }

    @Test
    fun `formatEventDateTime should show time for non-midnight`() {
        val afternoon = DateTime(2024, 1, 15, 14, 30, 0, 0)
        val formatted = formatEventDateTime(afternoon)
        assertTrue(formatted.contains(":"), "Non-midnight times should show time portion")
    }

    @Test
    fun `formatEventDateTime should return empty string for null`() {
        assertEquals("", formatEventDateTime(null))
    }

    @Test
    fun `formatEventDateTimeRange should format single date`() {
        val date = DateTime(2024, 1, 15, 0, 0, 0, 0)
        val formatted = formatEventDateTimeRange(date, null)
        assertFalse(formatted.contains("-"), "Single date should not contain dash")
        assertTrue(formatted.isNotEmpty())
    }

    @Test
    fun `formatEventDateTimeRange should format date range`() {
        val from = DateTime(2024, 1, 15, 0, 0, 0, 0)
        val to = DateTime(2024, 1, 17, 0, 0, 0, 0)
        val formatted = formatEventDateTimeRange(from, to)
        assertTrue(formatted.contains("-"), "Date range should contain dash")
    }

    @Test
    fun `formatEventDateTimeRange should return empty string when from is null`() {
        val to = DateTime(2024, 1, 17, 0, 0, 0, 0)
        assertEquals("", formatEventDateTimeRange(null, to))
    }

    @Test
    fun `formatEventDateTimeRange should handle datetime range with times`() {
        val from = DateTime(2024, 1, 15, 14, 30, 0, 0)
        val to = DateTime(2024, 1, 15, 18, 0, 0, 0)
        val formatted = formatEventDateTimeRange(from, to)
        assertTrue(formatted.contains("-"), "DateTime range should contain dash")
        assertTrue(formatted.contains(":"), "DateTime range should show times")
    }

    @Test
    fun `isEventSelected should match by slug in advanced mode`() {
        val event = createRemoteEvent("test-event", subeventId = 123L)
        val selectedSlugs = setOf("test-event")

        assertTrue(
            isEventSelected(event, null, selectedSlugs, advancedMode = true)
        )
    }

    @Test
    fun `isEventSelected should not match unselected slug in advanced mode`() {
        val event = createRemoteEvent("test-event", subeventId = 123L)
        val selectedSlugs = setOf("other-event")

        assertFalse(
            isEventSelected(event, null, selectedSlugs, advancedMode = true)
        )
    }

    @Test
    fun `isEventSelected should match by slug and subevent_id in normal mode`() {
        val event = createRemoteEvent("test-event", subeventId = 123L)
        val selectedEvent = createRemoteEvent("test-event", subeventId = 123L)

        assertTrue(
            isEventSelected(event, selectedEvent, emptySet(), advancedMode = false)
        )
    }

    @Test
    fun `isEventSelected should not match different subevent_id in normal mode`() {
        val event = createRemoteEvent("test-event", subeventId = 123L)
        val selectedEvent = createRemoteEvent("test-event", subeventId = 456L)

        assertFalse(
            isEventSelected(event, selectedEvent, emptySet(), advancedMode = false)
        )
    }

    @Test
    fun `isEventSelected should not match different slug in normal mode`() {
        val event = createRemoteEvent("test-event", subeventId = 123L)
        val selectedEvent = createRemoteEvent("other-event", subeventId = 123L)

        assertFalse(
            isEventSelected(event, selectedEvent, emptySet(), advancedMode = false)
        )
    }

    @Test
    fun `isEventSelected should match when both subevent_ids are null in normal mode`() {
        val event = createRemoteEvent("test-event", subeventId = null)
        val selectedEvent = createRemoteEvent("test-event", subeventId = null)

        assertTrue(
            isEventSelected(event, selectedEvent, emptySet(), advancedMode = false)
        )
    }

    @Test
    fun `isEventSelected should not match when one subevent_id is null in normal mode`() {
        val event = createRemoteEvent("test-event", subeventId = 123L)
        val selectedEvent = createRemoteEvent("test-event", subeventId = null)

        assertFalse(
            isEventSelected(event, selectedEvent, emptySet(), advancedMode = false)
        )
    }

    @Test
    fun `isEventSelected should return false when selectedEvent is null in normal mode`() {
        val event = createRemoteEvent("test-event", subeventId = 123L)

        assertFalse(
            isEventSelected(event, null, emptySet(), advancedMode = false)
        )
    }
}
