package eu.pretix.pretixscan.desktop.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXToggleButton
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.libpretixsync.db.CheckInList
import eu.pretix.libpretixsync.db.Event
import eu.pretix.libpretixsync.db.SubEvent
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
import java.lang.Integer.min
import java.net.URI
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import javax.sound.sampled.AudioSystem


var re_alphanum = Pattern.compile("^[a-zA-Z0-9]+\$")

class MainView : View() {
    private val controller: MainController by inject()
    private var resultCards = mutableListOf<VBox>()
    private var spinnerAnimation: Timeline? = null
    private var searchCardAnimation: Timeline? = null
    private var syncStatusTimeline: Timeline? = null
    private var syncTriggerTimeline: Timeline? = null
    private var startSearchTimeline: Timeline? = null
    private var hideSearchTimeline: Timeline? = null
    private var revertBackgroundTimeline: Timeline? = null
    private var lastSearchQuery: String? = null
    private var selectedSearchResult: TicketCheckProvider.SearchResult? = null

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
                    handleSearchInput("")
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

                label(it.secret!!.substring(0, min(it.secret!!.length, 20)) + "…")
                hbox {
                    style {
                        maxWidth = 630.px
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

                    label(ticketname ?: "") {
                        addClass(MainStyleSheet.searchItemProduct)
                        isWrapText = true
                        vgrow = Priority.ALWAYS
                        style {
                            maxWidth = 530.px
                        }
                    }
                    spacer {}
                    if (it.isRedeemed) {
                        label(messages["searchresult_state_redeemed"]) {
                            addClass(MainStyleSheet.searchItemStatusRedeemed)
                            hgrow = Priority.NEVER
                        }
                    } else if (it.status == TicketCheckProvider.SearchResult.Status.PENDING) {
                        label(messages["searchresult_state_unpaid"]) {
                            addClass(MainStyleSheet.searchItemStatusUnpaid)
                            hgrow = Priority.NEVER
                        }
                    } else if (it.status == TicketCheckProvider.SearchResult.Status.CANCELED) {
                        label(messages["searchresult_state_canceled"]) {
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
                    label(if (it.attendee_name != "null") it.attendee_name ?: "" else "") {
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
        addClass(eu.pretix.pretixscan.desktop.ui.style.MainStyleSheet.resultHolder)
        opacity = 0.0
        isVisible = false
        isManaged = false
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
                renewSearchResultLifetime()
                handleSearchResultSelected(searchResultListView.selectionModel.selectedItem)
                it.consume()
            }
        }
        searchResultListView.setOnKeyReleased {
            if (it.code == KeyCode.ENTER && searchResultListView.selectionModel.selectedItem != null) {
                renewSearchResultLifetime()
                handleSearchResultSelected(searchResultListView.selectionModel.selectedItem)
                it.consume()
            }
        }
    }

    private val confdetailLabel = label() {
        isWrapText = true
    }

    private val resultHolder = stackpane {
        addClass(eu.pretix.pretixscan.desktop.ui.style.MainStyleSheet.resultHolder)

        vbox {
            this += mainSpinner
        }
    }

    private val contentBox = vbox {
        useMaxHeight = true

        style {
            alignment = Pos.CENTER
            spacing = 20.px
        }
        if (STYLE_BACKGROUND_IMAGE == null) {
            addClass(MainStyleSheet.bgDefault)
        }

        hbox {
            style {
                paddingBottom = 10.0
                alignment = Pos.CENTER
            }
            addClass(eu.pretix.pretixscan.desktop.ui.style.MainStyleSheet.logoHolder)
            imageview(Image(PretixScanMain::class.java.getResourceAsStream("logo.png"))) {
                fitHeight = 100.0
                fitWidth = 500.0
                isPreserveRatio = true
            }
            spacer {}
            this += confdetailLabel
        }

        this += searchField
        this += searchResultCard
        this += resultHolder
    }

    private val syncStatusLabel = jfxButton("") {
        action {
            displaySyncStatus(controller, root)
        }
    }

    private val eventNameLabel = label("Event name")

    var toggleExit: JFXToggleButton? = null
    val rootBox = vbox {
        useMaxHeight = true

        style {
            alignment = Pos.CENTER
            if (STYLE_BACKGROUND_IMAGE != null) {
                backgroundImage += URI(STYLE_BACKGROUND_IMAGE)
            }
            spacing = 20.px
        }
        if (STYLE_BACKGROUND_IMAGE == null) {
            addClass(MainStyleSheet.bgDefault)
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
                    jfxButton(messages["toolbar_switch"]) {
                        action {
                            replaceWith(SelectEventView::class, MaterialSlide(ViewTransition.Direction.DOWN))
                        }
                    }
                }
                hbox {
                    gridpaneColumnConstraints { percentWidth = 33.33 }
                    style {
                        alignment = Pos.CENTER_RIGHT
                    }
                    label(messages["toolbar_toggle_entry"])
                    val conf = (app as PretixScanMain).configStore
                    toggleExit = jfxTogglebutton(messages["toolbar_toggle_exit"]) {
                        toggleColor = c(STYLE_STATE_VALID_COLOR)
                        isSelected = conf.scanType == "exit"
                        isDisable = conf.knownPretixVersion < 30090001000
                        action {
                            if (conf.scanType == "exit") {
                                conf.scanType = "entry"
                            } else {
                                conf.scanType = "exit"
                            }
                            toggleExit?.isSelected = conf.scanType == "exit"
                        }
                    }
                    this += toggleExit!!
                }
            }
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
                        val conf = (app as PretixScanMain).configStore
                        toggleColor = c(STYLE_STATE_VALID_COLOR)
                        isSelected = !conf.asyncModeEnabled
                        isDisable = conf.proxyMode
                        action {
                            controller.toggleAsync(!isSelected)
                            infoButton.isVisible = !(!conf.syncOrders && conf.asyncModeEnabled)
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
    override val root: StackPane = stackpane {
        this += rootBox
    }

    private fun beep(sound: String) {
        if (!controller.soundEnabled()) {
            return
        }
        try {
            val clip = AudioSystem.getClip()
            val inputStream = AudioSystem.getAudioInputStream(PretixScanMain::class.java.getResourceAsStream(sound + ".wav"))
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
        eventNameLabel.text = conf.eventName + ": " + conf.checkInListName
        currentWindow?.setOnCloseRequest {
            controller.close()
        }
        confdetailLabel.text = getConfDetails()
        infoButton.isVisible = !(!conf.syncOrders && conf.asyncModeEnabled)
    }

    fun getConfDetails(): String {
        var confdetails = ""
        val conf = (app as PretixScanMain).configStore
        if (!conf.eventSlug.isNullOrBlank()) {
            val event = (app as PretixScanMain).data().select(Event::class.java)
                    .where(Event.SLUG.eq(conf.eventSlug))
                    .get().firstOrNull()
            if (event != null) {
                confdetails += MessageFormat.format(messages["debug_info_event"], event.name)
                if (conf.subEventId != null && conf.subEventId!! > 0) {
                    val subevent = (app as PretixScanMain).data().select(SubEvent::class.java)
                            .where(SubEvent.SERVER_ID.eq(conf.subEventId))
                            .get().firstOrNull()
                    if (subevent != null) {
                        confdetails += "\n"
                        val df = SimpleDateFormat(messages["short_datetime_format"])
                        confdetails += MessageFormat.format(messages["debug_info_subevent"], subevent.name, df.format(subevent.date_from))
                    }
                }

                if (conf.checkInListId > 0) {
                    val cl = (app as PretixScanMain).data().select(CheckInList::class.java)
                            .where(CheckInList.SERVER_ID.eq(conf.checkInListId))
                            .get().firstOrNull()
                    if (cl != null) {
                        confdetails += "\n"
                        confdetails += MessageFormat.format(messages["debug_info_list"], cl.name)
                    }
                }

                if (!conf.deviceKnownGateName.isBlank()) {
                    confdetails += "\n"
                    confdetails += MessageFormat.format(messages["debug_info_gate"], conf.deviceKnownGateName)
                }
            }
            confdetails += "\n"
            confdetails += MessageFormat.format(messages["debug_info_device"], conf.deviceKnownName)
        }
        return confdetails
    }

    init {
        title = messages["title"]

        syncStatusTimeline = timeline {
            cycleCount = Timeline.INDEFINITE

            keyframe(Duration.seconds(.5)) {
                setOnFinished {
                    var text = "?"
                    var confDetails = "?"
                    runAsync {
                        text = controller.syncStatusText()
                        confDetails = getConfDetails()
                    } ui {
                        syncStatusLabel.text = text
                        confdetailLabel.text = confDetails
                    }
                }
            }
        }

        syncTriggerTimeline = timeline {
            cycleCount = Timeline.INDEFINITE

            keyframe(Duration.seconds(10.0)) {
                setOnFinished {
                    runAsync {
                        controller.triggerSync(eh = { e ->
                            ui {
                                val conf = (app as PretixScanMain).configStore
                                conf.eventSlug = e.eventSlug
                                conf.subEventId = e.subeventId
                                conf.eventName = e.eventName
                                conf.checkInListId = e.checkinlistId
                                (app as PretixScanMain).reloadCheckProvider()
                                onDock()
                                foregroundSync(controller, root)
                            }
                        })
                    }
                }
            }
        }

        // Focus grabber
        currentStage?.addEventFilter(KeyEvent.KEY_PRESSED, {
            val fo = currentStage?.scene?.focusOwner
            try {
                if (fo !is TextInputControl && fo !is ComboBoxBase<*> && re_alphanum.matcher(it.text).matches()) {
                    searchField.requestFocus()
                }
            } catch (e: NoClassDefFoundError) {
                // No idea why, but we do not care
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
                                    Desktop.getDesktop().browse(URI("https://pretix.eu/about/en/scan"));
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

    private fun handleSearchResultSelected(searchResult: TicketCheckProvider.SearchResult, answers: List<Answer>? = null, ignore_pending: Boolean = false) {
        selectedSearchResult = searchResult
        handleTicketInput(searchResult.secret!!, answers, ignore_pending)
    }

    private fun removeCard(card: VBox) {
        timeline {
            keyframe(MaterialDuration.EXIT) {
                keyvalue(card.translateXProperty(), 480.0, MaterialInterpolator.EXIT)
                keyvalue(card.opacityProperty(), 0.0, MaterialInterpolator.EXIT)
            }
        }.setOnFinished {
            card.removeFromParent()
            resultCards.remove(card)
        }
    }

    private fun showCard(card: VBox) {
        resultHolder += card
        resultCards.add(card)

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

    private fun renewSearchResultLifetime() {
        hideSearchTimeline?.stop()
        hideSearchTimeline = timeline {
            keyframe(Duration.seconds(30.0)) {
                setOnFinished {
                    hideSearchResultCard()
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
            searchResultCard.isManaged = true
            searchCardAnimation = timeline {
                keyframe(MaterialDuration.ENTER) {
                    keyvalue(searchResultCard.opacityProperty(), 1.0, MaterialInterpolator.ENTER)
                    keyvalue(searchResultCard.translateYProperty(), 0.0, MaterialInterpolator.ENTER)
                }
            }
            renewSearchResultLifetime()
        } else {
            searchResultCard.translateY = 0.0
            searchResultCard.opacity = 1.0
        }
    }

    private fun hideSearchResultCard() {
        searchCardAnimation?.stop()
        startSearchTimeline?.stop()
        hideSearchTimeline?.stop()
        if (searchResultCard.isVisible) {
            searchCardAnimation = timeline {
                keyframe(MaterialDuration.EXIT) {
                    keyvalue(searchResultCard.opacityProperty(), 0.0, MaterialInterpolator.EXIT)
                    keyvalue(searchResultCard.translateYProperty(), 200.0, MaterialInterpolator.EXIT)
                }
            }
            searchCardAnimation?.setOnFinished {
                searchResultCard.isVisible = false
                searchResultCard.isManaged = false
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
        if (value.isEmpty()) {
            hideSearchResultCard()
            return
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

    private fun revertBackground() {
        contentBox.removeClass("bg-valid").removeClass("bg-invalid").removeClass("bg-repeat").removeClass("bg-attention")
        rootBox.removeClass("bg-valid").removeClass("bg-invalid").removeClass("bg-repeat").removeClass("bg-attention")
    }

    private fun showFlash(t: TicketCheckProvider.CheckResult.Type, attention: Boolean) {
        if (revertBackgroundTimeline != null) {
            revertBackgroundTimeline!!.stop()
        }
        var c = when (t) {
            TicketCheckProvider.CheckResult.Type.VALID -> MainStyleSheet.bgValid
            TicketCheckProvider.CheckResult.Type.USED -> MainStyleSheet.bgRepeat
            TicketCheckProvider.CheckResult.Type.ANSWERS_REQUIRED -> MainStyleSheet.bgRepeat
            else -> MainStyleSheet.bgInvalid
        }
        if (t == TicketCheckProvider.CheckResult.Type.VALID && attention) {
            c = MainStyleSheet.bgAttention
        }
        contentBox.addClass(c)
        rootBox.addClass(c)
        revertBackgroundTimeline = timeline {
            keyframe(Duration.seconds(5.0)) {
                setOnFinished {
                    revertBackground()
                }
            }
        }
    }

    private fun handleTicketInput(value: String, answers: List<Answer>? = null, ignore_pending: Boolean = false) {
        for (oldResultCard in resultCards) {
            removeCard(oldResultCard)
        }
        showSpinner()

        searchField.text = ""
        var resultData: TicketCheckProvider.CheckResult? = null
        runAsync {
            resultData = controller.handleScanInput(
                    value,
                    answers,
                    ignore_pending,
                    TicketCheckProvider.CheckInType.valueOf((app as PretixScanMain).configStore.scanType.toUpperCase())
            )
        } ui {
            hideSpinner()

            val newCard = makeNewCard(resultData)
            showCard(newCard)
            if (controller.largeColorEnabled() && resultData != null) {
                showFlash(resultData!!.type!!, resultData!!.isRequireAttention)
            }
            if (selectedSearchResult != null && selectedSearchResult?.orderCode == resultData?.orderCode) {
                if (resultData?.type == TicketCheckProvider.CheckResult.Type.VALID || resultData?.type == TicketCheckProvider.CheckResult.Type.USED) {
                    val index = searchResultList.indexOf(selectedSearchResult)
                    if (index >= 0) {
                        searchResultList.remove(selectedSearchResult)
                        selectedSearchResult = TicketCheckProvider.SearchResult(selectedSearchResult!!)
                        selectedSearchResult?.isRedeemed = true
                        searchResultList.add(index, selectedSearchResult)
                        searchResultListView.selectionModel.select(selectedSearchResult)
                        searchResultListView.refresh()
                    }
                }
            }
            if (resultData?.type == TicketCheckProvider.CheckResult.Type.VALID) {
                beep(when {
                    resultData?.scanType == TicketCheckProvider.CheckInType.EXIT -> "exit"
                    else -> if (resultData?.isRequireAttention == true) {
                        "attention"
                    } else {
                        "enter"
                    }
                })
                if (resultData?.scanType != TicketCheckProvider.CheckInType.EXIT) {
                    if (resultData?.position != null && (app as PretixScanMain).configStore.badgePrinterName != null && (app as PretixScanMain).configStore.autoPrintBadges) {
                        runAsync {
                            printBadge(app as PretixScanMain, resultData!!.position!!, (app as PretixScanMain).configStore.eventSlug!!)
                        }
                    }
                }
            } else if (resultData?.type == TicketCheckProvider.CheckResult.Type.ANSWERS_REQUIRED) {
                val dialog = questionsDialog(resultData!!.requiredAnswers!!) { a ->
                    handleTicketInput(value, a, ignore_pending)
                }
                dialog.show(root)
            } else if (resultData?.type == TicketCheckProvider.CheckResult.Type.UNPAID && resultData?.isCheckinAllowed == true) {
                val dialog = unpaidOrderDialog { new_ignore_pending ->
                    handleTicketInput(value, answers, new_ignore_pending)
                }
                dialog.show(root)
            } else {
                beep("error")
            }

            runAsync {
                controller.triggerSync({})
            }
        }
    }

    private fun handleInput(value: String) {
        // TODO: Support pretix instances with lower entropy levels
        revertBackground()
        if (value.matches(Regex("[a-zA-Z0-9=+/]{12,}"))) {
            hideSearchResultCard()
            selectedSearchResult = null
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
                        TicketCheckProvider.CheckResult.Type.CANCELED -> MainStyleSheet.cardHeaderError
                        TicketCheckProvider.CheckResult.Type.RULES -> MainStyleSheet.cardHeaderError
                        TicketCheckProvider.CheckResult.Type.AMBIGUOUS -> MainStyleSheet.cardHeaderError
                        TicketCheckProvider.CheckResult.Type.REVOKED -> MainStyleSheet.cardHeaderError
                        TicketCheckProvider.CheckResult.Type.UNAPPROVED -> MainStyleSheet.cardHeaderError
                        TicketCheckProvider.CheckResult.Type.BLOCKED -> MainStyleSheet.cardHeaderError
                        TicketCheckProvider.CheckResult.Type.INVALID_TIME -> MainStyleSheet.cardHeaderError
                        null -> MainStyleSheet.cardHeaderError
                    })

                    val headline = when (data?.type) {
                        TicketCheckProvider.CheckResult.Type.INVALID -> messages["state_invalid"]
                        TicketCheckProvider.CheckResult.Type.VALID -> {
                            if (data?.scanType == TicketCheckProvider.CheckInType.EXIT) {
                                messages["state_valid_exit"]
                            } else {
                                messages["state_valid"]
                            }
                        }
                        TicketCheckProvider.CheckResult.Type.USED -> messages["state_used"]
                        TicketCheckProvider.CheckResult.Type.ANSWERS_REQUIRED -> messages["state_questions"]
                        TicketCheckProvider.CheckResult.Type.ERROR -> messages["state_error"]
                        TicketCheckProvider.CheckResult.Type.UNPAID -> messages["state_unpaid"]
                        TicketCheckProvider.CheckResult.Type.INVALID_TIME -> messages["state_invalid_time"]
                        TicketCheckProvider.CheckResult.Type.BLOCKED -> messages["state_blocked"]
                        TicketCheckProvider.CheckResult.Type.CANCELED -> messages["state_canceled"]
                        TicketCheckProvider.CheckResult.Type.PRODUCT -> messages["state_product"]
                        TicketCheckProvider.CheckResult.Type.RULES -> messages["state_rules"]
                        TicketCheckProvider.CheckResult.Type.AMBIGUOUS -> messages["state_ambiguous"]
                        TicketCheckProvider.CheckResult.Type.REVOKED -> messages["state_revoked"]
                        TicketCheckProvider.CheckResult.Type.UNAPPROVED -> messages["state_unapproved"]
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
                                label(if (data?.attendee_name != "null") data?.attendee_name ?: "" else "") {
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
                            if (data?.seat != null) {
                                hbox {
                                    label (data?.seat!!) {
                                        isWrapText = true
                                    }
                                }
                            }
                            if (data?.position != null && data?.position?.has("pdf_data") == true) {
                                val t = data?.position?.optJSONObject("pdf_data")?.optString("addons")?.replace("<br/>", ", ")
                                if (t?.isNotEmpty() == true) {
                                    hbox {
                                        label("+ $t") {
                                            isWrapText = true
                                        }
                                    }
                                }
                            }
                            if (!data?.reasonExplanation.isNullOrBlank()) {
                                label(data?.reasonExplanation!!)
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
                                        && getBadgeLayout(app as PretixScanMain, data.position!!, (app as PretixScanMain).configStore.eventSlug!!) != null
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
                                        printBadge(app as PretixScanMain, data!!.position!!, (app as PretixScanMain).configStore.eventSlug!!)
                                    }
                                }
                            }
                        }
                    }
                }
                if (data?.isRequireAttention ?: false) {
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
