package eu.pretix.pretixdesk

import eu.pretix.libpretixsync.DummySentryImplementation
import eu.pretix.libpretixsync.api.DefaultHttpClientFactory
import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.check.AsyncCheckProvider
import eu.pretix.libpretixsync.check.OnlineCheckProvider
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.Models
import eu.pretix.pretixdesk.ui.MainView
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import io.requery.BlockingEntityStore
import io.requery.Persistable
import io.requery.cache.EntityCacheBuilder
import io.requery.sql.ConfigurationBuilder
import io.requery.sql.EntityDataStore
import io.requery.sql.SchemaModifier
import io.requery.sql.TableCreationMode
import javafx.scene.image.Image
import javafx.stage.Stage
import net.harawata.appdirs.AppDirsFactory
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import tornadofx.App
import java.io.File


class PretixDeskMain : App(MainView::class, MainStyleSheet::class) {
    val configStore = PretixDeskConfig()
    private var dataStore: BlockingEntityStore<Persistable>? = null
    private val appDirs = AppDirsFactory.getInstance()!!
    private val dataDir = appDirs.getUserDataDir("pretixdesk", "1.0.0", "pretix")
    private var apiClient: PretixApi? = null

    override fun start(stage: Stage) {
        stage.icons += Image(PretixDeskMain::class.java.getResourceAsStream("icon.png"))
        stage.isMaximized = true
        stage.minHeight = 600.0
        stage.minWidth = 800.0

        configStore.setEventConfig(
                "foo",
                "bar",
                2,
                true,
                true
        )

        super.start(stage)

        val stylesheets = stage.scene.getStylesheets()
        stylesheets.addAll(PretixDeskMain::class.java.getResource("/css/jfoenix-fonts.css").toExternalForm(),
                PretixDeskMain::class.java.getResource("/css/jfoenix-design.css").toExternalForm())
    }


    fun data(): BlockingEntityStore<Persistable> {
        if (dataStore == null) {
            File(dataDir).mkdirs()
            val dbFile = File(dataDir + "/data.sqlite")
            val dbIsNew = !dbFile.exists()

            val dataSource = SQLiteDataSource()
            dataSource.url = "jdbc:sqlite:" + dbFile.absolutePath
            val config = SQLiteConfig()
            config.setDateClass("TEXT")
            dataSource.config = config
            dataSource.setEnforceForeignKeys(true)
            val model = Models.DEFAULT

            if (dbIsNew) {
                // TODO: Database upgrades/migrations
                SchemaModifier(dataSource, model).createTables(TableCreationMode.CREATE_NOT_EXISTS)
            }

            val configuration = ConfigurationBuilder(dataSource, model)
                    .useDefaultLogging()
                    .setEntityCache(EntityCacheBuilder(model)
                            .useReferenceCache(false)
                            .useSerializableCache(false)
                            .build())
                    .build()

            dataStore = EntityDataStore<Persistable>(configuration)
        }
        return dataStore!!
    }

    fun api(): PretixApi {
        if (apiClient == null) {
            apiClient = PretixApi.fromConfig(configStore, DefaultHttpClientFactory());
        }
        return apiClient!!
    }

    fun newCheckProvider(): TicketCheckProvider {
        val p: TicketCheckProvider
        if (configStore.getAsyncModeEnabled()) {
            p = AsyncCheckProvider(configStore, data(), DefaultHttpClientFactory())
        } else {
            p = OnlineCheckProvider(configStore, DefaultHttpClientFactory())
        }
        p.setSentry(DummySentryImplementation())
        return p
    }
}
