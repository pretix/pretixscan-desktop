package eu.pretix.pretixdesk.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.ui.helpers.MaterialSlide
import eu.pretix.pretixdesk.ui.helpers.jfxButton
import eu.pretix.pretixdesk.ui.helpers.jfxDialog
import eu.pretix.pretixdesk.ui.helpers.jfxProgressDialog
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import eu.pretix.pretixdesk.ui.style.STYLE_PRIMARY_DARK_COLOR
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.StackPane
import javafx.scene.text.TextAlignment
import tornadofx.*

class SetupView : View() {
    private val controller: SetupController by inject()

    private val manualInputURL : TextField = textfield {
        text = "https://pretix.eu"
        addClass(MainStyleSheet.mainSearchField)
        val mI = this

        setOnKeyReleased {
            if (it.code == KeyCode.ENTER && mI.text.length > 1) {
                manualInputToken.requestFocus()
            }
        }
    }
    private val manualInputToken : TextField = textfield {
        promptText = "wnc5y04mlpmvguky"
        addClass(MainStyleSheet.mainSearchField)
        val mI = this

        setOnKeyReleased {
            if (it.code == KeyCode.ENTER && mI.text.length > 1) {
                handleConfiguration(manualInputURL.text, mI.text)
            }
        }
    }

    private val contentBox = vbox {
        useMaxHeight = true
        addClass(MainStyleSheet.setupScreen)

        style {
            alignment = Pos.CENTER
            spacing = 15.px
        }

        hbox {
            style {
                paddingBottom = 10.0
                alignment = Pos.CENTER
            }
            imageview(Image(PretixDeskMain::class.java.getResourceAsStream("logo_white.png")))
        }

        label(messages["setup_headline"]) {
            style {
                fontSize = 20.pt
            }
        }
        label(messages["setup_instructions_headline"]) {
            isWrapText = true
            textAlignment = TextAlignment.CENTER
            style {
                fontSize = 13.pt
            }
        }
        label(messages["setup_instructions_step1"]) {
            isWrapText = true
            textAlignment = TextAlignment.LEFT
            graphic = icon(MaterialIcon.CHECK)
            style {
                alignment = Pos.CENTER_LEFT
                fontSize = 13.pt
                minWidth = 460.px
                maxWidth = 460.px
            }
        }
        label(messages["setup_instructions_step2"]) {
            isWrapText = true
            textAlignment = TextAlignment.LEFT
            graphic = icon(MaterialIcon.CHECK)
            style {
                alignment = Pos.CENTER_LEFT
                fontSize = 13.pt
                minWidth = 460.px
                maxWidth = 460.px
            }
        }
        label(messages["setup_instructions_step3"]) {
            isWrapText = true
            textAlignment = TextAlignment.LEFT
            graphic = icon(MaterialIcon.CHECK)
            style {
                alignment = Pos.CENTER_LEFT
                fontSize = 13.pt
                minWidth = 460.px
                maxWidth = 460.px
            }
        }

        this += manualInputURL
        this += manualInputToken
        manualInputToken.requestFocus()
    }

    override val root: StackPane = stackpane {
        vbox {
            useMaxHeight = true
            style {
                backgroundColor += c(STYLE_PRIMARY_DARK_COLOR)
                alignment = Pos.CENTER
                spacing = 20.px
            }

            spacer {
                style {
                    maxHeight = 50.px
                }
            }
            this += contentBox
            spacer {
                style {
                    maxHeight = 50.px
                }
            }
        }
    }

    fun handleConfiguration(url: String, token: String) {
        val progressDialog = jfxProgressDialog(heading = messages["progress_connecting"]) {}
        progressDialog.show(root)
        runAsync {
            controller.configure(url, token)
        } ui {
            progressDialog.close()
            if (it.state == SetupResultState.OK) {
                replaceWith(SelectEventView::class, MaterialSlide(ViewTransition.Direction.UP))
            } else {
                val message = when(it.state) {
                    SetupResultState.ERR_SSL -> messages["setup_error_ssl"]
                    SetupResultState.ERR_IO -> messages["setup_error_io"]
                    SetupResultState.ERR_SERVERERROR -> messages["setup_error_server"]
                    SetupResultState.ERR_BADREQUEST -> it.message
                    SetupResultState.ERR_BADRESPONSE -> messages["setup_error_response"]
                    else -> ""
                }

                val okButton: JFXButton = jfxButton(messages.getString("dialog_ok").toUpperCase())
                val dialog = jfxDialog(transitionType = JFXDialog.DialogTransition.BOTTOM) {
                    setBody(label(message))
                    setActions(okButton)
                }
                okButton.action {
                    dialog.close()
                }
                dialog.show(root)
            }
        }
    }

    init {
        title = messages["title"]
    }

    private fun icon(icon: MaterialIcon): MaterialIconView {
        val iconView = MaterialIconView(icon)
        iconView.glyphSize = 25
        iconView.glyphStyle = "-fx-fill: white;"
        return iconView
    }
}