package eu.pretix.pretixdesk.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixdesk.ConfigureEvent
import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.readFromInputStream
import eu.pretix.pretixdesk.ui.helpers.MaterialSlide
import eu.pretix.pretixdesk.ui.helpers.jfxButton
import eu.pretix.pretixdesk.ui.helpers.jfxDialog
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import eu.pretix.pretixdesk.ui.style.STYLE_BACKGROUND_COLOR
import eu.pretix.pretixdesk.ui.style.STYLE_PRIMARY_DARK_COLOR
import eu.pretix.pretixdesk.ui.style.STYLE_STATE_VALID_COLOR
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import javafx.util.Duration
import tornadofx.*
import java.awt.Color

class SetupView : View() {
    private val controller: SetupController by inject()

    private val manualInput = textfield {
        promptText = "pretixdesk://setup?url=…"
        addClass(MainStyleSheet.mainSearchField)
        val mI = this

        setOnKeyReleased {
            if (it.code == KeyCode.ENTER && mI.text.length > 1) {
                handleConfiguration(mI.text)
                mI.clear()
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
        label(messages["setup_instructions2"]) {
            isWrapText = true
            textAlignment = TextAlignment.JUSTIFY
        }

        this += manualInput
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

    fun handleConfiguration(rawUrl: String) {
        val res = controller.configure(rawUrl)
        if (res == SetupResult.OK) {
            replaceWith(MainView::class, MaterialSlide(ViewTransition.Direction.UP))
        } else {
            val message = when (res) {
                SetupResult.INVALID_URL -> messages["setup_invalid"]
                SetupResult.VERSION_PRETIXDROID_OLD -> messages["setup_pretixdroid_old"]
                SetupResult.VERSION_PRETIX_OLD -> messages["setup_pretix_old"]
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

    override fun onDock() {
        super.onDock()
        if ((app as PretixDeskMain).getInitUrl() != null && !(app as PretixDeskMain).parameters_handled) {
            runAsync {
                Thread.sleep(100)
            } ui {
                handleConfiguration((app as PretixDeskMain).getInitUrl()!!)
            }
            (app as PretixDeskMain).parameters_handled = true
        }
    }

    init {
        title = messages["title"]

        subscribe<ConfigureEvent> { event ->
            forceFocus(root)
            handleConfiguration(event.rawUrl)
        }
    }

    private fun icon(icon: MaterialIcon): MaterialIconView {
        val iconView = MaterialIconView(icon)
        iconView.glyphSize = 25
        iconView.glyphStyle = "-fx-fill: white;"
        return iconView
    }
}