package eu.pretix.pretixdesk.ui.helpers

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXDialogLayout
import javafx.event.EventTarget
import javafx.scene.layout.StackPane
import tornadofx.opcr

fun EventTarget.jfxDialog(dialogContainer: StackPane? = null, transitionType: JFXDialog.DialogTransition = JFXDialog.DialogTransition.CENTER, overlayClose: Boolean = true, op: (JFXDialogLayout.() -> Unit)? = null): JFXDialog {
    val content = JFXDialogLayout()
    val dialog = JFXDialog(dialogContainer, content, transitionType, overlayClose)
    op?.invoke(content)
    return dialog
}


fun EventTarget.jfxDialogLayout(op: (JFXDialogLayout.() -> Unit)? = null) = opcr(this, JFXDialogLayout(), op)