package eu.pretix.pretixscan.desktop.ui.helpers

import com.jfoenix.controls.*
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.AbstractQuestion
import eu.pretix.libpretixsync.db.Question
import eu.pretix.libpretixsync.db.QuestionOption
import eu.pretix.pretixscan.desktop.ui.style.MainStyleSheet
import javafx.collections.FXCollections
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.*
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

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
            QuestionType.F -> label("-not supported-")
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
                selected = ra.currentValue.split(",")
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

    val dialog = JFXDialog(null, content, JFXDialog.DialogTransition.BOTTOM, true)
    dialog.overlayCloseProperty().set(false)
    closeButton.action {
        dialog.close()
    }
    okButton.action {
        val answers = ArrayList<TicketCheckProvider.Answer>()
        var has_errors = false

        for (ra in requiredAnswers) {
            val view = fviews[ra.question]

            val empty = when (ra.question.type) {
                QuestionType.B -> !((view as CheckBox).isSelected)
                QuestionType.C -> ((view as ComboBoxBase<QuestionOption>).value == null)
                QuestionType.F -> true
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
                answers.add(TicketCheckProvider.Answer(ra.question, ""))
            } else {
                val answerstring = when (ra.question.type) {
                    QuestionType.B -> if ((view as CheckBox).isSelected) "True" else ""
                    QuestionType.C ->
                        if ((view as ComboBoxBase<QuestionOption>).value.getServer_id() != 0L)
                            view.value.getServer_id().toString()
                            else ""
                    QuestionType.F -> ""
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
                    ra.question.clean_answer(answerstring, ra.question.options)
                } catch (e: AbstractQuestion.ValidationException) {
                    if (view is Node) {
                        view.addDecorator(SimpleMessageDecorator(messages["field_invalid"], ValidationSeverity.Warning))
                    } else if (view is DateTimeFieldCombo) {
                        (view.datefield as Control).addDecorator(SimpleMessageDecorator(messages["field_invalid"], ValidationSeverity.Warning))
                    }
                    has_errors = true
                }
                answers.add(TicketCheckProvider.Answer(ra.question, answerstring))
            }
        }

        if (!has_errors) {
            dialog.close()
            retry?.invoke(answers)
        }
    }
    return dialog
}