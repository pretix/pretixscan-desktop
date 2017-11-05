package eu.pretix.pretixdesk
import com.jfoenix.controls.JFXButton
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import tornadofx.*

class HelloWorld : View() {
    override val root: HBox by fxml()

    init {
        title = "pretixdesk"
    }

}