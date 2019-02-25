package eu.pretix.pretixscan.desktop.ui.helpers

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXDialogLayout
import javafx.event.EventTarget
import javafx.scene.layout.StackPane
import tornadofx.action
import tornadofx.label


fun EventTarget.jfxDialog(dialogContainer: StackPane? = null, transitionType: JFXDialog.DialogTransition = JFXDialog.DialogTransition.CENTER, overlayClose: Boolean = true, op: (JFXDialogLayout.() -> Unit)? = null): JFXDialog {
    val content = JFXDialogLayout()
    val dialog = JFXDialog(dialogContainer, content, transitionType, true)
    op?.invoke(content)
    if (!overlayClose) {
        dialog.overlayCloseProperty().set(overlayClose)
    }
    return dialog
}

fun EventTarget.jfxAlert(dialogContainer: StackPane? = null, heading: String? = null, message: String, op: (JFXDialog.() -> Unit)? = null): JFXDialog {
    val closeButton: JFXButton = jfxButton("CLOSE")
    val dialog = jfxDialog(transitionType = JFXDialog.DialogTransition.CENTER) {
        if (heading != null) {
            setHeading(label(heading))
        }
        setBody(label(message))
        setActions(closeButton)
    }
    closeButton.action {
        dialog.close()
    }
    op?.invoke(dialog)
    return dialog
}

fun EventTarget.jfxProgressDialog(dialogContainer: StackPane? = null, heading: String? = null, op: (JFXDialog.() -> Unit)? = null): JFXDialog {
    val dialog = jfxDialog(transitionType = JFXDialog.DialogTransition.CENTER, overlayClose = false) {
        if (heading != null) {
            setHeading(label(heading))
        }
        setBody(jfxSpinner { })
    }
    op?.invoke(dialog)
    return dialog
}