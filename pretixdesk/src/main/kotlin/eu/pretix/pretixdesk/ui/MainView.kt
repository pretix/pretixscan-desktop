package eu.pretix.pretixdesk.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXDialogLayout
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.ui.helpers.*
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import eu.pretix.pretixdesk.ui.style.STYLE_BACKGROUND_COLOR
import eu.pretix.pretixdesk.ui.style.STYLE_STATE_VALID_COLOR
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import tornadofx.*
import java.util.regex.Pattern


var re_alphanum = Pattern.compile("^[a-zA-Z0-9]+\$")

class MainView : View() {
    private val controller: MainController by inject()
    private var resultCards: List<VBox> = ArrayList()
    private var spinnerAnimation: Timeline? = null
    private var searchCardAnimation: Timeline? = null
    private var syncStatusTimeline: Timeline? = null
    private var syncTriggerTimeline: Timeline? = null
    private var startSearchTimeline: Timeline? = null
    private var lastSearchQuery: String? = null

    private val searchField = textfield {
        promptText = "Ticket code or name…"
        addClass(MainStyleSheet.mainSearchField)
        val sF = this

        setOnKeyReleased {
            startSearchTimeline?.stop()
            if (it.code == KeyCode.ENTER) {
                if (sF.text == "" && searchResultCard.isVisible && searchResultListView.selectionModel.selectedIndex >= 0) {
                    handleSearchResultSelected(searchResultListView.selectionModel.selectedItem)
                } else {
                    handleInput(sF.text)
                    sF.text = ""
                }
                it.consume()
            } else if (it.code == KeyCode.DOWN && searchResultCard.isVisible) {
                searchResultListView.selectionModel.select(searchResultListView.selectionModel.selectedIndex + 1)
                searchResultListView.scrollTo(searchResultListView.selectionModel.selectedIndex)
                it.consume()
            } else if (it.code == KeyCode.UP && searchResultCard.isVisible) {
                searchResultListView.selectionModel.select(searchResultListView.selectionModel.selectedIndex - 1)
                searchResultListView.scrollTo(searchResultListView.selectionModel.selectedIndex)
                it.consume()
            } else {
                if (sF.text.length >= 4) {
                    startSearchTimeline = timeline {
                        keyframe(Duration.seconds(.1)) {
                            setOnFinished {
                                handleSearchInput(sF.text)
                            }
                        }
                    }
                } else {
                    hideSearchResultCard()
                }
            }
        }
    }

    private val mainSpinner = jfxSpinner {
        useMaxHeight = false
        useMaxWidth = false
        opacity = 0.0
    }

    private val searchResultList = ArrayList<TicketCheckProvider.SearchResult>().observable()
    private val searchResultListView = jfxListview(searchResultList) {
        vboxConstraints { vGrow = Priority.ALWAYS }

        cellCache {
            vbox {
                label(it.secret.substring(0, 20) + "…")
                hbox {
                    var ticketname = it.ticket
                    if (it.variation != null && it.variation != "null") {
                        ticketname += " – " + it.variation
                    }
                    // TODO: require attention
                    label(ticketname) { addClass(MainStyleSheet.searchItemProduct) }
                    spacer {}
                    if (it.isRedeemed) {
                        label("REDEEMED") { addClass(MainStyleSheet.searchItemStatusRedeemed) }
                    } else if (!it.isPaid) {
                        label("UNPAID") { addClass(MainStyleSheet.searchItemStatusUnpaid) }
                    } else {
                        label("VALID") { addClass(MainStyleSheet.searchItemStatusValid) }
                    }
                }
                hbox {
                    label(it.orderCode + "  ") { addClass(MainStyleSheet.searchItemOrderCode) }
                    label(it.attendee_name ?: "") { addClass(MainStyleSheet.searchItemAttendeeName) }
                }
            }
        }
        cellFormat {
        }
        placeholder = label("No search result found (enter at least 4 characters).")
    }

    private val searchResultCard = vbox {
        opacity = 0.0
        isVisible = false
        addClass(MainStyleSheet.card)
        vboxConstraints { vGrow = Priority.ALWAYS }
        style {
            padding = box(5.px)
            minHeight = 200.px
            maxHeight = 200.px
        }
        this += searchResultListView

        searchResultListView.setOnMouseClicked {
            if (it.clickCount == 2) {
                handleSearchResultSelected(searchResultListView.selectionModel.selectedItem)
                it.consume()
            }
        }
        searchResultListView.setOnKeyReleased {
            if (it.code == KeyCode.ENTER) {
                handleSearchResultSelected(searchResultListView.selectionModel.selectedItem)
                it.consume()
            }
        }
    }

    private val resultHolder = stackpane {
        addClass(eu.pretix.pretixdesk.ui.style.MainStyleSheet.resultHolder)

        vbox {
            this += mainSpinner
        }
        this += searchResultCard
    }

    private val contentBox = vbox {
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

    private val syncStatusLabel = jfxButton("") {
        action {
            displaySyncStatus()
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
            this += contentBox
            spacer { }
            hbox {
                addClass(MainStyleSheet.toolBar)

                jfxTogglebutton("SCAN ONLINE") {
                    toggleColor = c(STYLE_STATE_VALID_COLOR)
                    isSelected = !(app as PretixDeskMain).configStore.getAsyncModeEnabled()

                    action {
                        controller.toggleAsync(!isSelected)
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

        syncTriggerTimeline = timeline {
            cycleCount = Timeline.INDEFINITE

            keyframe(Duration.seconds(10.0)) {
                setOnFinished {
                    runAsync {
                        controller.triggerSync()
                    }
                }
            }
        }

        currentStage?.setOnCloseRequest {
            syncTriggerTimeline?.stop()
            syncStatusTimeline?.stop()
        }

        // Focus grabber
        currentStage?.addEventFilter(KeyEvent.KEY_PRESSED, {
            if (currentStage?.scene?.focusOwner !is TextField && re_alphanum.matcher(it.text).matches()) {
                searchField.requestFocus()
            }
        })
    }

    private fun handleSearchResultSelected(searchResult: TicketCheckProvider.SearchResult) {
        var resultData: TicketCheckProvider.CheckResult? = null

        val progressDialog = jfxProgressDialog(heading = "Redeeming ticket") {}
        progressDialog.show(root)
        runAsync {
            resultData = controller.handleScanInput(searchResult.secret)
        } ui {
            val message = when (resultData?.type) {
                TicketCheckProvider.CheckResult.Type.INVALID -> "Unknown ticket."
                TicketCheckProvider.CheckResult.Type.VALID -> "Ticket successfully redeemed."
                TicketCheckProvider.CheckResult.Type.USED -> "Ticket already used."
                TicketCheckProvider.CheckResult.Type.ERROR -> "Unknown error."
                TicketCheckProvider.CheckResult.Type.UNPAID -> "Ticket not paid."
                TicketCheckProvider.CheckResult.Type.PRODUCT -> "This product type is invalid."
                null -> ""
            }
            progressDialog.isOverlayClose = true
            (progressDialog.content as JFXDialogLayout).setBody(label(message))
            (progressDialog.content as JFXDialogLayout).setActions(
                    jfxButton("CLOSE") {
                        action {
                            progressDialog.close()
                        }
                    }
            )

            if (resultData?.type == TicketCheckProvider.CheckResult.Type.VALID) {
                searchResult.isRedeemed = true
                val i: Int = searchResultList.indexOf(searchResult)
                val cloned = TicketCheckProvider.SearchResult(searchResult)
                searchResultList.remove(searchResult)
                searchResultList.add(i, cloned)
                searchResultListView.selectionModel.select(cloned)
            }

            runAsync {
                controller.triggerSync()
            }
        }
    }

    private fun removeCard(card: VBox) {
        timeline {
            keyframe(MaterialDuration.EXIT) {
                keyvalue(card.translateXProperty(), 480.0, MaterialInterpolator.EXIT)
                keyvalue(card.opacityProperty(), 0.0, MaterialInterpolator.EXIT)
            }
        }.setOnFinished {
            card.removeFromParent()
            resultCards -= card
        }
    }

    private fun showCard(card: VBox) {
        resultHolder += card
        resultCards += card

        timeline {
            keyframe(MaterialDuration.ENTER) {
                keyvalue(card.translateXProperty(), 0.0, MaterialInterpolator.ENTER)
                keyvalue(card.opacityProperty(), 1.0, MaterialInterpolator.ENTER)
            }
        }.setOnFinished {
            mainSpinner.opacity = 0.0
        }

        timeline {
            keyframe(Duration.seconds(15.0)) {
                setOnFinished {
                    removeCard(card)
                }
            }
        }
    }

    private fun showSearchResultCard() {
        if (!searchResultCard.isVisible) {
            searchCardAnimation?.stop()
            searchResultCard.translateY = 200.0
            searchResultCard.opacity = 0.0
            searchResultCard.isVisible = true
            searchCardAnimation = timeline {
                keyframe(MaterialDuration.ENTER) {
                    keyvalue(searchResultCard.opacityProperty(), 1.0, MaterialInterpolator.ENTER)
                    keyvalue(searchResultCard.translateYProperty(), 0.0, MaterialInterpolator.ENTER)
                }
            }
        } else {
            searchResultCard.translateY = 0.0
            searchResultCard.opacity = 1.0
        }
    }

    private fun hideSearchResultCard() {
        searchCardAnimation?.stop()
        startSearchTimeline?.stop()
        if (searchResultCard.isVisible) {
            searchCardAnimation = timeline {
                keyframe(MaterialDuration.EXIT) {
                    keyvalue(searchResultCard.opacityProperty(), 0.0, MaterialInterpolator.EXIT)
                    keyvalue(searchResultCard.translateYProperty(), 200.0, MaterialInterpolator.EXIT)
                }
            }
            searchCardAnimation?.setOnFinished {
                searchResultCard.isVisible = false
            }
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

    private fun handleSearchInput(value: String) {
        for (oldResultCard in resultCards) {
            removeCard(oldResultCard)
        }
        showSpinner()

        var resultData: List<TicketCheckProvider.SearchResult>? = null
        lastSearchQuery = value
        runAsync {
            resultData = controller.handleSearchInput(value)
        } ui {
            if (lastSearchQuery == value) {
                // Prevent race condition
                searchResultList.clear()
                if (resultData != null) {
                    searchResultList.addAll(resultData!!)
                }
                hideSpinner()
                showSearchResultCard()
            }
        }
    }

    private fun handleTicketInput(value: String) {
        for (oldResultCard in resultCards) {
            removeCard(oldResultCard)
        }
        hideSearchResultCard()
        showSpinner()

        searchField.text = ""
        var resultData: TicketCheckProvider.CheckResult? = null
        runAsync {
            resultData = controller.handleScanInput(value)
        } ui {
            hideSpinner()

            val newCard = makeNewCard(resultData)
            showCard(newCard)

            runAsync {
                controller.triggerSync()
            }
        }
    }

    private fun handleInput(value: String) {

        // TODO: Support pretix instances with lower entropy levels
        if (value.matches(Regex("[a-z0-9]{32,}"))) {
            handleTicketInput(value)
        } else {
            handleSearchInput(value)
        }

    }

    private fun makeNewCard(data: TicketCheckProvider.CheckResult?): VBox {
        val vb = VBox()
        with(vb) {
            translateX = -480.0
            opacity = 0.2

            vbox {
                addClass(MainStyleSheet.card)

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

                        if (data?.type == TicketCheckProvider.CheckResult.Type.ERROR) {
                            label(data.message ?: "?")
                        } else {
                            var ticket = data?.ticket ?: ""
                            if (data?.variation != null && data.variation != "null") {
                                ticket += " – " + data.variation
                            }
                            hbox {
                                label(data?.attendee_name ?: "")
                                spacer {}
                                label(data?.orderCode ?: "")
                            }
                            hbox {
                                label(ticket)
                            }
                        }
                    }
                }
                if (data?.isRequireAttention() ?: false) {
                    val attbox = vbox {
                        addClass(MainStyleSheet.cardFooterAttention)
                        addClass(MainStyleSheet.cardBody)
                        label("Attention, special ticket!")
                    }
                    timeline {
                        cycleCount = 10
                        keyframe(Duration.seconds(0.2)) {
                            setOnFinished {
                                attbox.removeClass(MainStyleSheet.cardFooterAttention)
                                attbox.addClass(MainStyleSheet.cardFooterAttentionBlink)
                            }
                        }
                        keyframe(Duration.seconds(0.4)) {
                            setOnFinished {
                                attbox.removeClass(MainStyleSheet.cardFooterAttentionBlink)
                                attbox.addClass(MainStyleSheet.cardFooterAttention)
                            }
                        }
                    }
                }
            }
        }
        return vb
    }

    private fun displaySyncStatus() {
        val closeButton: JFXButton = jfxButton("CLOSE")
        val dialog = jfxDialog(transitionType = JFXDialog.DialogTransition.BOTTOM) {
            setHeading(label("Synchronization status"))
            setBody(label(controller.syncStatusLongText()))
            setActions(closeButton)
        }
        closeButton.action {
            dialog.close()
        }
        dialog.show(root)
    }
}
