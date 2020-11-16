package eu.pretix.pretixscan.desktop.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import eu.pretix.pretixscan.desktop.PretixScanMain
import eu.pretix.pretixscan.desktop.readFromInputStream
import eu.pretix.pretixscan.desktop.ui.helpers.*
import eu.pretix.pretixscan.desktop.ui.style.MainStyleSheet
import eu.pretix.pretixscan.desktop.ui.style.STYLE_BACKGROUND_COLOR
import eu.pretix.pretixscan.desktop.ui.style.STYLE_STATE_VALID_COLOR
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import tornadofx.*
import javax.print.DocFlavor
import javax.print.DocPrintJob
import javax.print.PrintService
import javax.print.ServiceUIFactory
import javax.print.attribute.Attribute
import javax.print.attribute.AttributeSet
import javax.print.attribute.PrintServiceAttribute
import javax.print.attribute.PrintServiceAttributeSet
import javax.print.event.PrintServiceAttributeListener


class FakePrintService : PrintService {
    override fun isDocFlavorSupported(flavor: DocFlavor?): Boolean {
        return false
    }

    override fun getName(): String? {
        return ""
    }

    override fun removePrintServiceAttributeListener(listener: PrintServiceAttributeListener?) {}
    override fun getDefaultAttributeValue(category: Class<out Attribute>?): Any? {
        return null
    }

    override fun getSupportedDocFlavors(): Array<DocFlavor> {
        return emptyArray()
    }

    override fun getAttributes(): PrintServiceAttributeSet? {
        return null
    }

    override fun getServiceUIFactory(): ServiceUIFactory? {
        return null
    }

    override fun isAttributeValueSupported(attrval: Attribute?, flavor: DocFlavor?, attributes: AttributeSet?): Boolean {
        return false
    }

    override fun getSupportedAttributeValues(category: Class<out Attribute>?, flavor: DocFlavor?, attributes: AttributeSet?): Any? {
        return null
    }

    override fun getSupportedAttributeCategories(): Array<Class<*>> {
        return emptyArray()
    }

    override fun <T : PrintServiceAttribute?> getAttribute(category: Class<T>?): T? {
        return null
    }

    override fun addPrintServiceAttributeListener(listener: PrintServiceAttributeListener?) {
    }

    override fun getUnsupportedAttributes(flavor: DocFlavor?, attributes: AttributeSet?): AttributeSet? {
        return null
    }

    override fun createPrintJob(): DocPrintJob? {
        return null
    }

    override fun isAttributeCategorySupported(category: Class<out Attribute>?): Boolean {
        return false
    }

    override fun toString(): String {
        return ""
    }
}

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

    private val printersComboBox = jfxCombobox<PrintService> {
        useMaxWidth = true
        items = FXCollections.observableList(controller.getPrinters().toMutableList())
        items.add(0, FakePrintService())
        for (item in items) {
            if (item.name == controller.getCurrentPrinterName()) {
                selectionModel.select(item)
                break
            }
        }

        valueProperty().onChange {
            controller.setBadgePrinter(it)
        }
    }

    private val printOrientationComboBox = jfxCombobox<String> {
        useMaxWidth = true
        items = FXCollections.observableList(mutableListOf("Auto", "Landscape", "Portrait"))
        for (item in items) {
            if (item == controller.getPrintOrientation()) {
                selectionModel.select(item)
                break
            }
        }

        valueProperty().onChange {
            controller.setPrintOrientation(it!!)
        }
    }

    private val badgesBtn = jfxTogglebutton() {
        toggleColor = c(STYLE_STATE_VALID_COLOR)
        isSelected = !(app as PretixScanMain).configStore.autoPrintBadges
        action {
            controller.toggleAutoPrintBadges(isSelected)
        }
    }

    private val syncOrdersBtn = jfxTogglebutton() {
        toggleColor = c(STYLE_STATE_VALID_COLOR)
        isSelected = !(app as PretixScanMain).configStore.syncOrders
        action {
            controller.toggleSyncOrders(isSelected)
        }
    }

    private val soundBtn = jfxTogglebutton() {
        toggleColor = c(STYLE_STATE_VALID_COLOR)
        isSelected = !(app as PretixScanMain).configStore.playSound
        action {
            controller.toggleSound(isSelected)
        }
    }

    private val largecolorBtn = jfxTogglebutton() {
        toggleColor = c(STYLE_STATE_VALID_COLOR)
        isSelected = !(app as PretixScanMain).configStore.largeColor
        action {
            controller.toggleLargeColor(isSelected)
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
                hbox {
                    style {
                        alignment = Pos.CENTER
                    }
                    label(messages["settings_largecolor"])
                    spacer {}
                    this += largecolorBtn
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
                    label(messages["settings_sync_orders"])
                    spacer {}
                    this += syncOrdersBtn
                }
            }
        }

        vbox {
            addClass(MainStyleSheet.card)
            addClass(MainStyleSheet.eventSettingsCard)
            vbox {
                addClass(MainStyleSheet.cardBody)
                style {
                    padding = box(15.px, 15.px)
                }
                hbox {
                    style {
                        alignment = Pos.CENTER
                    }
                    label(messages["settings_autoprint_badges"])
                    spacer {}
                    this += badgesBtn
                }
                hbox {
                    style {
                        alignment = Pos.CENTER
                    }
                    label(messages["settings_printers_badge"]) {
                        hboxConstraints {
                            hGrow = Priority.ALWAYS
                        }
                    }
                    spacer {}
                    this += printersComboBox
                    printersComboBox.hboxConstraints {
                        hGrow = Priority.ALWAYS
                    }
                }
                hbox {
                    style {
                        alignment = Pos.CENTER
                    }
                    label(messages["settings_printers_orientation"]) {
                        hboxConstraints {
                            hGrow = Priority.ALWAYS
                        }
                    }
                    spacer {}
                    this += printOrientationComboBox
                    printOrientationComboBox.hboxConstraints {
                        hGrow = Priority.ALWAYS
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
                                    jfxTextarea(readFromInputStream(PretixScanMain::class.java.getResourceAsStream("licenses.txt"))) {
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
        if (!(app as PretixScanMain).configStore.isConfigured()) {
            replaceWith(SetupView::class, MaterialSlide(ViewTransition.Direction.DOWN))
        }
        soundBtn.isSelected = (app as PretixScanMain).configStore.playSound
        syncOrdersBtn.isSelected = (app as PretixScanMain).configStore.syncOrders
        badgesBtn.isSelected = (app as PretixScanMain).configStore.autoPrintBadges
        largecolorBtn.isSelected = (app as PretixScanMain).configStore.largeColor
        currentWindow?.setOnCloseRequest {
            controller.close()
        }
    }

    init {
        title = messages["title"]
    }
}