package navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import eu.pretix.desktop.cache.AppConfig
import org.koin.compose.koinInject
import screen.main.MainScreen
import screen.setup.SetupScreen
import screen.welcome.WelcomeScreen

@Composable
fun Navigation(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    // determine the starting screen based on whether the app is configured or not
    val appConfig = koinInject<AppConfig>()
    val startDestination: String = if (appConfig.isConfigured) Route.Main.route else Route.Welcome.route

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
            WelcomeScreen(navHostController = navHostController)
        }
        composable(route = Route.Setup.route) {
            SetupScreen(navHostController = navHostController)
        }
        composable(route = Route.Main.route) {
            MainScreen(navHostController = navHostController)
        }
    }
}