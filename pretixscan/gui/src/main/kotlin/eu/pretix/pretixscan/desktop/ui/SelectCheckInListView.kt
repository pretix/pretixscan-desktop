package eu.pretix.pretixscan.desktop.ui

import eu.pretix.libpretixsync.db.CheckInList
import eu.pretix.libpretixsync.sync.SyncManager
import eu.pretix.pretixscan.desktop.PretixScanMain
import eu.pretix.pretixscan.desktop.ui.helpers.*
import eu.pretix.pretixscan.desktop.ui.style.MainStyleSheet
import eu.pretix.pretixscan.desktop.ui.style.STYLE_BACKGROUND_COLOR
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import tornadofx.*


class SelectCheckInListView : View() {
    private val controller: SelectCheckInListController by inject()
    private val listList = ArrayList<CheckInList>().observable()
    private var spinnerAnimation: Timeline? = null

    private val listListView = jfxListview(listList) {
        vboxConstraints { vGrow = Priority.ALWAYS }

        cellCache {
            vbox {
                label(it.name)
            }
        }
        cellFormat {
        }
        placeholder = label(messages["no_checkinlist"])
    }

    private fun showSpinner() {
        listListView.hide()
        spinnerAnimation?.stop()
        statusText.text = ""
        spinnerAnimation = timeline {
            keyframe(MaterialDuration.ENTER) {
                keyvalue(mainSpinner.opacityProperty(), 1.0, MaterialInterpolator.ENTER)
                keyvalue(statusText.opacityProperty(), 1.0, MaterialInterpolator.ENTER)
            }
        }
    }

    private fun hideSpinner() {
        spinnerAnimation?.stop()
        spinnerAnimation = timeline {
            keyframe(MaterialDuration.EXIT) {
                keyvalue(mainSpinner.opacityProperty(), 0.0, MaterialInterpolator.EXIT)
                keyvalue(statusText.opacityProperty(), 0.0, MaterialInterpolator.EXIT)
            }
        }
        listListView.show()
    }

    private val mainSpinner = jfxSpinner {
        useMaxHeight = false
        useMaxWidth = false
        opacity = 0.0
        maxWidth = 64.0
        maxHeight = 64.0
    }

    private val statusText = text { "…" }

    private val contentBox = vbox {
        vboxConstraints { vGrow = Priority.ALWAYS }
        useMaxHeight = true

        style {
            alignment = Pos.CENTER
            backgroundColor += c(STYLE_BACKGROUND_COLOR)
            spacing = 10.px
        }

        label(messages["select_list"])
        hbox {
            addClass(MainStyleSheet.card)
            addClass(MainStyleSheet.selectHolder)
            hboxConstraints { hGrow = Priority.ALWAYS }
            stackpane {
                hboxConstraints { hGrow = Priority.ALWAYS }
                vbox {
                    this += mainSpinner
                    this += statusText
                    style {
                        alignment = Pos.CENTER
                        spacing = 10.px
                    }
                }
                this += listListView
            }

            listListView.setOnMouseClicked {
                if (it.clickCount == 2 && listListView.selectionModel.selectedItem != null) {
                    handleListSelected(listListView.selectionModel.selectedItem)
                    it.consume()
                }
            }
            listListView.setOnKeyReleased {
                if (it.code == KeyCode.ENTER && listListView.selectionModel.selectedItem != null) {
                    handleListSelected(listListView.selectionModel.selectedItem)
                    it.consume()
                }
            }
        }
    }

    fun handleListSelected(list: CheckInList) {
        controller.setList(list)
        replaceWith(MainView::class, MaterialSlide(ViewTransition.Direction.UP))
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
                        jfxButton(messages["settings_reset_button"].toUpperCase()) {
                            action {
                                runAsync {
                                    SettingsController().resetApp()
                                    ui {
                                        replaceWith(SetupView::class, MaterialSlide(ViewTransition.Direction.DOWN))
                                    }
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
                        vbox {
                            hgrow = Priority.ALWAYS
                        }
                        jfxButton(messages["toolbar_refresh"]) {
                            action {
                                loadLists()
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
        listListView.hide()
        loadLists()

        currentWindow?.setOnCloseRequest {
            controller.close()
        }
    }



    fun loadLists() {
        showSpinner()
        var lists = emptyList<CheckInList>()
        runAsync {
            controller.triggerMinimalDownload(SyncManager.ProgressFeedback { statusText.text = it })
            lists = controller.getAllLists()
        } ui {
            listList.clear()
            if (lists.size > 0) {
                listList.addAll(lists)
            }
            hideSpinner()
        }
    }

    init {
        title = messages["title"]
    }
}
