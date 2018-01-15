package eu.pretix.pretixdesk.ui.helpers

import com.jfoenix.controls.*
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.Question
import eu.pretix.libpretixsync.db.QuestionOption
import eu.pretix.pretixdesk.ui.style.MainStyleSheet
import javafx.collections.FXCollections
import javafx.event.EventTarget
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBoxBase
import javafx.scene.control.TextInputControl
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.SimpleDateFormat
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class DateTimeFieldCombo {
    var datefield: JFXDatePicker? = null
    var timefield: JFXTimePicker? = null

    constructor(datefield: JFXDatePicker, timefield: JFXTimePicker) {
        this.datefield = datefield
        this.timefield = timefield
    }
}


fun TextInputControl.stripNonTime() = textProperty().mutateOnChange {
    it?.replace(Regex("[^0-9.:]"), "")
}

fun EventTarget.questionsDialog(requiredAnswers: List<TicketCheckProvider.RequiredAnswer>, retry: ((List<TicketCheckProvider.Answer>) -> Unit)? = null): JFXDialog {
    val content = JFXDialogLayout()

    val fview = vbox {
        addClass(MainStyleSheet.questionsForm)
    }

    val fviews = HashMap<Question, Any>()

    for (ra in requiredAnswers) {
        val fieldcontrol = when (ra.question.type) {
            QuestionType.TEXT -> jfxTextarea(ra.currentValue ?: "") {
                prefRowCount = 2
            }
            QuestionType.BOOLEAN -> jfxCheckbox(ra.question.question)
            QuestionType.CHOICE -> jfxCombobox<QuestionOption> {
                useMaxWidth = true
                items = FXCollections.observableArrayList(ra.question.options)
            }
            QuestionType.FILE -> label("-not supported-")
            QuestionType.DATE -> jfxDatepicker()
            QuestionType.TIME -> jfxTimepicker()
            QuestionType.DATETIME -> hbox {
                val dp = jfxDatepicker { }
                val tp = jfxTimepicker { }
                this += dp
                this += tp
                fviews[ra.question] = DateTimeFieldCombo(dp, tp)
            }
            else -> jfxTextfield(ra.currentValue ?: "")
        }
        if (ra.question.type != QuestionType.BOOLEAN) {
            fview += label(ra.question.question)
        }
        if (ra.question.type == QuestionType.CHOICE_MULTIPLE) {
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
        if (ra.question.isRequired && fieldcontrol is TextInputControl) {
            fieldcontrol.required()
        }
        if (ra.question.type == QuestionType.NUMBER && fieldcontrol is TextInputControl) {
            fieldcontrol.stripNonNumeric()
        }
    }

    val closeButton: JFXButton = jfxButton(messages.getString("dialog_close"))
    val okButton: JFXButton = jfxButton(messages.getString("dialog_ok").toUpperCase())

    content.setActions(closeButton, okButton)
    content.setBody(fview)  // TODO: scrollpane?

    val dialog = JFXDialog(null, content, JFXDialog.DialogTransition.BOTTOM, true)
    dialog.overlayCloseProperty().set(false)
    closeButton.action {
        dialog.close()
    }
    okButton.action {
        val answers = ArrayList<TicketCheckProvider.Answer>()
        val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm'Z'")
        val tf = DateTimeFormatter.ofPattern("HH:mm")

        for (ra in requiredAnswers) {
            val view = fviews[ra.question]
            val answerstring = when (ra.question.type) {
                QuestionType.BOOLEAN -> if ((view as CheckBox).isSelected) "True" else ""
                QuestionType.CHOICE -> (view as ComboBoxBase<QuestionOption>).value.getServer_id().toString()
                QuestionType.FILE -> ""
                QuestionType.DATE -> df.format((view as JFXDatePicker).value)
                QuestionType.TIME -> tf.format((view as JFXTimePicker).value)
                QuestionType.DATETIME -> dtf.format(LocalDateTime.of(((view as DateTimeFieldCombo).datefield as JFXDatePicker).value, ((view as DateTimeFieldCombo).timefield as JFXTimePicker).value))
                QuestionType.CHOICE_MULTIPLE -> (view as List<CheckBox>).filter { it.isSelected }.map { it.tag }.joinToString(",")
                else -> (view as TextInputControl).text
            }
            answers.add(TicketCheckProvider.Answer(ra.question, answerstring))
        }

        dialog.close()
        retry?.invoke(answers)
    }
    return dialog
}