package eu.pretix.pretixdesk
import eu.pretix.pretixdesk.ui.MainView
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import javafx.scene.image.Image
import javafx.stage.Stage
import tornadofx.*


class PretixDeskMain : App(MainView::class, MainStyleSheet::class) {
    val configStore = PretixDeskConfig()

    override fun start(stage: Stage) {
        stage.icons += Image(PretixDeskMain::class.java.getResourceAsStream("icon.png"))
        stage.isMaximized = true
        stage.minHeight = 600.0
        stage.minWidth = 800.0
        super.start(stage)
    }
}
