package eu.pretix.pretixdesk.ui

import eu.pretix.pretixdesk.ui.helpers.*
import tornadofx.*

class HelloWorld : View() {
    override val root = hbox {
        style {
            backgroundColor += c(STYLE_BACKGROUND_COLOR)
        }

        jfxButton("Foo")
    }

    init {
        title = "pretixdesk"
    }

}