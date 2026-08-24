package eu.pretix.desktop.migration.di

import eu.pretix.desktop.migration.MigrationCoordinator
import eu.pretix.desktop.migration.TokenRoller
import eu.pretix.libpretixsync.api.HttpClientFactory
import org.koin.dsl.module

val migrationModule = module {
    single<TokenRoller> {
        TokenRoller(
            dataStoreConfig = get(),
            httpFactory = get<HttpClientFactory>()
        )
    }

    single<MigrationCoordinator> {
        MigrationCoordinator(
            configMigration = get(),
            tokenRoller = get()
        )
    }
}
