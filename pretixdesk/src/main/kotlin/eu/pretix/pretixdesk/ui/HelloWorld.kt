package eu.pretix.pretixdesk.ui

import eu.pretix.pretixdesk.ui.helpers.*
import tornadofx.*

class HelloWorld : View() {
    override val root = hbox {
        jfxButton("Foo")
        jfxTextfield {  }
        jfxScrollpane {  }
        jfxSlider{  }
    }

    init {
        title = "pretixdesk"
    }

}