package eu.pretix.desktop.cache

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import eu.pretix.libpretixsync.sqldelight.AndroidUtilDateAdapter
import eu.pretix.libpretixsync.sqldelight.BigDecimalAdapter
import eu.pretix.libpretixsync.sqldelight.SyncDatabase
import java.nio.file.Path
import java.util.Date
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReusableMediumExpiryRepairTest {

    private lateinit var testTempDir: Path
    private lateinit var driver: SqlDriver

    @BeforeTest
    fun setup() {
        testTempDir = createTempDirectory("pretixscan-db-test-")
        driver = JdbcSqliteDriver("jdbc:sqlite:" + testTempDir.resolve("sync.db").toAbsolutePath())
        SyncDatabase.Schema.create(driver)
    }

    @OptIn(ExperimentalPathApi::class)
    @AfterTest
    fun teardown() {
        driver.close()
        runCatching { testTempDir.deleteRecursively() }
    }

    private fun insertMedium(serverId: Long, expires: String) {
        driver.execute(
            identifier = null,
            sql = "INSERT INTO ReusableMedium(active, expires, server_id) VALUES (1, ?, ?);",
            parameters = 2,
        ) {
            bindString(0, expires)
            bindLong(1, serverId)
        }
    }

    @Test
    fun `upgrading a schema 116 database clears empty expiry dates and keeps real ones`() {
        val expiry = Date(1_767_225_600_000L)
        insertMedium(serverId = 1L, expires = "")
        insertMedium(serverId = 2L, expires = AndroidUtilDateAdapter().encode(expiry))

        SyncDatabase.Schema.migrate(driver, 116L, 117L, clearEmptyReusableMediumExpiryCallback)

        val db = createSyncDatabase(driver, AndroidUtilDateAdapter(), BigDecimalAdapter())
        assertNull(db.reusableMediumQueries.selectByServerId(1L).executeAsOne().expires)
        assertEquals(expiry, db.reusableMediumQueries.selectByServerId(2L).executeAsOne().expires)
    }
}
