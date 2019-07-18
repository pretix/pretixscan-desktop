package eu.pretix.pretixscan.desktop.ui.helpers

import com.jfoenix.controls.JFXScrollPane
import javafx.event.EventTarget
import tornadofx.opcr

fun EventTarget.jfxScrollpane(op: (JFXScrollPane.() -> Unit) = {}): JFXScrollPane {
    val pane = JFXScrollPane()
    opcr(this, pane, op)
    return pane
}
