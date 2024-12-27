package eu.pretix.desktop.cache

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import eu.pretix.libpretixsync.Models
import eu.pretix.libpretixsync.db.Migrations
import eu.pretix.libpretixsync.sqldelight.AndroidUtilDateAdapter
import eu.pretix.libpretixsync.sqldelight.BigDecimalAdapter
import eu.pretix.libpretixsync.sqldelight.SyncDatabase
import io.requery.Persistable
import io.requery.cache.EntityCacheBuilder
import io.requery.sql.ConfigurationBuilder
import io.requery.sql.EntityDataStore
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.io.File
import java.util.logging.Logger

class JvmLocalCacheFactory : LocalCacheFactory {
    val log = Logger.getLogger("JvmLocalCacheFactory")

    private fun jdbcConnectionString(dbFile: File): String {
        return "jdbc:sqlite:" + dbFile.absolutePath
    }

    override fun createDataSource(): EntityDataStore<Persistable> {
        val dbFile = getDatabasePath()
        val dbIsNew = !dbFile.exists()
        val dataSource = SQLiteDataSource()
        log.info("Using database file with requery at $dbFile")
        dataSource.url = jdbcConnectionString(dbFile)

        val config = SQLiteConfig()
        config.setDateClass("TEXT")
        dataSource.config = config
        dataSource.setEnforceForeignKeys(true)
        val model = Models.DEFAULT

        Migrations.migrate(dataSource, dbIsNew)

        val configuration = ConfigurationBuilder(dataSource, model)
            // .useDefaultLogging()
            .setEntityCache(
                EntityCacheBuilder(model)
                    .useReferenceCache(false)
                    .useSerializableCache(false)
                    .build()
            )
            .build()

        val dataStore = EntityDataStore<Persistable>(configuration)
        return dataStore
    }

    private fun createDriver(url: String): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(
            url = url
        )
        return driver
    }

    override fun getSyncDataSource(): SyncDatabase {
        // NB: this implementation assumes the database schema has already been created by requery
        val dbFile = getDatabasePath()
        val url = jdbcConnectionString(dbFile)
        log.info("Using database file with sqldelight at $dbFile")
        val driver = createDriver(url)
        return createSyncDatabase(
            driver = createDriver(url),
            version = readVersionPragma(driver),
            dateAdapter = AndroidUtilDateAdapter(),
            bigDecimalAdapter = BigDecimalAdapter(),
        )
    }
}