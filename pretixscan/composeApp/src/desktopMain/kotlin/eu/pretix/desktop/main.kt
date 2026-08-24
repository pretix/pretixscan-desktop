package eu.pretix.desktop

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import eu.pretix.desktop.app.Root
import eu.pretix.desktop.cache.getLogDirectory
import eu.pretix.initModules
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.startKoin
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.app_name
import java.awt.Dimension
import java.io.ByteArrayInputStream
import java.io.File
import java.util.logging.LogManager

private fun initializeLogging() {
    try {
        // Get platform-specific log directory using AppDirs
        val logDir = File(getLogDirectory())

        // Create log directory if it doesn't exist
        val actualLogDir = if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                System.err.println("Failed to create log directory: ${logDir.absolutePath}")
                // Fallback to temp directory
                val tempLogDir = File(System.getProperty("java.io.tmpdir"), "pretixSCAN/logs")
                tempLogDir.mkdirs()
                System.err.println("Using fallback log directory: ${tempLogDir.absolutePath}")
                tempLogDir
            } else {
                logDir
            }
        } else {
            logDir
        }

        // Load logging.properties and substitute variables
        val configStream = object {}.javaClass.classLoader
            .getResourceAsStream("logging.properties")
        if (configStream != null) {
            // Read the properties file as text
            val propertiesText = configStream.bufferedReader().use { it.readText() }

            // Substitute ${pretixscan.log.dir} with actual path
            // Use forward slashes which work on all platforms including Windows
            val logDirPath = actualLogDir.absolutePath.replace("\\", "/")
            val substitutedText = propertiesText.replace("\${pretixscan.log.dir}", logDirPath)

            // Load the modified properties into LogManager
            ByteArrayInputStream(substitutedText.toByteArray()).use { modifiedStream ->
                LogManager.getLogManager().readConfiguration(modifiedStream)
            }

            println("Logging initialized. Log directory: $logDirPath")
        } else {
            System.err.println("Warning: logging.properties not found on classpath")
        }
    } catch (e: Exception) {
        System.err.println("Failed to initialize logging configuration: ${e.message}")
        e.printStackTrace()
    }
}

fun main() = application {
    initializeLogging()
    installSystemStreamLoggers()
    installUncaughtExceptionLogger()

    startKoin {
        initModules()
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
        state = WindowState(placement = WindowPlacement.Maximized),
        icon = painterResource("pretix_app_icon.png")
    ) {
        window.minimumSize = Dimension(1024, 768)
        Root()
    }
}