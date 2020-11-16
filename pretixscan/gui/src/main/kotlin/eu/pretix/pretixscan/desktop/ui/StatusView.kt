package eu.pretix.pretixscan.desktop.ui

import eu.pretix.libpretixsync.check.CheckException
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixscan.desktop.PretixScanMain
import eu.pretix.pretixscan.desktop.ui.helpers.*
import eu.pretix.pretixscan.desktop.ui.style.MainStyleSheet
import eu.pretix.pretixscan.desktop.ui.style.STYLE_BACKGROUND_COLOR
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
                addClass(MainStyleSheet.eventInfoItem)
                vbox {
                    addClass(MainStyleSheet.cardBody)
                    hbox {
                        label(it.name ?: "") {
                            addClass(MainStyleSheet.eventInfoItemHeader)
                            isWrapText = true
                        }
                        spacer {}
                        label(it.checkins.toString() + "/" + it.total.toString()) {
                            addClass(MainStyleSheet.eventInfoItemHeader)
                            addClass(MainStyleSheet.eventInfoItemNumber)
                        }
                    }
                    for (variation in it.variations!!) {
                        hbox {
                            label(variation.name ?: "") {
                                addClass(MainStyleSheet.eventInfoItemBody)
                                isWrapText = true
                            }
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
                    label(data.eventName ?: "") { addClass(MainStyleSheet.eventInfoItemHeader) }
                    spacer {}
                    label(data.alreadyScanned.toString() + "/" + data.totalTickets.toString()) {
                        addClass(MainStyleSheet.eventInfoItemHeader)
                        addClass(MainStyleSheet.eventInfoItemNumber)
                        style {
                            minWidth = 60.px
                        }
                    }
                }
                if (data.currentlyInside != null) {
                    hbox {
                        label(messages["currently_inside"]) { addClass(MainStyleSheet.eventInfoItemHeader) }
                        spacer {}
                        label(data.currentlyInside.toString()) {
                            addClass(MainStyleSheet.eventInfoItemHeader)
                            addClass(MainStyleSheet.eventInfoItemNumber)
                            style {
                                minWidth = 60.px
                            }
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

    private val eventNameLabel = label("Event name")

    override val root: StackPane = stackpane {
        vbox {
            useMaxHeight = true

            style {
                alignment = Pos.CENTER
                backgroundColor += c(STYLE_BACKGROUND_COLOR)
                spacing = 20.px
            }

            gridpane {
                addClass(MainStyleSheet.toolBar)
                style {
                    minWidth = 100.percent
                }
                row {
                    hbox {
                        gridpaneColumnConstraints { percentWidth = 66.66 }
                        style {
                            alignment = Pos.CENTER_LEFT
                            paddingLeft = 10.0
                        }
                        this += eventNameLabel
                    }
                    hbox {
                        gridpaneColumnConstraints { percentWidth = 33.33 }
                        style {
                            alignment = Pos.CENTER_RIGHT
                        }
                        jfxButton(messages["toolbar_switch"]) {
                            action {
                                replaceWith(SelectEventView::class, MaterialSlide(ViewTransition.Direction.DOWN))
                            }
                        }
                    }
                }
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
                        jfxButton(messages["toolbar_back"]) {
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
                        jfxButton(messages["toolbar_refresh"]) {
                            action {
                                loadStatus()
                            }
                        }
                        jfxButton(messages["toolbar_settings"]) {
                            action {
                                replaceWith(SettingsView::class, MaterialSlide(ViewTransition.Direction.LEFT))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        val conf = (app as PretixScanMain).configStore
        if (!conf.isConfigured()) {
            replaceWith(SetupView::class, MaterialSlide(ViewTransition.Direction.DOWN))
        }
        if (conf.eventName == null || conf.eventSlug == null) {
            replaceWith(SelectEventView::class, MaterialSlide(ViewTransition.Direction.DOWN))
        } else if (conf.checkInListId == 0L) {
            replaceWith(SelectCheckInListView::class, MaterialSlide(ViewTransition.Direction.DOWN))
        }
        eventNameLabel.text = conf.eventName + ": " + conf.checkInListName
        currentWindow?.setOnCloseRequest {
            controller.close()
        }
        loadStatus()
    }

    fun loadStatus() {
        showSpinner()
        runAsync {
            try {
                statusData = controller.retrieveInfo()
            } catch (e: CheckException) {
                statusData = null
            }

        } ui {
            eventInfoList.clear()
            if (statusData != null) {
                eventInfoList.addAll(statusData!!.items!!)
                refreshHeaderCard(statusData!!)
            }
            hideSpinner()
        }
    }

    init {
        title = messages["title"]

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
