package eu.pretix.pretixdesk.ui

import eu.pretix.pretixdesk.ui.helpers.MaterialDuration
import eu.pretix.pretixdesk.ui.helpers.MaterialSlide
import eu.pretix.pretixdesk.ui.helpers.jfxButton
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import eu.pretix.pretixdesk.ui.style.STYLE_BACKGROUND_COLOR
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import javafx.util.Duration
import tornadofx.*


class StatusView : View() {
    private val controller: StatusController by inject()
    private var syncStatusTimeline: Timeline? = null


    private val syncStatusLabel = jfxButton("") {
        action {
            displaySyncStatus(controller, root)
        }
    }

    override val root: StackPane = stackpane {
        vbox {
            useMaxHeight = true

            style {
                alignment = Pos.CENTER
                backgroundColor += c(STYLE_BACKGROUND_COLOR)
                spacing = 20.px
            }

            spacer { }
            label("hi!")
            spacer { }
            hbox {
                addClass(MainStyleSheet.toolBar)
                jfxButton("GO BACK") {
                    action {
                        replaceWith(MainView::class, MaterialSlide(ViewTransition.Direction.RIGHT))
                    }
                }
                spacer {}
                this += syncStatusLabel
                spacer {}
                jfxButton("SETTINGS")
            }
        }
    }

    init {
        title = "pretixdesk"

        syncStatusTimeline = timeline {
            cycleCount = Timeline.INDEFINITE

            keyframe(Duration.seconds(.5)) {
                setOnFinished {
                    var text = "?"
                    runAsync {
                        text = controller.syncStatusText()
                    } ui {
                        syncStatusLabel.text = text
                    }
                }
            }
        }

        currentStage?.setOnCloseRequest {
            syncStatusTimeline?.stop()
        }
    }
}
