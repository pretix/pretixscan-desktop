package eu.pretix.pretixscan.desktop


import javafx.application.Platform.runLater
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import tornadofx.add
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.util.logging.Level
import java.util.logging.Logger


class ErrorHandler : Thread.UncaughtExceptionHandler {
    // startpoint was
    // https://github.com/edvin/tornadofx/blob/master/src/main/java/tornadofx/ErrorHandler.kt
    val log = Logger.getLogger("ErrorHandler")
    var lastErrorShown = 0L

    class ErrorEvent(val thread: Thread, val error: Throwable) {
        internal var consumed = false
        fun consume() {
            consumed = true
        }
    }

    companion object {
        // By default, all error messages are shown. Override to decide if certain errors should be handled another way.
        // Call consume to avoid error dialog.
        var filter: (ErrorEvent) -> Unit = { }
    }

    override fun uncaughtException(t: Thread, error: Throwable) {
        log.log(Level.SEVERE, "Uncaught error", error)

        if (isCycle(error)) {
            log.log(Level.INFO, "Detected cycle handling error, aborting.", error)
        } else {
            val event = ErrorEvent(t, error)
            filter(event)

            if (!event.consumed) {
                event.consume()
                runLater {
                    showErrorDialog(error)
                }
            }
        }

    }

    private fun isCycle(error: Throwable) = error.stackTrace.any {
        it.className.startsWith("${javaClass.name}\$uncaughtException$")
    }

    private fun showErrorDialog(error: Throwable) {
        if (System.currentTimeMillis() - lastErrorShown < 5 * 1000) {
            // Do not show an error dialog more often than every 5 seconds, otherwise users can
            // get spammed and their computer becomes unusable, if there is an error in a loop
            return
        }
        lastErrorShown = System.currentTimeMillis()
        val cause = Label(if (error.cause != null) error.cause?.message else "").apply {
            style = "-fx-font-weight: bold"
        }

        val textarea = TextArea().apply {
            prefRowCount = 20
            prefColumnCount = 50
            text = stringFromError(error)
        }

        Alert(ERROR).apply {
            title = error.message ?: "An error occured"
            isResizable = true
            headerText = if (error.stackTrace.isNullOrEmpty()) "Error" else "Error in " + error.stackTrace[0].toString()
            dialogPane.content = VBox().apply {
                add(cause)
                add(textarea)
            }
            setOnCloseRequest {

            }
            showAndWait()
        }
    }

}

private fun stringFromError(e: Throwable): String {
    val out = ByteArrayOutputStream()
    val writer = PrintWriter(out)
    e.printStackTrace(writer)
    writer.close()
    return out.toString()
}