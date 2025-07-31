package eu.pretix.desktop.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import eu.pretix.desktop.app.navigation.Navigation
import org.koin.compose.KoinContext


@Composable
fun Root() {
    KoinContext {
        val navHostController = rememberNavController()
        MaterialTheme {
            Navigation(
                navHostController = navHostController,
            )
        }
    }
}