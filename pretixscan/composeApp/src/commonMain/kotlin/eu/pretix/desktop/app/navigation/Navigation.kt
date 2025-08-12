package eu.pretix.desktop.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import eu.pretix.desktop.app.sync.SyncRoot
import eu.pretix.desktop.app.ui.ScreenRoot
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.scan.main.presentation.MainScreen
import eu.pretix.scan.settings.presentation.SettingsScreen
import eu.pretix.scan.setup.SetupScreen
import eu.pretix.scan.status.presentation.StatusScreen
import eu.pretix.scan.welcome.WelcomeScreen
import org.koin.compose.koinInject

@Composable
fun Navigation(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    // determine the starting screen based on whether the app is configured or not
    val appConfig = koinInject<AppConfig>()
    val startDestination: String = if (appConfig.isConfigured) Route.Main.route else Route.Welcome.route

    SyncRoot(navHostController) {
        // setup navigation graph
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