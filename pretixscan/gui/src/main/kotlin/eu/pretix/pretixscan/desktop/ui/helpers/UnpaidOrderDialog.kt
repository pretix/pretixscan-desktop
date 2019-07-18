package eu.pretix.pretixscan.desktop.ui.helpers

import com.jfoenix.controls.*
import javafx.event.EventTarget
import tornadofx.*
import tornadofx.FX.Companion.messages

fun EventTarget.unpaidOrderDialog(retry: ((Boolean) -> Unit)? = null): JFXDialog {
    val content = JFXDialogLayout()

    val fview = vbox {
        label(messages.getString("dialog_unpaid_text"))
    }

    val closeButton: JFXButton = jfxButton(messages.getString("dialog_cancel"))
    val okButton: JFXButton = jfxButton(messages.getString("dialog_unpaid_retry").toUpperCase())

    content.setHeading(label(messages.getString("dialog_unpaid_title")))
    content.setActions(closeButton, okButton)
    content.setBody(fview)  // TODO: scrollpane?

    val dialog = JFXDialog(null, content, JFXDialog.DialogTransition.BOTTOM, true)
    dialog.overlayCloseProperty().set(false)
    closeButton.action {
        dialog.close()
    }
    okButton.action {
        dialog.close()
        retry?.invoke(true)
    }
    return dialog
}