package eu.pretix.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import di.initModules
import eu.pretix.desktop.app.Root
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.startKoin
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.app_name
import java.awt.Dimension

fun main() = application {
    startKoin {
        initModules()
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
        state = WindowState(placement = WindowPlacement.Maximized)
    ) {
        window.minimumSize = Dimension(1024, 768)
        Root()
    }
}