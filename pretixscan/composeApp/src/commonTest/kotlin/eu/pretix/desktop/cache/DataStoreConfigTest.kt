package eu.pretix.desktop.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.joda.time.DateTime
import kotlin.test.*

class DataStoreConfigTest {

    private fun createTestDataStore(): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { "test_${System.nanoTime()}.preferences_pb".toPath() }
        )
    }

    @Test
    fun test_string_property_round_trip() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        config.setApiUrl("https://pretix.example.com/api/v1")
        val result = config.getApiUrl()

        assertEquals("https://pretix.example.com/api/v1", result)
    }

    @Test
    fun test_boolean_property_round_trip() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        config.setOfflineMode(true)
        val result = config.getOfflineMode()

        assertTrue(result)
    }

    @Test
    fun test_long_property_round_trip() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        val timestamp = 1704067200000L
        config.setLastSync(timestamp)
        val result = config.getLastSync()

        assertEquals(timestamp, result)
    }

    @Test
    fun test_int_property_round_trip() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        config.setActiveEventIndex(2)
        val result = config.getActiveEventIndex()

        assertEquals(2, result)
    }

    @Test
    fun test_nullable_string_with_null_value() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        config.setBadgePrinterName("HP LaserJet")
        config.setBadgePrinterName(null)
        val result = config.getBadgePrinterName()

        assertNull(result)
    }

    @Test
    fun test_nullable_string_with_non_null_value() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        config.setBadgePrinterName("HP LaserJet Pro")
        val result = config.getBadgePrinterName()

        assertEquals("HP LaserJet Pro", result)
    }

    @Test
    fun test_event_selection_list_serialization() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        val events = listOf(
            EventSelection(
                eventSlug = "summer-fest-2024",
                eventName = "Summer Festival 2024",
                subEventId = 42L,
                checkInListId = 1L,
                checkInListName = "Main Entrance",
                dateFrom = DateTime(2024, 6, 1, 0, 0),
                dateTo = DateTime(2024, 6, 3, 23, 59)
            ),
            EventSelection(
                eventSlug = "winter-conf",
                eventName = "Winter Conference",
                subEventId = null,
                checkInListId = 2L,
                checkInListName = "VIP Gate",
                dateFrom = null,
                dateTo = null
            )
        )

        config.setEventSelections(events)
        val result = config.getEventSelections()

        assertEquals(2, result.size)
        assertEquals("summer-fest-2024", result[0].eventSlug)
        assertEquals(42L, result[0].subEventId)
        assertEquals("Main Entrance", result[0].checkInListName)
        assertEquals("winter-conf", result[1].eventSlug)
        assertNull(result[1].subEventId)
        assertEquals("VIP Gate", result[1].checkInListName)
    }

    @Test
    fun test_set_string_serialization() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        val slugs = setOf("event-a", "event-b", "event-c")
        config.setKnownLiveEventSlugs(slugs)
        val result = config.getKnownLiveEventSlugs()

        assertEquals(3, result.size)
        assertTrue(result.containsAll(slugs))
    }

    @Test
    fun test_get_active_event_returns_correct_event() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        val events = listOf(
            EventSelection("event-1", "Event 1", null, 1L, "List 1", null, null),
            EventSelection("event-2", "Event 2", null, 2L, "List 2", null, null),
            EventSelection("event-3", "Event 3", null, 3L, "List 3", null, null)
        )
        config.setEventSelections(events)
        config.setActiveEventIndex(1)

        val result = config.getActiveEvent()

        assertEquals("event-2", result?.eventSlug)
        assertEquals(2L, result?.checkInListId)
    }

    @Test
    fun test_get_selected_checkin_list_for_event() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        val events = listOf(
            EventSelection("event-a", "Event A", null, 10L, "List A", null, null),
            EventSelection("event-b", "Event B", null, 20L, "List B", null, null)
        )
        config.setEventSelections(events)
        config.setActiveEventIndex(0)

        val activeListId = config.getSelectedCheckinListForEvent(null)
        assertEquals(10L, activeListId)

        val specificListId = config.getSelectedCheckinListForEvent("event-b")
        assertEquals(20L, specificListId)
    }

    @Test
    fun test_default_values() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        assertEquals("", config.getApiUrl())
        assertEquals("", config.getApiKey())
        assertEquals(false, config.getOfflineMode())
        assertEquals(true, config.getPlaySound())
        assertEquals(0L, config.getLastSync())
        assertNull(config.getBadgePrinterName())
        assertEquals(emptyList(), config.getEventSelections())
        assertEquals(emptySet(), config.getKnownLiveEventSlugs())
        assertFalse(config.isMigrationComplete())
    }

    @Test
    fun test_get_active_event_with_out_of_bounds_index() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        val events = listOf(
            EventSelection("event-1", "Event 1", null, 1L, "List 1", null, null)
        )
        config.setEventSelections(events)
        config.setActiveEventIndex(5)

        val result = config.getActiveEvent()

        assertNull(result)
    }

    @Test
    fun test_get_selected_checkin_list_for_non_existent_event() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        val events = listOf(
            EventSelection("event-a", "Event A", null, 10L, "List A", null, null)
        )
        config.setEventSelections(events)

        val result = config.getSelectedCheckinListForEvent("non-existent-event")

        assertEquals(0L, result)
    }

    @Test
    fun test_set_device_config_atomic_update() = runTest {
        val dataStore = createTestDataStore()
        val config = DataStoreConfig(dataStore)

        config.setLastSync(123456L)
        config.setLastDownload(789012L)

        config.setDeviceConfig(
            url = "https://pretix.example.com",
            key = "test-api-key",
            orgaSlug = "my-org",
            deviceId = 42L,
            serial = "DEVICE-001",
            sentVersion = 5
        )

        assertEquals("https://pretix.example.com", config.getApiUrl())
        assertEquals("test-api-key", config.getApiKey())
        assertEquals("my-org", config.getOrganizerSlug())
        assertEquals(42L, config.getApiDeviceId())
        assertEquals("DEVICE-001", config.getDeviceSerial())
        assertEquals(5, config.getDeviceKnownVersion())
        assertEquals(0L, config.getLastSync())
        assertEquals(0L, config.getLastDownload())
    }
}
