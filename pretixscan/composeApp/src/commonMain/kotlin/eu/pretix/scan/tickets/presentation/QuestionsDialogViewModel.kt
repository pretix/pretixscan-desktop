package eu.pretix.scan.tickets.presentation

import androidx.lifecycle.ViewModel
import com.vanniktech.locale.Country
import eu.pretix.desktop.app.ui.FieldValidationState
import eu.pretix.desktop.app.ui.SelectableValue
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.scan.tickets.data.PhoneValidator
import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.libpretixsync.db.QuestionOption
import eu.pretix.libpretixsync.models.Question
import eu.pretix.scan.tickets.data.EmailValidator
import eu.pretix.scan.tickets.data.ResultStateData
import eu.pretix.scan.tickets.data.calculateDefaultCountry
import eu.pretix.scan.tickets.utils.ImageLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.logging.Logger

class QuestionsDialogViewModel(
    private val config: DataStoreConfigStore,
    private val api: PretixApi
) : ViewModel() {

    private val log = Logger.getLogger("tickets")
    val imageLoader = ImageLoader(api)
    private val _form = MutableStateFlow(emptyList<QuestionFormField>())
    val form = _form.asStateFlow()

    private val _showNames = MutableStateFlow(false)
    val showNames = _showNames.asStateFlow()

    private val _uiBlinkSpecialTickets = MutableStateFlow(true)
    val uiBlinkSpecialTickets = _uiBlinkSpecialTickets.asStateFlow()

    private val _modalQuestion = MutableStateFlow<QuestionFormField?>(null)
    val modalQuestion = _modalQuestion.asStateFlow()

    private val emailValidator = EmailValidator()

    private val phoneValidator = PhoneValidator()
    fun getCurrentAnswers(data: ResultStateData): List<Answer> {
        val values = _form.value.toMutableList()

        return data.requiredQuestions
            .filter { question -> values.any { it.id == question.serverId } } // only return answers for supported questions
            .mapNotNull { question ->
                val formValue = values.first { it.id == question.serverId }
                when (formValue.fieldType) {
                    QuestionType.C -> {
                        Answer(question = question, value = formValue.value ?: "", options = formValue.options)
                    }

                    QuestionType.M -> {
                        val formattedValue = formValue.values?.joinToString(",") ?: ""
                        Answer(question = question, value = formattedValue, options = formValue.options)
                    }

                    QuestionType.F -> {
                        Answer(question = question, value = formValue.value ?: "")
                    }

                    QuestionType.N,
                    QuestionType.S,
                    QuestionType.T,
                    QuestionType.B,
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


    fun applyUiSettings() {
        _uiBlinkSpecialTickets.value = !config.uiReduceMotion
        _showNames.value = !config.uiHideNames
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

        val formFields: List<QuestionFormField> = data.requiredQuestions.map {
            when (it.type) {
                QuestionType.N -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it.serverId]),
                        it.type,
                        it.required,
                        numberMin = data.questionNumberMin[it.serverId],
                        numberMax = data.questionNumberMax[it.serverId]
                    )
                }

                QuestionType.EMAIL,
                QuestionType.F,
                QuestionType.B -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it.serverId]),
                        it.type,
                        it.required
                    )
                }

                QuestionType.S,
                QuestionType.T -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it.serverId]),
                        it.type,
                        it.required,
                        maxLength = data.questionMaxLengths[it.serverId]
                    )
                }

                QuestionType.TEL -> {
                    val answerValue = startingAnswerValue(it, data.answers[it.serverId])
                    val countryCode = if (!answerValue.isNullOrBlank()) {
                        calculateDefaultCountry(answerValue).code
                    } else {
                        null
                    }
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        answerValue,
                        it.type,
                        it.required,
                        uiExtra = countryCode
                    )
                }

                QuestionType.C -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it.serverId]),
                        it.type,
                        it.required,
                        keyValueOptions = it.options?.sortedBy { option -> option.position }
                            ?.map { option ->
                                SelectableValue(
                                    option.server_id.toString(),
                                    label = option.value
                                )
                            },
                        options = it.options?.toMutableList() ?: emptyList()
                    )
                }

                QuestionType.M -> {
                    val answerValue = startingAnswerValue(it, data.answers[it.serverId])
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        answerValue,
                        it.type,
                        it.required,
                        keyValueOptions = it.options?.sortedBy { option -> option.position }
                            ?.map { option ->
                                SelectableValue(
                                    option.server_id.toString(),
                                    label = option.value
                                )
                            },
                        options = it.options?.toMutableList() ?: emptyList(),
                        values = answerValue?.split(",")?.filter { it.isNotBlank() }
                    )
                }

                QuestionType.D -> {
                    val dateMinStr = data.questionDateMin[it.serverId]
                    val dateMaxStr = data.questionDateMax[it.serverId]
                    val minMillis = dateMinStr?.let { s ->
                        try { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(s)?.time } catch (_: Exception) { null }
                    }
                    val maxMillis = dateMaxStr?.let { s ->
                        try { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(s)?.time } catch (_: Exception) { null }
                    }
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it.serverId]),
                        it.type,
                        it.required,
                        dateConfig = DateConfig(minDate = minMillis, maxDate = maxMillis),
                        dateMin = dateMinStr,
                        dateMax = dateMaxStr
                    )
                }

                QuestionType.W -> {
                    val dateTimeMinStr = data.questionDateTimeMin[it.serverId]
                    val dateTimeMaxStr = data.questionDateTimeMax[it.serverId]
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.US)
                    val minMillis = dateTimeMinStr?.let { s ->
                        try { dateFormat.parse(s.take(10))?.time } catch (_: Exception) { null }
                    }
                    val maxMillis = dateTimeMaxStr?.let { s ->
                        try { dateFormat.parse(s.take(10))?.time } catch (_: Exception) { null }
                    }
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it.serverId]),
                        it.type,
                        it.required,
                        dateConfig = DateConfig(minDate = minMillis, maxDate = maxMillis),
                        dateMin = dateTimeMinStr,
                        dateMax = dateTimeMaxStr
                    )
                }

                QuestionType.H -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it.serverId]),
                        it.type,
                        it.required
                    )
                }

                QuestionType.CC -> {
                    QuestionFormField(
                        it.serverId,
                        it.question,
                        startingAnswerValue(it, data.answers[it.serverId]),
                        it.type,
                        it.required,
                        keyValueOptions = Country.entries.map { country ->
                            SelectableValue(
                                country.code,
                                label = country.readableName()
                            )
                        }
                    )
                }
            }
        }

        _form.value = formFields
    }


    fun updateChoiceAnswer(questionId: Long, value: String, checked: Boolean?) {
        _form.value = _form.value.map { field ->
            if (field.id == questionId && field.fieldType == QuestionType.M) {
                log.info("Updating choices for $questionId (${field.fieldType})")
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

    fun updateAnswer(questionId: Long, answer: String?, extra: String? = null) {
        _form.value = _form.value.map { field ->
            if (field.id == questionId) {
                log.info("Updating answer for $questionId (${field.fieldType})")

                when (field.fieldType) {
                    QuestionType.F -> {
                        field.copy(value = formatFileAnswer(answer))
                    }

                    QuestionType.N -> {
                        if (answer.isNullOrEmpty() || answer.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                            val validation = validateNumberRange(answer, field.numberMin, field.numberMax)
                            field.copy(value = answer, validation = validation)
                        } else {
                            field
                        }
                    }

                    QuestionType.EMAIL -> {
                        field.copy(value = answer)
                    }

                    QuestionType.TEL -> {
                        field.copy(value = answer, uiExtra = extra)
                    }

                    QuestionType.D -> {
                        val validation = validateDateRange(answer, field.dateMin, field.dateMax)
                        field.copy(value = answer, validation = validation)
                    }

                    QuestionType.W -> {
                        val validation = validateDateTimeRange(answer, field.dateMin, field.dateMax)
                        field.copy(value = answer, validation = validation)
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


    fun validateForConfirm(): Int? {
        formatAndValidateForm()
        val index = _form.value.indexOfFirst { it.validation != null }
        return if (index == -1) null else index
    }

    fun formatAndValidateForm() {
        // process all values, apply formatting and validation
        _form.value = _form.value.map {
            when (it.fieldType) {
                QuestionType.TEL -> {
                    val answer = it.value
                    val country = it.uiExtra
                    if (answer.isNullOrBlank()) {
                        if (it.required) {
                            it.copy(validation = FieldValidationState.MISSING)
                        } else {
                            it.copy(validation = null)
                        }
                    } else {
                        val parsed = phoneValidator.parse(answer, country)
                        if (parsed == null) {
                            it.copy(validation = FieldValidationState.INVALID)
                        } else {
                            it.copy(validation = null, value = parsed.number)
                        }
                    }
                }

                QuestionType.M -> {
                    if (it.required && it.values.isNullOrEmpty()) {
                        it.copy(validation = FieldValidationState.MISSING)
                    } else {
                        it.copy(validation = null)
                    }
                }

                QuestionType.S,
                QuestionType.T -> {
                    val value = it.value
                    if (value.isNullOrBlank() && it.required) {
                        it.copy(validation = FieldValidationState.MISSING)
                    } else if (!value.isNullOrBlank() && it.maxLength != null && value.length > it.maxLength) {
                        it.copy(validation = FieldValidationState.INVALID)
                    } else {
                        it.copy(validation = null)
                    }
                }

                QuestionType.N -> {
                    val value = it.value
                    if (it.required && value.isNullOrBlank()) {
                        it.copy(validation = FieldValidationState.MISSING)
                    } else if (!value.isNullOrBlank() && value.toBigDecimalOrNull() == null) {
                        it.copy(validation = FieldValidationState.INVALID)
                    } else {
                        val validation = validateNumberRange(value, it.numberMin, it.numberMax)
                        it.copy(validation = validation)
                    }
                }

                QuestionType.D -> {
                    val value = it.value
                    if (it.required && value.isNullOrBlank()) {
                        it.copy(validation = FieldValidationState.MISSING)
                    } else if (!value.isNullOrBlank()) {
                        try {
                            LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val validation = validateDateRange(value, it.dateMin, it.dateMax)
                            it.copy(validation = validation)
                        } catch (_: DateTimeParseException) {
                            it.copy(validation = FieldValidationState.INVALID)
                        }
                    } else {
                        it.copy(validation = null)
                    }
                }

                QuestionType.W -> {
                    val value = it.value
                    if (it.required && value.isNullOrBlank()) {
                        it.copy(validation = FieldValidationState.MISSING)
                    } else if (!value.isNullOrBlank()) {
                        try {
                            LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                            val validation = validateDateTimeRange(value, it.dateMin, it.dateMax)
                            it.copy(validation = validation)
                        } catch (_: DateTimeParseException) {
                            it.copy(validation = FieldValidationState.INVALID)
                        }
                    } else {
                        it.copy(validation = null)
                    }
                }

                QuestionType.B -> {
                    if (it.required && it.value != "True") {
                        it.copy(validation = FieldValidationState.MISSING)
                    } else {
                        it.copy(validation = null)
                    }
                }

                QuestionType.EMAIL -> {
                    val value = it.value
                    if (it.required && value.isNullOrBlank()) {
                        it.copy(validation = FieldValidationState.MISSING)
                    } else if (!value.isNullOrBlank() && !emailValidator.isValidEmail(value)) {
                        it.copy(validation = FieldValidationState.INVALID)
                    } else {
                        it.copy(validation = null)
                    }
                }

                QuestionType.H -> {
                    val value = it.value
                    if (it.required && value.isNullOrBlank()) {
                        it.copy(validation = FieldValidationState.MISSING)
                    } else if (!value.isNullOrBlank()) {
                        try {
                            LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"))
                            it.copy(validation = null)
                        } catch (_: DateTimeParseException) {
                            it.copy(validation = FieldValidationState.INVALID)
                        }
                    } else {
                        it.copy(validation = null)
                    }
                }

                else -> {
                    if (it.required && it.value.isNullOrBlank()) {
                        it.copy(validation = FieldValidationState.MISSING)
                    } else {
                        it.copy(validation = null)
                    }
                }
            }
        }
        _form.value.forEach { field ->
            log.info("question ${field.label}, valid: ${if (field.validation == null) "yes" else "${field.validation}"}")
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

        val defaultValue = question.default
        if (!defaultValue.isNullOrBlank()) {
            return defaultValue
        }

        // For Country questions, use the system's default country when no value is set
        if (question.type == QuestionType.CC) {
            return calculateDefaultCountry(null).code
        }

        return null
    }

    private fun validateNumberRange(value: String?, min: String?, max: String?): FieldValidationState? {
        if (value.isNullOrEmpty()) return null
        val number = value.toBigDecimalOrNull() ?: return null
        if (min != null) {
            val minVal = min.toBigDecimalOrNull()
            if (minVal != null && number < minVal) return FieldValidationState.INVALID
        }
        if (max != null) {
            val maxVal = max.toBigDecimalOrNull()
            if (maxVal != null && number > maxVal) return FieldValidationState.INVALID
        }
        return null
    }

    private fun validateDateRange(value: String?, min: String?, max: String?): FieldValidationState? {
        if (value.isNullOrEmpty()) return null
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = try { LocalDate.parse(value, formatter) } catch (_: DateTimeParseException) { return null }
        if (min != null) {
            try {
                if (date < LocalDate.parse(min, formatter)) return FieldValidationState.INVALID
            } catch (_: DateTimeParseException) { }
        }
        if (max != null) {
            try {
                if (date > LocalDate.parse(max, formatter)) return FieldValidationState.INVALID
            } catch (_: DateTimeParseException) { }
        }
        return null
    }

    private fun validateDateTimeRange(value: String?, min: String?, max: String?): FieldValidationState? {
        if (value.isNullOrEmpty()) return null
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val dateTime = try { LocalDateTime.parse(value, formatter) } catch (_: DateTimeParseException) { return null }
        if (min != null) {
            try {
                if (dateTime < LocalDateTime.parse(min, formatter)) return FieldValidationState.INVALID
            } catch (_: DateTimeParseException) { }
        }
        if (max != null) {
            try {
                if (dateTime > LocalDateTime.parse(max, formatter)) return FieldValidationState.INVALID
            } catch (_: DateTimeParseException) { }
        }
        return null
    }

    private fun formatFileAnswer(answer: String?): String? {
        return when {
            answer == null -> null
            answer.contains("://") -> answer
            answer.isEmpty() -> answer
            else -> "file://$answer"
        }
    }
}

data class QuestionFormField(
    val id: Long,
    val label: String,
    var value: String?,
    val fieldType: QuestionType,
    val required: Boolean,
    var values: List<String>? = null,
    var dateConfig: DateConfig? = null,
    val keyValueOptions: List<SelectableValue>? = null,
    val options: List<QuestionOption> = emptyList(),
    var validation: FieldValidationState? = null,
    val maxLength: Int? = null,
    val numberMin: String? = null,
    val numberMax: String? = null,
    val dateMin: String? = null,
    val dateMax: String? = null,
    // Extra value used by the UI for form state which isn't part of the pretixsync model
    var uiExtra: String? = null
)

data class DateConfig(val minDate: Long?, val maxDate: Long?)

