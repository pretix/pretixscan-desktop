package eu.pretix.pretixscan.desktop.ui

import eu.pretix.libpretixsync.check.CheckException
import eu.pretix.libpretixsync.setup.RemoteEvent
import eu.pretix.pretixscan.desktop.PretixScanMain
import eu.pretix.pretixscan.desktop.ui.helpers.*
import eu.pretix.pretixscan.desktop.ui.style.MainStyleSheet
import eu.pretix.pretixscan.desktop.ui.style.STYLE_BACKGROUND_COLOR
import eu.pretix.pretixscan.desktop.ui.style.STYLE_TEXT_COLOR_MUTED
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.text.FontWeight
import tornadofx.*
import java.text.SimpleDateFormat


class SelectEventView : View() {
    private val controller: SelectEventController by inject()
    private val eventList = ArrayList<RemoteEvent>().observable()
    private var spinnerAnimation: Timeline? = null

    private val eventListView = jfxListview(eventList) {
        vboxConstraints { vGrow = Priority.ALWAYS }

        cellCache {
            vbox {
                label(it.name) {
                    style {
                        fontWeight = FontWeight.BOLD
                    }
                }
                label(it.slug) {
                    style {
                        textFill = c(STYLE_TEXT_COLOR_MUTED)
                    }
                }
                val formatter = SimpleDateFormat(messages.getString("date_format"))
                if (it.date_to != null) {
                    label(formatter.format(it.date_from.toLocalDate().toDate()) + " – " + formatter.format(it.date_to!!.toLocalDate().toDate()))
                } else {
                    label(formatter.format(it.date_from.toLocalDate().toDate()))
                }
            }
        }
        cellFormat {
        }
        placeholder = label(messages["no_events"])
    }

    private fun showSpinner() {
        spinnerAnimation?.stop()
        spinnerAnimation = timeline {
            keyframe(MaterialDuration.ENTER) {
                keyvalue(mainSpinner.opacityProperty(), 1.0, MaterialInterpolator.ENTER)
            }
        }
    }

    private fun hideSpinner() {
        spinnerAnimation?.stop()
        spinnerAnimation = timeline {
            keyframe(MaterialDuration.EXIT) {
                keyvalue(mainSpinner.opacityProperty(), 0.0, MaterialInterpolator.EXIT)
            }
        }
    }

    private val mainSpinner = jfxSpinner {
        useMaxHeight = false
        useMaxWidth = false
        opacity = 0.0
        maxWidth = 64.0
        maxHeight = 64.0
    }

    private val contentBox = vbox {
        vboxConstraints { vGrow = Priority.ALWAYS }
        useMaxHeight = true

        style {
            alignment = Pos.CENTER
            backgroundColor += c(STYLE_BACKGROUND_COLOR)
            spacing = 10.px
        }

        label(messages["select_event"])
        hbox {
            useMaxHeight = true
            addClass(MainStyleSheet.card)
            addClass(MainStyleSheet.selectHolder)
            hboxConstraints { hGrow = Priority.ALWAYS }
            stackpane {
                hboxConstraints { hGrow = Priority.ALWAYS }
                this += eventListView
                this += mainSpinner
            }

            eventListView.setOnMouseClicked {
                if (it.clickCount == 2 && eventListView.selectionModel.selectedItem != null) {
                    handleEventSelected(eventListView.selectionModel.selectedItem)
                    it.consume()
                }
            }
            eventListView.setOnKeyReleased {
                if (it.code == KeyCode.ENTER && eventListView.selectionModel.selectedItem != null) {
                    handleEventSelected(eventListView.selectionModel.selectedItem)
                    it.consume()
                }
            }
        }
    }

    fun handleEventSelected(event: RemoteEvent) {
        controller.setEvent(event)
        replaceWith(SelectCheckInListView::class, MaterialSlide(ViewTransition.Direction.UP))
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
                        gridpaneColumnConstraints { percentWidth = 33.33 }
                        style {
                            alignment = Pos.CENTER_LEFT
                        }
                        val conf = (app as PretixScanMain).configStore
                        if (conf.eventSlug !== null) {
                            jfxButton(messages["toolbar_back"]) {
                                action {
                                    replaceWith(MainView::class, MaterialSlide(ViewTransition.Direction.RIGHT))
                                }
                            }
                        }
                    }
                    hbox {
                        gridpaneColumnConstraints { percentWidth = 33.33 }
                        style {
                            alignment = Pos.CENTER
                        }
                    }
                    hbox {
                        gridpaneColumnConstraints { percentWidth = 33.33 }
                        style {
                            alignment = Pos.CENTER_RIGHT
                        }
                        jfxButton(messages["toolbar_refresh"]) {
                            action {
                                loadEvents()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        if (!(app as PretixScanMain).configStore.isConfigured()) {
            replaceWith(SetupView::class, MaterialSlide(ViewTransition.Direction.DOWN))
        }
        loadEvents()
        currentWindow?.setOnCloseRequest {
            controller.close()
        }
    }

    fun loadEvents() {
        showSpinner()
        var events = emptyList<RemoteEvent>()
        runAsync {
            try {
                events = controller.fetchEvents()
            } catch (e: CheckException) {
            }

        } ui {
            eventList.clear()
            if (events.size > 0) {
                eventList.addAll(events)
            }
            hideSpinner()
        }
    }

    init {
        title = messages["title"]
    }
}
