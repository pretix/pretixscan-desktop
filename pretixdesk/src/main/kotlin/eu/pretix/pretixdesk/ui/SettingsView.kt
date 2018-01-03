package eu.pretix.pretixdesk.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixdesk.ConfigureEvent
import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.readFromInputStream
import eu.pretix.pretixdesk.ui.helpers.*
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import eu.pretix.pretixdesk.ui.style.STYLE_BACKGROUND_COLOR
import eu.pretix.pretixdesk.ui.style.STYLE_STATE_VALID_COLOR
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import tornadofx.*
import java.awt.SystemColor.window
import javafx.stage.Stage



class SettingsView : View() {
    private val controller: SettingsController by inject()

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

    private val soundBtn = jfxTogglebutton() {
        toggleColor = c(STYLE_STATE_VALID_COLOR)
        isSelected = !(app as PretixDeskMain).configStore.playSound
        action {
            controller.toggleSound(isSelected)
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
                                    replaceWith(SetupView::class, MaterialSlide(ViewTransition.Direction.DOWN))
                                }
                                dialog.show(root)
                            } else {
                                controller.resetApp()
                                replaceWith(SetupView::class, MaterialSlide(ViewTransition.Direction.DOWN))
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
                style {
                    padding = box(0.px, 15.px)
                }
                hbox {
                    style {
                        alignment = Pos.CENTER
                    }
                    label(messages["settings_sound"])
                    spacer {}
                    this += soundBtn
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
                        spacer {}
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        if (!(app as PretixDeskMain).configStore.isConfigured()) {
            replaceWith(SetupView::class, MaterialSlide(ViewTransition.Direction.DOWN))
        }
        soundBtn.isSelected = (app as PretixDeskMain).configStore.playSound
    }

    init {
        title = messages["title"]

        subscribe<ConfigureEvent> {
            forceFocus(root)
            requestReset(root)
        }
    }
}