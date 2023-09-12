package eu.pretix.pretixscan.desktop.ui.helpers

import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamException
import com.github.sarxos.webcam.WebcamResolution
import com.github.sarxos.webcam.util.ImageUtils
import com.jfoenix.controls.*
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.libpretixsync.db.Question
import eu.pretix.libpretixsync.db.QuestionLike
import eu.pretix.libpretixsync.db.QuestionOption
import eu.pretix.pretixscan.desktop.PretixScanMain
import eu.pretix.pretixscan.desktop.ui.style.MainStyleSheet
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max


val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

class DateTimeFieldCombo(datefield: JFXDatePicker, timefield: JFXTimePicker) {
    var datefield: JFXDatePicker? = datefield
    var timefield: JFXTimePicker? = timefield

}


fun TextInputControl.stripNonTime() = textProperty().mutateOnChange {
    it?.replace(Regex("[^0-9.:]"), "")
}

data class WebCamInfo(val webCamIndex: Int, val webCamName: String)


val ASPECT_RATIO = 3.0 / 4.0

class PhotoDialog (val success: ((File) -> Unit)) : JFXDialog(null, null, JFXDialog.DialogTransition.BOTTOM, true) {
    lateinit var imgWebCamCapturedImage: ImageView
    lateinit var webCamPane: BorderPane
    lateinit var topPane: FlowPane
    var webCam: Webcam? = null
    var stopCamera = false
    private val imageProperty: ObjectProperty<Image> = SimpleObjectProperty<Image>()

    init {
        buildLayout()
    }

    override fun close() {
        disposeWebCamCamera()
        super.close()
    }

    private fun buildLayout() {
        val root = BorderPane()
        topPane = FlowPane()
        topPane.paddingAll = 5.0
        topPane.alignment = Pos.CENTER
        topPane.hgap = 20.0
        topPane.orientation = Orientation.HORIZONTAL
        topPane.prefHeight = 40.0
        root.top = topPane
        webCamPane = BorderPane()
        webCamPane.style = "-fx-background-color: #ccc;"
        imgWebCamCapturedImage = ImageView()
        webCamPane.center = imgWebCamCapturedImage
        root.center = webCamPane
        createTopPanel()
        content = root

        root.bottom = HBox()

        val closeButton: JFXButton = jfxButton(messages.getString("dialog_close"))
        val okButton: JFXButton = jfxButton(messages.getString("dialog_continue").toUpperCase())

        closeButton.action {
            close()
        }
        okButton.action {
            val dir = File(PretixScanMain.dataDir, "photos")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val photoFile = File(
                    dir,
                    SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".png"
            )
            try {
                val ref = AtomicReference<WritableImage>()
                val i = SwingFXUtils.toFXImage(webCam!!.image, ref.get())
                val newWidth = (i.height * ASPECT_RATIO).toInt()
                val croppedImage = WritableImage(i.pixelReader, ((i.width - newWidth) / 2).toInt(), 0, newWidth, i.height.toInt())
                val bimg = BufferedImage(croppedImage.width.toInt(), croppedImage.height.toInt(), BufferedImage.TYPE_INT_RGB)
                SwingFXUtils.fromFXImage(croppedImage, bimg)
                ImageIO.write(bimg, ImageUtils.FORMAT_PNG, photoFile)
                success(photoFile)
            } catch (e: IOException) {
                throw WebcamException(e)
            }
            close()
        }

        root.bottom.add(closeButton)
        root.bottom.add(Region())
        root.bottom.add(okButton)

        Platform.runLater { setImageViewSize() }
    }

    protected fun setImageViewSize() {
        val height: Double = webCamPane.height
        val width: Double = webCamPane.width
        imgWebCamCapturedImage.fitHeight = max(height, 450.0)
        imgWebCamCapturedImage.fitWidth = width
        imgWebCamCapturedImage.prefHeight(height)
        imgWebCamCapturedImage.prefWidth(width)
        imgWebCamCapturedImage.isPreserveRatio = true
    }

    private fun createTopPanel() {
        var webCamCounter = 0
        val options: ObservableList<WebCamInfo> = FXCollections.observableArrayList<WebCamInfo>()
        for (webcam in Webcam.getWebcams()) {
            val webCamInfo = WebCamInfo(webCamCounter, webcam.name)
            options.add(webCamInfo)
            webCamCounter++
        }
        val cameraOptions = ComboBox<WebCamInfo>()
        cameraOptions.items = options
        cameraOptions.promptText = "Cameras"
        if (options.size > 0) {
            initializeWebCam(options[0].webCamIndex)
            cameraOptions.selectionModel.select(options[0])
        }
        cameraOptions.selectionModel.selectedItemProperty().addListener(object : ChangeListener<WebCamInfo> {
            override fun changed(arg0: ObservableValue<out WebCamInfo?>?, arg1: WebCamInfo?, arg2: WebCamInfo?) {
                if (arg2 != null) {
                    initializeWebCam(arg2.webCamIndex)
                }
            }
        })
        topPane.children.add(cameraOptions)
    }

    protected fun initializeWebCam(webCamIndex: Int) {
        val webCamTask: Task<Void> = object : Task<Void>() {
            @Throws(Exception::class)
            override fun call(): Void? {
                if (webCam != null) {
                    disposeWebCamCamera()
                }
                webCam = Webcam.getWebcams()[webCamIndex]
                webCam!!.setCustomViewSizes(
                        WebcamResolution.VGA.size,
                        WebcamResolution.HD.size,
                        WebcamResolution.FHD.size
                )
                try {
                    webCam!!.viewSize = WebcamResolution.FHD.size
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    webCam!!.viewSize = WebcamResolution.HD.size
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                webCam!!.open()
                startWebCamStream()
                return null
            }
        }
        val webCamThread = Thread(webCamTask)
        webCamThread.isDaemon = true
        webCamThread.start()
    }

    protected fun disposeWebCamCamera() {
        stopCamera = true
        webCam!!.close()
    }

    protected fun startWebCamStream() {
        stopCamera = false
        val task: Task<Void> = object : Task<Void>() {
            @Throws(Exception::class)
            override fun call(): Void? {
                val ref = AtomicReference<WritableImage>()
                var img: BufferedImage?
                while (!stopCamera) {
                    try {
                        if (webCam!!.getImage().also { img = it } != null) {
                            ref.set(SwingFXUtils.toFXImage(img, ref.get()))
                            img!!.flush()
                            Platform.runLater {
                                val i = ref.get()
                                val newWidth = (i.height * ASPECT_RATIO).toInt()
                                val croppedImage = WritableImage(i.pixelReader, ((i.width - newWidth) / 2).toInt(), 0, newWidth, i.height.toInt())

                                imageProperty.set(croppedImage)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return null
            }
        }
        val th = Thread(task)
        th.isDaemon = true
        th.start()
        imgWebCamCapturedImage.imageProperty().bind(imageProperty)
    }

}


class QuestionsDialog(val requiredAnswers: List<TicketCheckProvider.RequiredAnswer>, val retry: ((List<Answer>) -> Unit)?) : JFXDialog(null, null, JFXDialog.DialogTransition.BOTTOM, true) {
    val content = JFXDialogLayout()
    val fviews = HashMap<Question, Any>()

    init {
        buildLayout()
        setContent(content)
    }

    fun takePhoto(q: Question) {
        val dialog = PhotoDialog() {
            Platform.runLater {
                val hbox = fviews[q] as HBox
                if (hbox.children.size == 1) {
                    hbox.children.add(0, ImageView())
                }
                val img = hbox.children[0] as ImageView
                img.style = "-fx-background-color: #ccc;"
                img.fitWidth = 200.0
                img.fitHeight = 150.0
                img.prefWidth(200.0)
                img.prefHeight(150.0)
                img.isPreserveRatio = true
                img.image = Image(it.toURI().toString())
                hbox.userData = it.absolutePath
            }
        }
        dialog.overlayCloseProperty().set(false)
        dialog.show(dialogContainer)
    }

    private fun buildLayout() {
        val fview = vbox {
            addClass(MainStyleSheet.questionsForm)
        }

        for (ra in requiredAnswers) {
            val fieldcontrol = when (ra.question.type) {
                QuestionType.T -> jfxTextarea(ra.currentValue ?: "") {
                    prefRowCount = 2
                }
                QuestionType.B -> jfxCheckbox(ra.question.question, ra.currentValue == "True")
                QuestionType.C -> jfxCombobox<QuestionOption> {
                    useMaxWidth = true
                    items = FXCollections.observableArrayList(ra.question.options)
                    if (!ra.question.required) {
                        val qoempty = QuestionOption(0, 0, "", "")
                        items.add(0, qoempty)
                    }
                    for (item in items) {
                        if (item.getServer_id().toString() == ra.currentValue) {
                            selectionModel.select(item)
                            break
                        }
                    }
                }
                QuestionType.F -> hbox {
                    val btn = jfxButton(messages.getString("take_photo"))
                    btn.action {
                        takePhoto(ra.question)
                    }
                    this += btn
                    this.userData = null
                }
                QuestionType.D ->
                    jfxDatepicker(if (!ra.currentValue.isNullOrBlank()) LocalDate.parse(ra.currentValue, dateFormat) else null)
                QuestionType.H ->
                    jfxTimepicker(if (!ra.currentValue.isNullOrBlank()) LocalTime.parse(ra.currentValue, timeFormat) else null)
                QuestionType.W -> hbox {
                    val dp = jfxDatepicker(
                            if (!ra.currentValue.isNullOrBlank()) LocalDateTime.parse(ra.currentValue, dateTimeFormat).toLocalDate() else null
                    )
                    val tp = jfxTimepicker(
                            if (!ra.currentValue.isNullOrBlank()) LocalDateTime.parse(ra.currentValue, dateTimeFormat).toLocalTime() else null
                    )
                    this += dp
                    this += tp
                    fviews[ra.question] = DateTimeFieldCombo(dp, tp)
                }
                else -> jfxTextfield(ra.currentValue ?: "")
            }
            if (ra.question.type != QuestionType.B) {
                fview += label(ra.question.question)
            }
            if (ra.question.type == QuestionType.M) {
                var selected: List<String> = ArrayList()
                if (!ra.currentValue.isNullOrBlank()) {
                    selected = ra.currentValue!!.split(",")
                }
                val cbl = ArrayList<Any>()
                for (opt in ra.question.options) {
                    val cb = jfxCheckbox(opt.value) {
                        style {
                            paddingTop = 5.0
                            paddingBottom = 5.0
                        }
                    }
                    fview += cb
                    cb.tag = opt.getServer_id()
                    if (selected.contains(opt.getServer_id().toString())) {
                        cb.isSelected = true
                    }
                    cbl.add(cb)
                }
                fviews[ra.question] = cbl
                fieldcontrol.hide()
            } else {
                fview += fieldcontrol
                if (ra.question !in fviews) {
                    fviews[ra.question] = fieldcontrol
                }
            }
            fview += label(" ") {}
            if (ra.question.type == QuestionType.N && fieldcontrol is TextInputControl) {
                fieldcontrol.stripNonNumeric()
            }
        }

        val closeButton: JFXButton = jfxButton(messages.getString("dialog_close"))
        val okButton: JFXButton = jfxButton(messages.getString("dialog_continue").toUpperCase())

        content.setActions(closeButton, okButton)
        content.setBody(fview)  // TODO: scrollpane?

        closeButton.action {
            close()
        }
        okButton.action {
            val answers = ArrayList<Answer>()
            var has_errors = false

            for (ra in requiredAnswers) {
                val view = fviews[ra.question]

                val empty = when (ra.question.type) {
                    QuestionType.B -> !((view as CheckBox).isSelected)
                    QuestionType.C -> ((view as ComboBoxBase<QuestionOption>).value == null)
                    QuestionType.F -> ((view as HBox).userData == null)
                    QuestionType.D -> (view as JFXDatePicker).value == null
                    QuestionType.H -> (view as JFXTimePicker).value == null
                    QuestionType.W -> (((view as DateTimeFieldCombo).datefield as JFXDatePicker).value == null
                            || (view.timefield as JFXTimePicker).value == null)
                    QuestionType.M -> (view as List<CheckBox>).filter { it.isSelected }.isEmpty()
                    else -> (view as TextInputControl).text.isBlank()
                }

                if (empty && ra.question.required) {
                    // error!
                    if (view is Node) {
                        view.addDecorator(SimpleMessageDecorator(messages["field_required"], ValidationSeverity.Error))
                    } else if (view is DateTimeFieldCombo) {
                        (view.datefield as Control).addDecorator(SimpleMessageDecorator(messages["field_required"], ValidationSeverity.Error))
                    }
                    has_errors = true
                } else if (empty) {
                    answers.add(Answer(ra.question, ""))
                } else {
                    val answerstring = when (ra.question.type) {
                        QuestionType.B -> if ((view as CheckBox).isSelected) "True" else ""
                        QuestionType.C ->
                            if ((view as ComboBoxBase<QuestionOption>).value.getServer_id() != 0L)
                                view.value.getServer_id().toString()
                            else ""
                        QuestionType.F -> {
                            "file://${(view as HBox).userData}"
                        }
                        QuestionType.D -> dateFormat.format((view as JFXDatePicker).value)
                        QuestionType.H -> timeFormat.format((view as JFXTimePicker).value)
                        QuestionType.W ->
                            dateTimeFormat.format(
                                    LocalDateTime.of(((view as DateTimeFieldCombo).datefield as JFXDatePicker).value,
                                            (view.timefield as JFXTimePicker).value)
                            )
                        QuestionType.M -> (view as List<CheckBox>).filter { it.isSelected }.map { it.tag }.joinToString(",")
                        else -> (view as TextInputControl).text
                    }
                    try {
                        ra.question.clean_answer(answerstring, ra.question.options, false)
                    } catch (e: QuestionLike.ValidationException) {
                        if (view is Node) {
                            view.addDecorator(SimpleMessageDecorator(messages["field_invalid"], ValidationSeverity.Warning))
                        } else if (view is DateTimeFieldCombo) {
                            (view.datefield as Control).addDecorator(SimpleMessageDecorator(messages["field_invalid"], ValidationSeverity.Warning))
                        }
                        has_errors = true
                    }
                    answers.add(Answer(ra.question, answerstring))
                }
            }

            if (!has_errors) {
                close()
                retry?.invoke(answers)
            }
        }
    }
}


fun EventTarget.questionsDialog(requiredAnswers: List<TicketCheckProvider.RequiredAnswer>, retry: ((List<Answer>) -> Unit)? = null): JFXDialog {
    val dialog = QuestionsDialog(requiredAnswers, retry)
    dialog.overlayCloseProperty().set(false)
    return dialog
}