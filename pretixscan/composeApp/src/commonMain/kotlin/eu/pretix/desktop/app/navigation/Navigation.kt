package eu.pretix.desktop.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import eu.pretix.desktop.app.sync.SyncRoot
import eu.pretix.desktop.app.ui.ScreenRoot
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.migration.MigrationCoordinator
import eu.pretix.desktop.migration.MigrationErrorDialog
import eu.pretix.desktop.migration.MigrationProgressDialog
import eu.pretix.desktop.migration.MigrationResult
import eu.pretix.scan.main.presentation.MainScreen
import eu.pretix.scan.settings.presentation.SettingsScreen
import eu.pretix.scan.setup.SetupScreen
import eu.pretix.scan.status.presentation.StatusScreen
import eu.pretix.scan.welcome.WelcomeScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private sealed class MigrationState {
    object Checking : MigrationState()
    object InProgress : MigrationState()
    object Complete : MigrationState()
    object NotNeeded : MigrationState()
    data class Failed(val error: String, val canRetry: Boolean) : MigrationState()
}

@Composable
fun Navigation(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val migrationCoordinator = koinInject<MigrationCoordinator>()
    val scope = rememberCoroutineScope()
    var migrationState by remember { mutableStateOf<MigrationState>(MigrationState.Checking) }

    LaunchedEffect(Unit) {
        if (migrationCoordinator.needsMigration()) {
            migrationState = MigrationState.InProgress
            when (val result = migrationCoordinator.executeMigration()) {
                is MigrationResult.Success -> {
                    migrationState = MigrationState.Complete
                }

                is MigrationResult.Failure -> {
                    migrationState = MigrationState.Failed(
                        error = result.error,
                        canRetry = result.canRetry
                    )
                }
            }
        } else {
            migrationState = MigrationState.NotNeeded
        }
    }

    when (val state = migrationState) {
        is MigrationState.Checking, is MigrationState.InProgress -> {
            MigrationProgressDialog()
        }

        is MigrationState.Failed -> {
            MigrationErrorDialog(
                error = state.error,
                canRetry = state.canRetry,
                onRetry = {
                    scope.launch {
                        migrationState = MigrationState.InProgress
                        when (val result = migrationCoordinator.executeMigration()) {
                            is MigrationResult.Success -> {
                                migrationState = MigrationState.Complete
                            }

                            is MigrationResult.Failure -> {
                                migrationState = MigrationState.Failed(result.error, result.canRetry)
                            }
                        }
                    }
                },
                onDismiss = {
                    migrationState = MigrationState.NotNeeded
                }
            )
        }

        is MigrationState.Complete, is MigrationState.NotNeeded -> {
            val appConfig = koinInject<DataStoreConfigStore>()
            val startDestination: String = if (appConfig.isConfigured) Route.Main.route else Route.Welcome.route

            SyncRoot(navHostController) {
                NavHost(
                    navController = navHostController,
                    startDestination = startDestination,
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() },
                    popEnterTransition = { fadeIn() },
                    popExitTransition = { fadeOut() },
                    modifier = modifier.fillMaxSize(),
                ) {
                    composable(route = Route.Welcome.route) {
                        ScreenRoot {
                            WelcomeScreen(navHostController = navHostController)
                        }
                    }
                    composable(route = Route.Setup.route) {
                        ScreenRoot {
                            SetupScreen(navHostController = navHostController)
                        }
                    }
                    composable(route = Route.Main.route) {
                        ScreenRoot {
                            MainScreen(navHostController = navHostController)
                        }
                    }
                    composable(route = Route.Settings.route) {
                        ScreenRoot {
                            SettingsScreen(navHostController = navHostController)
                        }
                    }
                    composable(route = Route.EventStats.route) {
                        ScreenRoot {
                            StatusScreen(navHostController = navHostController)
                        }
                    }
                }
            }
        }
    }
}