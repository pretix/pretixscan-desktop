package eu.pretix.pretixdesk

import eu.pretix.libpretixsync.DummySentryImplementation
import eu.pretix.libpretixsync.api.DefaultHttpClientFactory
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
import javafx.scene.image.Image
import javafx.stage.Stage
import net.harawata.appdirs.AppDirsFactory
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import tornadofx.App


class PretixDeskMain : App(MainView::class, MainStyleSheet::class) {
    val configStore = PretixDeskConfig()
    var dataStore: BlockingEntityStore<Persistable>? = null
    val appDirs = AppDirsFactory.getInstance();
    val dataDir = appDirs.getUserDataDir("pretixdesk", "1.0.0", "pretix")

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
    }


    fun data(): BlockingEntityStore<Persistable> {
        if (dataStore == null) {
            val dataSource = SQLiteDataSource()
            dataSource.setUrl("jdbc:sqlite:" + dataDir + "/data.sqlite");
            val config = SQLiteConfig()
            config.setDateClass("TEXT");
            dataSource.setConfig(config);
            dataSource.setEnforceForeignKeys(true);

            val model = Models.DEFAULT
            val configuration = ConfigurationBuilder(dataSource, model)
                    .useDefaultLogging()
                    .setEntityCache(EntityCacheBuilder(model)
                            .useReferenceCache(false)
                            .useSerializableCache(false)
                            .build())
                    .build()

            dataStore = EntityDataStore<Persistable>(configuration);
        }
        return dataStore!!
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
