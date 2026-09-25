package eu.pretix.desktop.migration

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import eu.pretix.desktop.cache.DataStoreConfig
import eu.pretix.libpretixsync.api.HttpClientFactory
import eu.pretix.pretixscan.desktop.AppConfig
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Path.Companion.toPath
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.ExperimentalPathApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs

class MigrationCoordinatorTest {

    private lateinit var testTempDir: Path

    @BeforeTest
    fun setup() {
        testTempDir = createTempDirectory("pretixscan-migration-test-")
    }

    @OptIn(ExperimentalPathApi::class)
    @AfterTest
    fun teardown() {
        runCatching { testTempDir.deleteRecursively() }
    }

    private val unauthorizedHttpFactory = HttpClientFactory {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(401)
                    .message("Unauthorized")
                    .body("{\"detail\":\"Invalid token.\"}".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
    }

    @Test
    fun test_rejected_token_discards_migrated_configuration() = runTest {
        val oldConfig = AppConfig(testTempDir.resolve("prefs").toString())
        oldConfig.setDeviceConfig(
            url = "https://pretix.eu",
            key = "sk_invalid_key",
            orga_slug = "demo-org",
            device_id = 100L,
            serial = "SN-12345",
            sent_version = 3
        )
        val dataStoreConfig = DataStoreConfig(
            PreferenceDataStoreFactory.createWithPath(
                produceFile = { testTempDir.resolve("config.preferences_pb").toString().toPath() }
            )
        )
        val configMigration = ConfigMigration(oldConfig, dataStoreConfig)
        val coordinator = MigrationCoordinator(
            configMigration,
            TokenRoller(dataStoreConfig, unauthorizedHttpFactory),
            cleanupV1Storage = { CleanupResult.Success }
        )

        assertIs<MigrationResult.TokenRejected>(coordinator.executeMigration())
        assertFalse(dataStoreConfig.isConfigured())
        assertFalse(dataStoreConfig.isMigrationComplete())
        assertFalse(configMigration.canMigrate())
    }
}
