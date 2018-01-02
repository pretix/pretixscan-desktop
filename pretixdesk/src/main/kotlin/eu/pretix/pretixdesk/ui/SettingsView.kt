package eu.pretix.pretixdesk.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.readFromInputStream
import eu.pretix.pretixdesk.ui.helpers.*
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import eu.pretix.pretixdesk.ui.style.STYLE_BACKGROUND_COLOR
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import tornadofx.*
import java.awt.Desktop
import java.net.URI

class SettingsView : View() {
    private val controller: SettingsController by inject()
    private var syncStatusTimeline: Timeline? = null
    private var loadDataTimeline: Timeline? = null
    private var spinnerAnimation: Timeline? = null
    private var statusData: TicketCheckProvider.StatusResult? = null

    private var headerCardHolder = vbox {
        style {
            alignment = Pos.CENTER
        }
        vbox {
            addClass(MainStyleSheet.card)
            addClass(MainStyleSheet.eventInfoHeader)
            vbox {
                addClass(MainStyleSheet.cardBody)
                hbox {
                    label(messages["settings_head"]) { addClass(MainStyleSheet.eventInfoItemHeader) }
                }
            }
        }
    }

    private val contentBox = vbox {
        vboxConstraints { vGrow = Priority.ALWAYS }
        useMaxHeight = true

        style {
            alignment = Pos.CENTER
            backgroundColor += c(STYLE_BACKGROUND_COLOR)
            spacing = 10.px
        }

        this += headerCardHolder

        vbox {
            addClass(MainStyleSheet.card)
            addClass(MainStyleSheet.eventSettingsCard)
            vbox {
                addClass(MainStyleSheet.cardBody)
                hbox {
                    label(messages["settings_reset_text"])
                    spacer {}
                    jfxButton(messages["settings_reset_button"].toUpperCase()) {
                        action {
                            if (controller.hasLocalChanges()) {

                                val okButton: JFXButton = jfxButton(messages.getString("dialog_ok").toUpperCase())
                                val cancelButton: JFXButton = jfxButton(messages.getString("dialog_cancel").toUpperCase())
                                val dialog = jfxDialog(transitionType = JFXDialog.DialogTransition.BOTTOM) {
                                    setBody(label(messages.getString("settings_reset_warning")))
                                    setActions(cancelButton, okButton)
                                }
                                cancelButton.action {
                                    dialog.close()
                                }
                                okButton.action {
                                    dialog.close()
                                    controller.resetApp()
                                }
                                dialog.show(root)
                            } else {
                                controller.resetApp()
                            }
                        }
                    }
                }
            }
        }

        vbox {
            addClass(MainStyleSheet.card)
            addClass(MainStyleSheet.eventSettingsCard)
            vbox {
                addClass(MainStyleSheet.cardBody)
                hbox {
                    label(messages["settings_licenses"])
                    spacer {}
                    jfxButton(messages["settings_licenses_more"].toUpperCase()) {
                        action {
                            val closeButton: JFXButton = this.jfxButton(messages.getString("dialog_close"))
                            val dialog = this.jfxDialog(transitionType = JFXDialog.DialogTransition.BOTTOM) {
                                setHeading(label(messages.getString("settings_licenses")))
                                setBody(jfxScrollpane {
                                    style {
                                        minHeight = 300.px
                                        maxHeight = 300.px
                                    }
                                    jfxTextarea(readFromInputStream(PretixDeskMain::class.java.getResourceAsStream("licenses.txt"))) {
                                        isEditable = false
                                    }
                                })
                                setActions(closeButton)
                            }
                            closeButton.action {
                                dialog.close()
                            }
                            dialog.show(root)
                        }
                    }
                }
            }
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

            gridpane {
                addClass(MainStyleSheet.toolBar)
                row {
                    hbox {
                        style {
                            alignment = Pos.CENTER_LEFT
                        }
                        jfxButton(messages["toolbar_back"]) {
                            action {
                                replaceWith(MainView::class, MaterialSlide(ViewTransition.Direction.RIGHT))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
    }

    init {
        title = messages["title"]
    }
}