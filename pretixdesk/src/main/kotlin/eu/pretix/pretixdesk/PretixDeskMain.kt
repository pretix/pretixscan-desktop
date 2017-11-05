package eu.pretix.pretixdesk
import javafx.scene.image.Image
import javafx.stage.Stage
import tornadofx.*


class PretixDeskMain : App() {
    override val primaryView = HelloWorld::class

    override fun start(stage: Stage) {
        stage.icons += Image(PretixDeskMain::class.java.getResourceAsStream("icon.png"))
        stage.isMaximized = true
        super.start(stage)
    }
}
