package eu.pretix.pretixdesk.ui.helpers

import com.jfoenix.controls.JFXScrollPane
import javafx.event.EventTarget
import tornadofx.opcr

fun EventTarget.jfxScrollpane(op: (JFXScrollPane.() -> Unit)? = null): JFXScrollPane {
    val pane = JFXScrollPane()
    opcr(this, pane, op)
    return pane
}
