package eu.pretix.desktop.cache

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import eu.pretix.libpretixsync.sqldelight.AndroidUtilDateAdapter
import eu.pretix.libpretixsync.sqldelight.BigDecimalAdapter
import eu.pretix.libpretixsync.sqldelight.Migrations.clearResourceSyncStatusCallback
import eu.pretix.libpretixsync.sqldelight.Migrations.minVersionCallback
import eu.pretix.libpretixsync.sqldelight.SyncDatabase
import java.io.File
import java.util.*
import java.util.logging.Logger

class JvmLocalCacheFactory : LocalCacheFactory {
    val log = Logger.getLogger("JvmLocalCacheFactory")

    private fun jdbcConnectionString(dbFile: File): String {
        return "jdbc:sqlite:" + dbFile.absolutePath
    }

    override fun deleteDataSource() {
        val dbFile = getDatabasePath()
        if (dbFile.exists()) {
            log.info("Deleting database at $dbFile")
            dbFile.delete()
        }
    }

    private fun createDriver(url: String): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(
            url = url,
            properties = Properties(1).apply { put("foreign_keys", "true") },
            schema = SyncDatabase.Schema,
            callbacks = arrayOf(
                minVersionCallback,
                clearResourceSyncStatusCallback,
            ),
        )
        return driver
    }

    /**
    Replaces blank expiry dates of reusable media with NULL so that they can be decoded as dates.
     */
    private fun clearBlankMediumExpiryDates(driver: SqlDriver) {
        driver.execute(
            identifier = null,
            sql = "UPDATE ReusableMedium SET expires = NULL WHERE expires = '';",
            parameters = 0,
        )
    }

    internal fun openSyncDatabase(url: String): SyncDatabase {
        val driver = createDriver(url)
        log.info("Enabling wal-mode")
        driver.execute(
            identifier = null,
            sql = "PRAGMA journal_mode = wal;",
            parameters = 0,
        )
        log.info("Clearing blank expiry dates of reusable media")
        clearBlankMediumExpiryDates(driver)
        return createSyncDatabase(
            driver = driver,
            dateAdapter = AndroidUtilDateAdapter(),
            bigDecimalAdapter = BigDecimalAdapter(),
        )
    }

    override fun getSyncDataSource(): SyncDatabase {
        val dbFile = getDatabasePath()
        log.info("Using database file with sqldelight at $dbFile")
        return openSyncDatabase(jdbcConnectionString(dbFile))
    }
}