package eu.pretix.desktop.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import eu.pretix.desktop.app.navigation.Navigation
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.asColor
import org.koin.compose.KoinContext


@Composable
fun Root() {
    KoinContext {
        val navHostController = rememberNavController()
        val colorScheme = lightColorScheme(primary = CustomColor.BrandDark.asColor())

        MaterialTheme(colorScheme = colorScheme) {
            Navigation(
                navHostController = navHostController,
            )
        }
    }
}