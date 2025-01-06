package tickets.presentation

import androidx.lifecycle.ViewModel
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tickets.data.ResultStateData
import java.util.logging.Logger

class QuestionsDialogViewModel(private val config: AppConfig) : ViewModel() {

    private val log = Logger.getLogger("tickets")
    private val _form = MutableStateFlow(emptyList<QuestionFormField>())
    val form = _form.asStateFlow()

    private val _showNames = MutableStateFlow(false)
    val showNames = _showNames.asStateFlow()

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
                QuestionType.B -> {
                    QuestionFormField(it.serverId, it.question, startingAnswerValue(it, data.answers[it]), it.type)
                }

                QuestionType.C -> {
                    null
                }

                QuestionType.M -> {
                    null
                }

                QuestionType.F -> {
                    null
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

    fun updateAnswer(questionId: Long, answer: String?) {
        log.info("Updating answer for $questionId to $answer")
        _form.value = _form.value.map { field ->
            if (field.id == questionId) {
                field.copy(value = answer)
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
    val fieldType: QuestionType
)