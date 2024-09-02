package eu.pretix.desktop.cache

import eu.pretix.libpretixsync.Models
import eu.pretix.libpretixsync.db.Migrations
import io.requery.Persistable
import io.requery.cache.EntityCacheBuilder
import io.requery.sql.ConfigurationBuilder
import io.requery.sql.EntityDataStore
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.util.logging.Logger

class JvmLocalCacheFactory : LocalCacheFactory {
    val log = Logger.getLogger("JvmLocalCacheFactory")

    override fun createDataSource(): EntityDataStore<Persistable> {
        val dbFile = getDatabasePath()
        val dbIsNew = !dbFile.exists()
        val dataSource = SQLiteDataSource()
        log.info("Using database at $dbFile")
        dataSource.url = "jdbc:sqlite:" + dbFile.absolutePath

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
}