package eu.pretix.pretixdesk.ui

import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.ui.helpers.jfxSpinner
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import eu.pretix.pretixdesk.ui.style.STYLE_BACKGROUND_COLOR
import javafx.animation.Interpolator
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.VBox
import javafx.util.Duration
import tornadofx.*

class MainView : View() {
    override val root = vbox {
        style {
            alignment = Pos.CENTER
            backgroundColor += c(STYLE_BACKGROUND_COLOR)
            spacing = 20.px
        }

        hbox {
            style {
                paddingBottom = 20.0
                alignment = Pos.CENTER
            }
            imageview(Image(PretixDeskMain::class.java.getResourceAsStream("logo.png")))
        }
    }

    var resultCard: VBox? = null
    var spinnerAnimation: Timeline? = null

    val searchField = textfield {
        promptText = "Ticket code or name…"
        addClass(MainStyleSheet.mainSearchField)

        setOnKeyReleased {
            if (it.code == KeyCode.ENTER) {
                loadNewCard()
            }
        }
    }

    val mainSpinner = jfxSpinner {
        useMaxHeight = false
        useMaxWidth = false
        opacity = 0.0
    }

    val resultHolder = stackpane {
        addClass(eu.pretix.pretixdesk.ui.style.MainStyleSheet.resultHolder)

        vbox {
            this += mainSpinner
        }
    }

    init {
        title = "pretixdesk"

        with(root) {
            this += searchField
            this += resultHolder
        }
    }

    fun loadNewCard() {
        val oldResultCard = resultCard
        if (oldResultCard != null) {
            timeline {
                keyframe(Duration.seconds(0.25)) {
                    keyvalue(oldResultCard.translateXProperty(), 480.0, Interpolator.EASE_IN)
                    keyvalue(oldResultCard.opacityProperty(), 0.0, Interpolator.EASE_IN)
                }
            }.setOnFinished {
                oldResultCard.removeFromParent()
            }
        }

        spinnerAnimation?.stop()
        spinnerAnimation = timeline {
            keyframe(Duration.seconds(0.25)) {
                keyvalue(mainSpinner.opacityProperty(), 1.0, Interpolator.EASE_OUT)
            }
        }

        runAsync {
            Thread.sleep(2000)
        } ui {
            spinnerAnimation?.stop()
            spinnerAnimation = timeline {
                keyframe(Duration.seconds(0.25)) {
                    keyvalue(mainSpinner.opacityProperty(), 0.0, Interpolator.EASE_OUT)
                }
            }

            val newCard = makeNewCard("Foo")
            resultHolder += newCard
            resultCard = newCard

            timeline {
                keyframe(Duration.seconds(0.5)) {
                    keyvalue(newCard.translateXProperty(), 0.0, Interpolator.EASE_OUT)
                    keyvalue(newCard.opacityProperty(), 1.0, Interpolator.EASE_OUT)
                }
            }.setOnFinished {
                mainSpinner.opacity = 0.0
            }
        }
    }

    fun makeNewCard(name: String): VBox {
        val vb = VBox()
        with(vb) {
            translateX = -480.0
            opacity = 0.2

            vbox {
                addClass(MainStyleSheet.card)
                addClass(MainStyleSheet.resultCard)

                vbox {
                    addClass(MainStyleSheet.cardBody)
                    addClass(MainStyleSheet.cardHeaderValid)
                    label("VALID") {
                        addClass(MainStyleSheet.cardHeaderLabel)
                    }
                }

                vbox {
                    addClass(MainStyleSheet.cardBody)
                    label(name);
                }
            }
        }
        return vb
    }
}