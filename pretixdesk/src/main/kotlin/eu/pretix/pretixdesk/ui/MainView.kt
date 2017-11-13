package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.ui.helpers.*
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import eu.pretix.pretixdesk.ui.style.STYLE_BACKGROUND_COLOR
import eu.pretix.pretixdesk.ui.style.STYLE_STATE_VALID_COLOR
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.VBox
import tornadofx.*

class MainView : View() {
    val controller: MainController by inject()
    var resultCards: List<VBox> = ArrayList<VBox>()
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

    val contentBox = vbox {
        useMaxHeight = true

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

        this += searchField
        this += resultHolder
    }

    override val root = vbox {
        useMaxHeight = true

        style {
            alignment = Pos.CENTER
            backgroundColor += c(STYLE_BACKGROUND_COLOR)
            spacing = 20.px
        }

        spacer { }
        this += contentBox
        spacer { }
        hbox {
            addClass(MainStyleSheet.toolBar)

            jfxTogglebutton("SCAN ONLINE") {
                toggleColor = c(STYLE_STATE_VALID_COLOR)
            }
            spacer {}
            jfxButton("SETTINGS")
        }
    }

    init {
        title = "pretixdesk"
    }

    fun loadNewCard() {
        for (oldResultCard in resultCards) {
            timeline {
                keyframe(MaterialDuration.EXIT) {
                    keyvalue(oldResultCard.translateXProperty(), 480.0, MaterialInterpolator.EXIT)
                    keyvalue(oldResultCard.opacityProperty(), 0.0, MaterialInterpolator.EXIT)
                }
            }.setOnFinished {
                oldResultCard.removeFromParent()
                resultCards -= oldResultCard
            }
        }

        spinnerAnimation?.stop()
        spinnerAnimation = timeline {
            keyframe(MaterialDuration.ENTER) {
                keyvalue(mainSpinner.opacityProperty(), 1.0, MaterialInterpolator.ENTER)
            }
        }

        val value = searchField.text
        searchField.text = ""
        if (value == "") {
            return
        }

        var resultData: TicketCheckProvider.CheckResult? = null
        runAsync {
            resultData = controller.handleScanInput(value)
        } ui {
            spinnerAnimation?.stop()
            spinnerAnimation = timeline {
                keyframe(MaterialDuration.EXIT) {
                    keyvalue(mainSpinner.opacityProperty(), 0.0, MaterialInterpolator.EXIT)
                }
            }

            val newCard = makeNewCard(resultData)
            resultHolder += newCard
            resultCards += newCard

            timeline {
                keyframe(MaterialDuration.ENTER) {
                    keyvalue(newCard.translateXProperty(), 0.0, MaterialInterpolator.ENTER)
                    keyvalue(newCard.opacityProperty(), 1.0, MaterialInterpolator.ENTER)
                }
            }.setOnFinished {
                mainSpinner.opacity = 0.0
            }
        }
    }

    fun makeNewCard(data: TicketCheckProvider.CheckResult?): VBox {
        val vb = VBox()
        with(vb) {
            translateX = -480.0
            opacity = 0.2

            vbox {
                addClass(MainStyleSheet.card)
                addClass(MainStyleSheet.resultCard)

                vbox {
                    addClass(MainStyleSheet.cardBody)
                    addClass(when (data?.type) {
                        TicketCheckProvider.CheckResult.Type.INVALID -> MainStyleSheet.cardHeaderErrorNoMessage
                        TicketCheckProvider.CheckResult.Type.VALID -> MainStyleSheet.cardHeaderValid
                        TicketCheckProvider.CheckResult.Type.USED -> MainStyleSheet.cardHeaderRepeat
                        TicketCheckProvider.CheckResult.Type.ERROR -> MainStyleSheet.cardHeaderError
                        TicketCheckProvider.CheckResult.Type.UNPAID -> MainStyleSheet.cardHeaderError
                        TicketCheckProvider.CheckResult.Type.PRODUCT -> MainStyleSheet.cardHeaderError
                        null -> MainStyleSheet.cardHeaderError
                    })

                    val headline = when (data?.type) {
                        TicketCheckProvider.CheckResult.Type.INVALID -> "UNKNOWN TICKET"
                        TicketCheckProvider.CheckResult.Type.VALID -> "VALID"
                        TicketCheckProvider.CheckResult.Type.USED -> "ALREADY SCANNED"
                        TicketCheckProvider.CheckResult.Type.ERROR -> "ERROR"
                        TicketCheckProvider.CheckResult.Type.UNPAID -> "NOT PAID"
                        TicketCheckProvider.CheckResult.Type.PRODUCT -> "INVALID PRODUCT"
                        null -> "UNKNOWN ERROR"
                    }

                    label(headline) {
                        addClass(MainStyleSheet.cardHeaderLabel)
                    }
                }
                if (data?.type != TicketCheckProvider.CheckResult.Type.INVALID) {
                    vbox {
                        addClass(MainStyleSheet.cardBody)
                        label(data?.message ?: "?");
                    }
                }
            }
        }
        return vb
    }
}
