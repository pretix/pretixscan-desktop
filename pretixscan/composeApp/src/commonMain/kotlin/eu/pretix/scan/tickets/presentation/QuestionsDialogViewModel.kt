package eu.pretix.scan.tickets.presentation

import androidx.lifecycle.ViewModel
import com.vanniktech.locale.Country
import eu.pretix.desktop.app.ui.KeyValueOption
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.libpretixsync.db.QuestionOption
import eu.pretix.libpretixsync.models.Question
import eu.pretix.scan.tickets.data.EmailValidator
import eu.pretix.scan.tickets.data.ResultStateData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.logging.Logger

class QuestionsDialogViewModel(private val config: AppConfig) : ViewModel() {

    private val log = Logger.getLogger("tickets")
    private val _form = MutableStateFlow(emptyList<QuestionFormField>())
    val form = _form.asStateFlow()

    private val _showNames = MutableStateFlow(false)
    val showNames = _showNames.asStateFlow()

    private val _modalQuestion = MutableStateFlow<QuestionFormField?>(null)
    val modalQuestion = _modalQuestion.asStateFlow()

    private val emailValidator = EmailValidator()
    fun getCurrentAnswers(data: ResultStateData): List<Answer> {
        val values = _form.value.toMutableList()

        return data.requiredQuestions
            .filter { question -> values.any { it.id == question.serverId } } // only return answers for supported questions
            .map { question ->
                val formValue = values.first { it.id == question.serverId }
                when (formValue.fieldType) {
                    QuestionType.C -> {
                        Answer(question = question, value = formValue.value ?: "", options = formValue.options)
                    }

                    QuestionType.M -> {
                        val formattedValue = formValue.values?.joinToString(",") ?: ""
                        Answer(question = question, value = formattedValue, options = formValue.options)
                    }

                    QuestionType.N,
                    QuestionType.S,
                    QuestionType.T,
                    QuestionType.B,
                    QuestionType.F,
                    QuestionType.D,
                    QuestionType.H,
                    QuestionType.W,
                    QuestionType.CC,
                    QuestionType.TEL,
                    QuestionType.EMAIL -> {
                        Answer(question = question, value = formValue.value ?: "")
                    }
                }
            }
    }

    fun buildQuestionsForm(data: ResultStateData) {
        log.info(
            "there are ${data.requiredQuestions.size} questions, ${
                data.requiredQuestions.map { it.type.name }.sorted()
            }"
        )

        log.info(
            "${
                data.requiredQuestions.map { it.serverId }.sorted()
            }"
        )

        _showNames.value = !config.hideNames
        // FIXME: What is DOB?

        val formFields = data.requiredQuestions.mapNotNull { it ->
            when (it.type) {
                QuestionType.N,
                QuestionType.EMAIL,
                QuestionType.S,
                QuestionType.T,
                QuestionType.F,
                QuestionType.TEL,
                QuestionType.B -> {
                    QuestionFormField(it.serverId, it.question, startingAnswerValue(it, data.answers[it]), it.type)
                }

                QuestionType.C -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it]),
                        it.type,
                        keyValueOptions = it.options?.sortedBy { option -> option.position }
                            ?.map { option -> KeyValueOption(option.value, option.server_id.toString()) },
                        options = it.options?.toMutableList() ?: emptyList()
                    )
                }

                QuestionType.M -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it]),
                        it.type,
                        keyValueOptions = it.options?.sortedBy { option -> option.position }
                            ?.map { option -> KeyValueOption(option.value, option.server_id.toString()) },
                        options = it.options?.toMutableList() ?: emptyList(),
                        values = it.dependencyValues
                    )
                }

                QuestionType.W,
                QuestionType.D -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it]),
                        it.type,
                        dateConfig = DateConfig(minDate = it.valid_date_min, maxDate = it.valid_date_max)
                    )
                }

                QuestionType.H -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it]),
                        it.type
                    )
                }

                QuestionType.CC -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it]),
                        it.type,
                        keyValueOptions = Country.entries.map { country -> KeyValueOption(country.name, country.code) }
                    )
                }
            }
        }

        _form.value = formFields
    }


    fun updateChoiceAnswer(questionId: Long, value: String, checked: Boolean?) {
        _form.value = _form.value.map { field ->
            if (field.id == questionId && field.fieldType == QuestionType.M) {
                log.info("Updating choices for $questionId (${field.fieldType}) value:checked $value:$checked")
                when (checked) {
                    true -> {
                        val updatedValues = (field.values?.toList()?.toMutableList()
                            ?: mutableListOf()).apply { if (value !in this) add(value) }
                        field.copy(values = updatedValues)
                    }

                    null,
                    false -> field.copy(values = field.values?.filter { it != value })
                }
            } else {
                field
            }
        }
    }

    fun updateAnswer(questionId: Long, answer: String?) {
        _form.value = _form.value.map { field ->
            if (field.id == questionId) {
                log.info("Updating answer for $questionId (${field.fieldType}) to $answer")

                when (field.fieldType) {
                    QuestionType.F -> {
                        if (answer != null) {
                            // for files, pretixlibsync requires a "file:///" prefix
                            field.copy(value = "file://${answer}")
                        } else {
                            field.copy(value = answer)
                        }
                    }

                    QuestionType.N -> {
                        if (answer != null && answer.all { it.isDigit() }) {
                            field.copy(value = answer)
                        } else {
                            field
                        }
                    }

                    QuestionType.EMAIL -> {
                        if (answer != null && emailValidator.isValidEmail(answer)) {
                            field.copy(value = answer)
                        } else {
                            field
                        }
                    }

                    else -> {
                        field.copy(value = answer)
                    }
                }
            } else {
                field
            }
        }
    }

    fun validateForConfirm(): Boolean {
        _form.value.forEach {
            log.info("question ${it.label}: ${it.value}")
        }
        // FIXME: Return false if validation fails
        return true
    }

    fun showModal(field: QuestionFormField) {
        _modalQuestion.update { field }
    }

    fun dismissModal(answer: String?) {
        val field = _modalQuestion.value
        if (field == null) {
            log.warning("Modal dismissed without a modal question")
            return
        }

        _modalQuestion.update { null }

        updateAnswer(field.id, answer)
    }

    private fun startingAnswerValue(question: Question, answer: String?): String? {
        if (!answer.isNullOrBlank()) {
            return answer
        }

        if (!question.default.isNullOrBlank()) {
            return question.default
        }

        return null
    }
}

data class QuestionFormField(
    val id: Long,
    val label: String,
    var value: String?,
    val fieldType: QuestionType,
    var values: List<String>? = null,
    var dateConfig: DateConfig? = null,
    val keyValueOptions: List<KeyValueOption>? = null,
    val options: List<QuestionOption> = emptyList()
)

data class DateConfig(val minDate: Long?, val maxDate: Long?)
  