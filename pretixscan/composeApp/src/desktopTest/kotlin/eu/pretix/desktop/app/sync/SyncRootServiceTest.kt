package eu.pretix.desktop.app.sync

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import eu.pretix.desktop.cache.DataStoreConfig
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.cache.EventSelection
import eu.pretix.libpretixsync.sync.SyncException
import eu.pretix.libpretixsync.sync.SyncManager
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okio.Path.Companion.toPath
import org.json.JSONObject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SyncRootServiceTest {

    private lateinit var testTempDir: Path
    private lateinit var config: DataStoreConfigStore

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        testTempDir = createTempDirectory("pretixscan-sync-test-")
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { testTempDir.resolve("config.preferences_pb").toString().toPath() }
        )
        config = DataStoreConfigStore(DataStoreConfig(dataStore))
        config.setDeviceConfig("https://pretix.example.com", "device-token", "demo-org", 1L, "SERIAL", 1)
        config.eventSelections = listOf(
            EventSelection(
                eventSlug = "test-event",
                eventName = "Test Event",
                subEventId = null,
                checkInListId = 1L,
                checkInListName = "Main List",
                dateFrom = null,
                dateTo = null
            )
        )
    }

    @OptIn(ExperimentalPathApi::class)
    @AfterTest
    fun teardown() {
        stopKoin()
        Dispatchers.resetMain()
        runCatching { testTempDir.deleteRecursively() }
    }

    private fun createService(pass: SyncManager.() -> SyncManager.SyncResult): SyncRootService {
        val syncManager = object : SyncManager(
            config, null, null, null, null, 1000L, 120000L, SyncManager.Profile.PRETIXSCAN, false, 1, JSONObject(),
            "", "", "", "", "", "", null, null, null
        ) {
            override fun sync(force: Boolean, feedback: ProgressFeedback?): SyncResult = pass()
        }
        startKoin { modules(module { factory<SyncManager> { syncManager } }) }
        return SyncRootService(config, mockk(relaxed = true))
    }

    private fun SyncRootService.runPass(): SyncState = runBlocking {
        forceSync(nowMillis = PASS_START)
        val state = withTimeout(5.seconds) {
            syncState.first { it is SyncState.Success || it is SyncState.Error }
        }
        runWithSyncStopped {}
        state
    }

    @Test
    fun test_completed_pass_leaves_sync_timestamps_to_sync_manager() {
        val service = createService {
            config.lastSync = SYNC_MANAGER_TIME
            SyncResult(true, false, null)
        }

        val state = service.runPass()

        assertEquals(SyncState.Success(lastSync = PASS_START), state)
        assertEquals(SYNC_MANAGER_TIME, config.lastSync)
        assertEquals(0L, config.lastDownload)
        assertEquals(0L, config.lastFailedSync)
    }

    @Test
    fun test_failure_recorded_by_sync_manager_keeps_its_timestamp() {
        val service = createService {
            config.lastFailedSync = SYNC_MANAGER_TIME
            config.lastFailedSyncMsg = "Unauthorized"
            SyncResult(true, true, SyncException("Unauthorized"))
        }

        val state = service.runPass()

        assertEquals(SyncState.Error("Unauthorized"), state)
        assertEquals(SYNC_MANAGER_TIME, config.lastFailedSync)
        assertEquals("Unauthorized", config.lastFailedSyncMsg)
    }

    @Test
    fun test_failure_outside_sync_manager_is_recorded_at_failure_time() {
        val service = createService { throw IllegalStateException("database is locked") }
        val beforePass = System.currentTimeMillis()

        val state = service.runPass()

        assertEquals(SyncState.Error("database is locked"), state)
        assertTrue(config.lastFailedSync >= beforePass)
        assertEquals("database is locked", config.lastFailedSyncMsg)
    }

    private companion object {
        const val PASS_START = 1234L
        const val SYNC_MANAGER_TIME = 5678L
    }
}
