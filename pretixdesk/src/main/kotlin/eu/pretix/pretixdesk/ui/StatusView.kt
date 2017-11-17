package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixdesk.ui.helpers.*
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import eu.pretix.pretixdesk.ui.style.STYLE_BACKGROUND_COLOR
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import tornadofx.*


class StatusView : View() {
    private val controller: StatusController by inject()
    private var syncStatusTimeline: Timeline? = null
    private var loadDataTimeline: Timeline? = null
    private var spinnerAnimation: Timeline? = null
    private var statusData: TicketCheckProvider.StatusResult? = null

    private val eventInfoList = ArrayList<TicketCheckProvider.StatusResultItem>().observable()
    private val eventInfoListView = jfxListview(eventInfoList) {
        addClass(MainStyleSheet.eventInfoList)
        vboxConstraints { vGrow = Priority.ALWAYS }
        isFocusTraversable = false

        cellCache {
            vbox {
                addClass(MainStyleSheet.card)
                vbox {
                    addClass(MainStyleSheet.cardBody)
                    hbox {
                        label(it.name) { addClass(MainStyleSheet.eventInfoItemHeader) }
                        spacer {}
                        label(it.checkins.toString() + "/" + it.total.toString()) {
                            addClass(MainStyleSheet.eventInfoItemHeader)
                            addClass(MainStyleSheet.eventInfoItemNumber)
                        }
                    }
                    for (variation in it.variations) {
                        hbox {
                            label(variation.name) { addClass(MainStyleSheet.eventInfoItemBody) }
                            spacer {}
                            label(variation.checkins.toString() + "/" + variation.total.toString()) {
                                addClass(MainStyleSheet.eventInfoItemBody)
                                addClass(MainStyleSheet.eventInfoItemNumber)
                            }
                        }
                    }
                }
            }
        }
        cellFormat {
        }
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

    private val syncStatusLabel = jfxButton("") {
        action {
            displaySyncStatus(controller, root)
        }
    }

    private var headerCardHolder = vbox {
        style {
            alignment = Pos.CENTER
        }
    }
    private var headerCard: VBox? = null

    fun refreshHeaderCard(data: TicketCheckProvider.StatusResult) {
        if (headerCard != null) {
            headerCard?.removeFromParent()
        }
        headerCard = vbox {
            addClass(MainStyleSheet.card)
            addClass(MainStyleSheet.eventInfoHeader)
            vbox {
                addClass(MainStyleSheet.cardBody)
                hbox {
                    label(data.eventName) { addClass(MainStyleSheet.eventInfoItemHeader) }
                    spacer {}
                    label(data.alreadyScanned.toString() + "/" + data.totalTickets.toString()) {
                        addClass(MainStyleSheet.eventInfoItemHeader)
                        addClass(MainStyleSheet.eventInfoItemNumber)
                        style {
                            minWidth = 60.px
                        }
                    }
                }
            }
        }
        headerCardHolder.add(headerCard!!)
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
        stackpane {
            this += eventInfoListView
            this += mainSpinner
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
                        gridpaneColumnConstraints { percentWidth = 33.33 }
                        style {
                            alignment = Pos.CENTER_LEFT
                        }
                        jfxButton("GO BACK") {
                            action {
                                replaceWith(MainView::class, MaterialSlide(ViewTransition.Direction.RIGHT))
                            }
                        }
                    }
                    hbox {
                        gridpaneColumnConstraints { percentWidth = 33.33 }
                        style {
                            alignment = Pos.CENTER
                        }
                        this += syncStatusLabel
                    }
                    hbox {
                        gridpaneColumnConstraints { percentWidth = 33.33 }
                        style {
                            alignment = Pos.CENTER_RIGHT
                        }
                        jfxButton("REFRESH") {
                            action {
                                loadStatus()
                            }
                        }
                        jfxButton("SETTINGS")
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        loadStatus()
    }

    fun loadStatus() {
        showSpinner()
        runAsync {
            statusData = controller.retrieveInfo()
        } ui {
            eventInfoList.clear()
            if (statusData != null) {
                eventInfoList.addAll(statusData!!.items)
                refreshHeaderCard(statusData!!)
            }
            hideSpinner()
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

        loadDataTimeline = timeline {
            cycleCount = Timeline.INDEFINITE

            keyframe(Duration.seconds(30.0)) {
                setOnFinished {
                    if (isDocked) {
                        loadStatus()
                    }
                }
            }
        }

        currentStage?.setOnCloseRequest {
            syncStatusTimeline?.stop()
        }
    }
}
