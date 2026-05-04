package eu.pretix.desktop.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import eu.pretix.desktop.migration.ConfigMigration
import eu.pretix.pretixscan.desktop.AppConfig
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.nio.file.Path

class ConfigMigrationTest {

    private lateinit var testTempDir: Path
    private val testFiles = mutableListOf<Path>()

    @BeforeTest
    fun setup() {
        testTempDir = createTempDirectory("pretixscan-test-")
    }

    @AfterTest
    fun teardown() {
        testFiles.forEach { file ->
            runCatching { file.deleteIfExists() }
        }
        runCatching { testTempDir.deleteIfExists() }
        testFiles.clear()
    }

    private fun createTestDataStore(): DataStore<Preferences> {
        val testFile = testTempDir.resolve("test_${System.nanoTime()}.preferences_pb")
        testFiles.add(testFile)
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { testFile.toString().toPath() }
        )
    }

    private fun createTestAppConfig(): AppConfig {
        val tempDir = createTempDirectory("test_prefs").toString()
        return AppConfig(tempDir)
    }


    @Test
    fun test_full_migration_copies_all_properties() = runTest {
        val oldConfig = createTestAppConfig()
        val newConfig = DataStoreConfig(createTestDataStore())

        oldConfig.setDeviceConfig(
            url = "https://pretix.eu",
            key = "sk_test_key",
            orga_slug = "demo-org",
            device_id = 100L,
            serial = "SN-12345",
            sent_version = 3
        )
        oldConfig.offlineMode = true
        oldConfig.playSound = false
        oldConfig.preferredCameraName = "Logitech C920"
        oldConfig.eventSelections = listOf(
            EventSelection("test-event", "Test Event", 5L, 1L, "Main", null, null)
        )
        oldConfig.activeEventIndex = 0
        oldConfig.setKnownLiveEventSlugs(setOf("event-1", "event-2"))
        oldConfig.uiHideNames = false
        oldConfig.uiReduceMotion = true
        oldConfig.autoPrintBadges = true
        oldConfig.scanType = "exit"
        oldConfig.badgePrinterName = "Canon Pixma"
        oldConfig.badgePrinterOrientation = "Landscape"
        oldConfig.proxyMode = true

        val migration = ConfigMigration(oldConfig, newConfig)
        migration.migrateSettings()

        assertEquals("https://pretix.eu", newConfig.getApiUrl())
        assertEquals("sk_test_key", newConfig.getApiKey())
        assertEquals("demo-org", newConfig.getOrganizerSlug())
        assertEquals(100L, newConfig.getApiDeviceId())
        assertEquals("SN-12345", newConfig.getDeviceSerial())
        assertEquals(3, newConfig.getDeviceKnownVersion())
        assertEquals(1, newConfig.getEventSelections().size)
        assertEquals("test-event", newConfig.getEventSelections()[0].eventSlug)
        assertEquals(0, newConfig.getActiveEventIndex())
        assertEquals(setOf("event-1", "event-2"), newConfig.getKnownLiveEventSlugs())
        assertEquals(true, newConfig.getOfflineMode())
        assertEquals(true, newConfig.getProxyMode())
        assertEquals(false, newConfig.getPlaySound())
        assertEquals(false, newConfig.getUiHideNames())
        assertEquals(true, newConfig.getUiReduceMotion())
        assertEquals(true, newConfig.getAutoPrintBadges())
        assertEquals("Logitech C920", newConfig.getPreferredCameraName())
        assertEquals("exit", newConfig.getScanType())
        assertEquals("Canon Pixma", newConfig.getBadgePrinterName())
        assertEquals("Landscape", newConfig.getBadgePrinterOrientation())
        assertTrue(newConfig.isMigrationComplete())
    }

    @Test
    fun test_migration_is_idempotent() = runTest {
        val oldConfig = createTestAppConfig()
        val newConfig = DataStoreConfig(createTestDataStore())

        oldConfig.setDeviceConfig("https://old.com", "key1", "org1", 1L, "serial1", 1)

        val migration = ConfigMigration(oldConfig, newConfig)

        migration.migrateSettings()
        assertEquals("https://old.com", newConfig.getApiUrl())

        oldConfig.setDeviceConfig("https://new.com", "key2", "org2", 2L, "serial2", 2)

        migration.migrateSettings()
        assertEquals("https://old.com", newConfig.getApiUrl())
    }

    @Test
    fun test_can_migrate_before_and_after() = runTest {
        val oldConfig = createTestAppConfig()
        val newConfig = DataStoreConfig(createTestDataStore())

        oldConfig.setDeviceConfig("https://test.com", "key", "org", 1L, "serial", 1)

        val migration = ConfigMigration(oldConfig, newConfig)

        assertTrue(migration.canMigrate())

        migration.migrateSettings()

        assertFalse(migration.canMigrate())
    }

    @Test
    fun test_migration_sets_complete_flag() = runTest {
        val oldConfig = createTestAppConfig()
        val newConfig = DataStoreConfig(createTestDataStore())

        oldConfig.setDeviceConfig("https://test.com", "key", "org", 1L, "serial", 1)

        val migration = ConfigMigration(oldConfig, newConfig)

        assertFalse(newConfig.isMigrationComplete())

        migration.migrateSettings()

        assertTrue(newConfig.isMigrationComplete())
    }
}
