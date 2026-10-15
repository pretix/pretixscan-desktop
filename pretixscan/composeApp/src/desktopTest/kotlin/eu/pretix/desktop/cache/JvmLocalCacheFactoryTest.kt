package eu.pretix.desktop.cache

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import eu.pretix.libpretixsync.sqldelight.SyncDatabase
import java.io.File
import java.time.OffsetDateTime
import java.util.Date
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JvmLocalCacheFactoryTest {

    private val directory = createTempDirectory("pretixscan-cache").toFile()
    private val url = "jdbc:sqlite:" + File(directory, "sync.sqlite").absolutePath

    @BeforeTest
    fun storeMedia() {
        JdbcSqliteDriver(url, schema = SyncDatabase.Schema).use { driver ->
            insertMedium(driver, serverId = BLANK_EXPIRY_SERVER_ID, expires = "")
            insertMedium(driver, serverId = DATED_SERVER_ID, expires = EXPIRY)
        }
    }

    @AfterTest
    fun removeDatabase() {
        directory.deleteRecursively()
    }

    private fun insertMedium(driver: JdbcSqliteDriver, serverId: Long, expires: String) {
        driver.execute(
            identifier = null,
            sql = "INSERT INTO ReusableMedium (active, server_id, identifier, type, expires) VALUES (1, ?, ?, 'barcode', ?);",
            parameters = 3,
        ) {
            bindLong(0, serverId)
            bindString(1, "medium-$serverId")
            bindString(2, expires)
        }
    }

    @Test
    fun `a medium stored without an expiry date is read back without one`() {
        val database = JvmLocalCacheFactory().openSyncDatabase(url)

        val medium = database.reusableMediumQueries.selectByServerId(BLANK_EXPIRY_SERVER_ID).executeAsOne()

        assertNull(medium.expires)
    }

    @Test
    fun `a medium keeps the expiry date it was stored with`() {
        val database = JvmLocalCacheFactory().openSyncDatabase(url)

        val medium = database.reusableMediumQueries.selectByServerId(DATED_SERVER_ID).executeAsOne()

        assertEquals(Date(OffsetDateTime.parse(EXPIRY).toInstant().toEpochMilli()), medium.expires)
    }

    private companion object {
        const val BLANK_EXPIRY_SERVER_ID = 1L
        const val DATED_SERVER_ID = 2L
        const val EXPIRY = "2026-01-02T03:04:05Z"
    }
}
