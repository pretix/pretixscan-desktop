package app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import app.navigation.Navigation
import org.koin.compose.KoinContext


@Composable
fun Root(modifier: Modifier = Modifier) {
    KoinContext {
        val navHostController = rememberNavController()
        MaterialTheme {
            Navigation(
                navHostController = navHostController,
            )
        }
    }
}