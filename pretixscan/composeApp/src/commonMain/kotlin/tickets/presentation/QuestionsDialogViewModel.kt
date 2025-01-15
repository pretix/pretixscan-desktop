package tickets.presentation

import androidx.lifecycle.ViewModel
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.db.QuestionOption
import eu.pretix.libpretixsync.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tickets.data.ResultStateData
import java.util.logging.Logger

class QuestionsDialogViewModel(private val config: AppConfig) : ViewModel() {

    private val log = Logger.getLogger("tickets")
    private val _form = MutableStateFlow(emptyList<QuestionFormField>())
    val form = _form.asStateFlow()

    private val _showNames = MutableStateFlow(false)
    val showNames = _showNames.asStateFlow()

    private val _modalQuestion = MutableStateFlow<QuestionFormField?>(null)
    val modalQuestion = _modalQuestion.asStateFlow()

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

        val formFields = data.requiredQuestions.mapNotNull {
            when (it.type) {
                QuestionType.N,
                QuestionType.S,
                QuestionType.T,
                QuestionType.F,
                QuestionType.B -> {
                    QuestionFormField(it.serverId, it.question, startingAnswerValue(it, data.answers[it]), it.type)
                }

                QuestionType.C -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it]),
                        it.type,
                        it.options
                    )
                }

                QuestionType.M -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it]),
                        it.type,
                        it.options,
                        it.dependencyValues
                    )
                }

                QuestionType.D -> {
                    null
                }

                QuestionType.H -> {
                    null
                }

                QuestionType.W -> {
                    null
                }

                QuestionType.CC -> {
                    null
                }

                QuestionType.TEL -> {
                    null
                }

                QuestionType.EMAIL -> {
                    null
                }
            }
        }

        _form.value = formFields
    }


    fun updateChoiceAnswer(questionId: Long, value: String, checked: Boolean?) {
        _form.value = _form.value.map { field ->
            if (field.id == questionId && field.fieldType == QuestionType.M) {
                log.info("Updating choices for $questionId (${field.fieldType}) $value to checked = $checked")
                when (checked) {
                    true -> {
                        val updatedValues = (field.values?.toList()?.toMutableList() ?: mutableListOf()).apply { if (value !in this) add(value) }
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
                    else -> {
                        field.copy(value = answer)
                    }
                }
            } else {
                field
            }
        }
    }

    fun validateAndContinue() {
        _form.value.forEach {
            log.info("question ${it.label}: ${it.value}")
        }
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
    val availableOptions: List<QuestionOption>? = null,
    var values: List<String>? = null,
)