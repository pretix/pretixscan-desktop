package eu.pretix.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import app.Root
import di.initModules
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        initModules()
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "PretixScan",
        state = WindowState(placement = WindowPlacement.Maximized)
    ) {
        Root()
    }
}