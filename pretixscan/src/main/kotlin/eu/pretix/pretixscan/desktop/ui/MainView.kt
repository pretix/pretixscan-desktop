package eu.pretix.pretixscan.desktop.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.pretixscan.desktop.PretixScanMain
import eu.pretix.pretixscan.desktop.getBadgeLayout
import eu.pretix.pretixscan.desktop.printBadge
import eu.pretix.pretixscan.desktop.ui.helpers.*
import eu.pretix.pretixscan.desktop.ui.style.*
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.control.ComboBoxBase
import javafx.scene.control.TextInputControl
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import tornadofx.*
import java.awt.Desktop
import java.net.URI
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.concurrent.CompletableFuture.runAsync
import java.util.regex.Pattern
import javax.sound.sampled.AudioSystem


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

    private val infoButton = jfxButton(messages["toolbar_info"]) {
        action {
            replaceWith(StatusView::class, MaterialSlide(ViewTransition.Direction.LEFT))
        }
    }

    private val searchField = textfield {
        promptText = messages["searchfield_prompt"]
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
                        keyframe(Duration.seconds(.2)) {
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
                    style {
                        maxWidth = 430.px
                    }

                    var ticketname = it.ticket
                    if (it.variation != null && it.variation != "null") {
                        ticketname += " – " + it.variation
                    }
                    if (it.isRequireAttention) {
                        imageview(Image(PretixScanMain::class.java.getResourceAsStream("icons/alert.png"))) {
                            fitWidth = 18.0
                            fitHeight = 18.0
                            translateY = 3.0
                        }
                    }

                    label(ticketname) {
                        addClass(MainStyleSheet.searchItemProduct)
                        isWrapText = true
                        vgrow = Priority.ALWAYS
                        style {
                            maxWidth = 330.px
                        }
                    }
                    spacer {}
                    if (it.isRedeemed) {
                        label(messages["searchresult_state_redeemed"]) {
                            addClass(MainStyleSheet.searchItemStatusRedeemed)
                            hgrow = Priority.NEVER
                        }
                    } else if (!it.isPaid) {
                        label(messages["searchresult_state_unpaid"]) {
                            addClass(MainStyleSheet.searchItemStatusUnpaid)
                            hgrow = Priority.NEVER
                        }
                    } else {
                        label(messages["searchresult_state_valid"]) {
                            addClass(MainStyleSheet.searchItemStatusValid)
                            hgrow = Priority.NEVER
                        }
                    }
                }
                hbox {
                    label(it.orderCode + "  ") { addClass(MainStyleSheet.searchItemOrderCode) }
                    label(it.attendee_name ?: "") {
                        addClass(MainStyleSheet.searchItemAttendeeName)
                        isWrapText = true
                    }
                }
                /* TODO:
                if (it.addonText != "") {
                    hbox {
                        label("+ " + it.addonText) { addClass(MainStyleSheet.searchItemAttendeeName) }
                    }
                }
                */
            }
        }
        cellFormat {
        }
        placeholder = label(messages["search_no_result"])
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
            if (it.clickCount == 2 && searchResultListView.selectionModel.selectedItem != null) {
                handleSearchResultSelected(searchResultListView.selectionModel.selectedItem)
                it.consume()
            }
        }
        searchResultListView.setOnKeyReleased {
            if (it.code == KeyCode.ENTER && searchResultListView.selectionModel.selectedItem != null) {
                handleSearchResultSelected(searchResultListView.selectionModel.selectedItem)
                it.consume()
            }
        }
    }

    private val resultHolder = stackpane {
        addClass(eu.pretix.pretixscan.desktop.ui.style.MainStyleSheet.resultHolder)

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
            imageview(Image(PretixScanMain::class.java.getResourceAsStream("logo.png")))
        }

        this += searchField
        this += resultHolder
    }

    private val syncStatusLabel = jfxButton("") {
        action {
            displaySyncStatus(controller, root)
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
            gridpane {
                addClass(MainStyleSheet.toolBar)
                style {
                    minWidth = 100.percent
                }
                row {
                    hbox {
                        gridpaneColumnConstraints { percentWidth = 33.33 }
                        style {
                            alignment = Pos.CENTER_LEFT
                        }
                        jfxTogglebutton(messages["toolbar_toggle_async"]) {
                            toggleColor = c(STYLE_STATE_VALID_COLOR)
                            isSelected = !(app as PretixScanMain).configStore.asyncModeEnabled
                            action {
                                controller.toggleAsync(!isSelected)
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
                        this += infoButton
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

    private fun beep() {
        if (!controller.soundEnabled()) {
            return
        }
        try {
            val clip = AudioSystem.getClip()
            val inputStream = AudioSystem.getAudioInputStream(PretixScanMain::class.java.getResourceAsStream("beep.wav"))
            clip.open(inputStream)
            clip.start()
        } catch (e: Exception) {
            e.printStackTrace()
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
        currentWindow?.setOnCloseRequest {
            controller.close()
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

        // Focus grabber
        currentStage?.addEventFilter(KeyEvent.KEY_PRESSED, {
            val fo = currentStage?.scene?.focusOwner
            if (fo !is TextInputControl && fo !is ComboBoxBase<*> && re_alphanum.matcher(it.text).matches()) {
                searchField.requestFocus()
            }
        })

        timeline {
            keyframe(Duration.seconds(0.1)) {
                setOnFinished {
                    runAsync {
                        controller.updateCheck()
                    } ui {
                        if (controller.updateCheckNewerVersion().length > 1) {
                            val closeButton: JFXButton = jfxButton(messages.getString("dialog_close"))
                            val downloadButton: JFXButton = jfxButton(messages.getString("update_download").toUpperCase())
                            val dialog = jfxDialog(transitionType = JFXDialog.DialogTransition.BOTTOM) {
                                setBody(label(messages.getString("update_available").replace("{0}", controller.updateCheckNewerVersion())))
                                setHeading(label(messages.getString("update_head")))
                                setActions(downloadButton, closeButton)
                            }
                            closeButton.action {
                                dialog.close()
                            }
                            downloadButton.action {
                                runAsync {
                                    Desktop.getDesktop().browse(URI("https://pretix.eu/about/en/desk"));
                                } ui {
                                    dialog.close()
                                }
                            }
                            dialog.show(root)
                        }
                    }
                }
            }
        }
    }

    private fun handleSearchResultSelected(searchResult: TicketCheckProvider.SearchResult, answers: List<TicketCheckProvider.Answer>? = null, ignore_pending: Boolean = false) {
        hideSearchResultCard()
        handleTicketInput(searchResult.secret, answers, ignore_pending)
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
            resultData = controller.handleSearchInput(value.trim())
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

    private fun handleTicketInput(value: String, answers: List<TicketCheckProvider.Answer>? = null, ignore_pending: Boolean = false) {
        for (oldResultCard in resultCards) {
            removeCard(oldResultCard)
        }
        hideSearchResultCard()
        showSpinner()

        searchField.text = ""
        var resultData: TicketCheckProvider.CheckResult? = null
        runAsync {
            resultData = controller.handleScanInput(value, answers, ignore_pending)
        } ui {
            hideSpinner()
            hideSearchResultCard()

            val newCard = makeNewCard(resultData)
            showCard(newCard)
            if (resultData?.type == TicketCheckProvider.CheckResult.Type.VALID) {
                beep()
                if (resultData?.position != null && (app as PretixScanMain).configStore.badgePrinterName != null && (app as PretixScanMain).configStore.autoPrintBadges) {
                    runAsync {
                        printBadge(app as PretixScanMain, resultData!!.position!!)
                    }
                }
            }

            if (resultData?.type == TicketCheckProvider.CheckResult.Type.ANSWERS_REQUIRED) {
                val dialog = questionsDialog(resultData!!.requiredAnswers) { a ->
                    handleTicketInput(value, a, ignore_pending)
                }
                dialog.show(root)
            }

            if (resultData?.type == TicketCheckProvider.CheckResult.Type.UNPAID && resultData?.isCheckinAllowed == true) {
                val dialog = unpaidOrderDialog { new_ignore_pending ->
                    handleTicketInput(value, answers, new_ignore_pending)
                }
                dialog.show(root)
            }

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
                        TicketCheckProvider.CheckResult.Type.ANSWERS_REQUIRED -> MainStyleSheet.cardHeaderRepeat
                        TicketCheckProvider.CheckResult.Type.ERROR -> MainStyleSheet.cardHeaderError
                        TicketCheckProvider.CheckResult.Type.UNPAID -> MainStyleSheet.cardHeaderError
                        TicketCheckProvider.CheckResult.Type.PRODUCT -> MainStyleSheet.cardHeaderError
                        null -> MainStyleSheet.cardHeaderError
                    })

                    val headline = when (data?.type) {
                        TicketCheckProvider.CheckResult.Type.INVALID -> messages["state_invalid"]
                        TicketCheckProvider.CheckResult.Type.VALID -> messages["state_valid"]
                        TicketCheckProvider.CheckResult.Type.USED -> messages["state_used"]
                        TicketCheckProvider.CheckResult.Type.ANSWERS_REQUIRED -> messages["state_questions"]
                        TicketCheckProvider.CheckResult.Type.ERROR -> messages["state_error"]
                        TicketCheckProvider.CheckResult.Type.UNPAID -> messages["state_unpaid"]
                        TicketCheckProvider.CheckResult.Type.PRODUCT -> messages["state_product"]
                        null -> messages["state_unknown"]
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
                                label(data?.attendee_name ?: "") {
                                    isWrapText = true
                                }
                                spacer {}
                                label(data?.orderCode ?: "")
                            }
                            hbox {
                                label(ticket) {
                                    isWrapText = true
                                }
                            }
                            if (data?.position != null) {
                                val t = data?.position.getJSONObject("pdf_data").getString("addons").replace("<br/>", ", ")
                                if (t.isNotEmpty()) {
                                    hbox {
                                        label("+ " + t) {
                                            isWrapText = true
                                        }
                                    }
                                }
                            }
                            if (data?.firstScanned != null) {
                                val df = SimpleDateFormat(messages.getString("short_datetime_format"))
                                label(
                                        MessageFormat.format(
                                                messages.getString("first_scanned"),
                                                df.format(data.firstScanned)
                                        )
                                )
                            }
                        }
                        val offer_print = (
                                data?.position != null
                                        && (app as PretixScanMain).configStore.badgePrinterName != null
                                        && (data.type == TicketCheckProvider.CheckResult.Type.VALID
                                            || data.type == TicketCheckProvider.CheckResult.Type.USED)
                                        && getBadgeLayout(app as PretixScanMain, data.position) != null
                                )
                        if (offer_print) {
                            jfxButton(messages["button_reprint_badge"]) {
                                style {
                                    buttonType = JFXButton.ButtonType.RAISED
                                    textFill = c(STYLE_TOOLBAR_TEXT_COLOR)
                                    backgroundColor += c(STYLE_PRIMARY_DARK_COLOR)
                                    alignment = Pos.CENTER_RIGHT
                                }
                                setOnMouseClicked {
                                    runAsync {
                                        printBadge(app as PretixScanMain, data!!.position)
                                    }
                                }
                            }
                        }
                    }
                }
                if (data?.isRequireAttention() ?: false) {
                    val attbox = vbox {
                        addClass(MainStyleSheet.cardFooterAttention)
                        addClass(MainStyleSheet.cardBody)
                        label(messages["special_ticket"])
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
}
