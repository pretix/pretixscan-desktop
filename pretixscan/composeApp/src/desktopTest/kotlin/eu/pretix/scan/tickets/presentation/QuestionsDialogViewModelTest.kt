package eu.pretix.scan.tickets.presentation

import eu.pretix.desktop.app.ui.FieldValidationState
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.libpretixsync.models.Question
import eu.pretix.scan.tickets.data.ResultState
import eu.pretix.scan.tickets.data.ResultStateData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class QuestionsDialogViewModelTest {

    private lateinit var config: DataStoreConfigStore
    private lateinit var api: PretixApi
    private lateinit var viewModel: QuestionsDialogViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        config = mockk(relaxed = true)
        api = mockk(relaxed = true)
        every { config.uiReduceMotion } returns false
        every { config.uiHideNames } returns false
        viewModel = QuestionsDialogViewModel(config, api)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateAnswer for TEL preserves country code in uiExtra`() = runTest {
        val telQuestion = Question(
            id = 1L,
            serverId = 1L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Phone Number",
            identifier = "phone",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.TEL
        )

        val data = ResultStateData(
            resultState = ResultState.SUCCESS,
            requiredQuestions = listOf(telQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)

        viewModel.updateAnswer(1L, "0474 12 34 56", "BE")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals("0474 12 34 56", formField.value)
        assertEquals("BE", formField.uiExtra)
    }

    @Test
    fun `formatAndValidateForm converts Belgium phone to E164 with correct country code`() = runTest {
        val telQuestion = Question(
            id = 1L,
            serverId = 1L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Phone Number",
            identifier = "phone",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.TEL
        )

        val data = ResultStateData(
            resultState = ResultState.SUCCESS,
            requiredQuestions = listOf(telQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "0474 12 34 56", "BE")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals("+32474123456", formField.value)
        assertNull(formField.validation)
    }

    @Test
    fun `formatAndValidateForm preserves selected country code not default US`() = runTest {
        val telQuestion = createTestQuestion()

        val data = ResultStateData(
            resultState = ResultState.SUCCESS,
            requiredQuestions = listOf(telQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "030 12345678", "DE")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals("+493012345678", formField.value)
        assertFalse(formField.value!!.startsWith("+1"))
    }

    @Test
    fun `formatAndValidateForm marks empty required TEL field as MISSING`() = runTest {
        val telQuestion = createTestQuestion()

        val data = ResultStateData(
            resultState = ResultState.SUCCESS,
            requiredQuestions = listOf(telQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.MISSING, formField.validation)
    }

    @Test
    fun `formatAndValidateForm marks invalid TEL field as INVALID`() = runTest {
        val telQuestion = createTestQuestion()

        val data = ResultStateData(
            resultState = ResultState.SUCCESS,
            requiredQuestions = listOf(telQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "not a phone number", "BE")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `getCurrentAnswers returns E164 formatted phone number`() = runTest {
        val telQuestion = createTestQuestion()

        val data = ResultStateData(
            resultState = ResultState.SUCCESS,
            requiredQuestions = listOf(telQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "0474 12 34 56", "BE")
        viewModel.formatAndValidateForm()

        val answers = viewModel.getCurrentAnswers(data)
        assertEquals(1, answers.size)
        assertEquals("+32474123456", answers[0].value)
    }

    @Test
    fun `country code from uiExtra is used for validation not default`() = runTest {
        val telQuestion = createTestQuestion()

        val data = ResultStateData(
            resultState = ResultState.SUCCESS,
            requiredQuestions = listOf(telQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "02071234567", "GB")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals("+442071234567", formField.value)
        assertNull(formField.validation)
    }

    @Test
    fun `validateForConfirm returns true when all TEL fields valid`() = runTest {
        val telQuestion = createTestQuestion()

        val data = ResultStateData(
            resultState = ResultState.SUCCESS,
            requiredQuestions = listOf(telQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "0474 12 34 56", "BE")

        val isValid = viewModel.validateForConfirm()
        assertNull(isValid)
    }

    @Test
    fun `validateForConfirm returns false when required TEL field empty`() = runTest {
        val telQuestion = createTestQuestion()

        val data = ResultStateData(
            resultState = ResultState.SUCCESS,
            requiredQuestions = listOf(telQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)

        val isValid = viewModel.validateForConfirm()
        assertNotNull(isValid)
    }

    @Test
    fun `text question with pre-answered value is pre-filled`() = runTest {
        val textQuestion = Question(
            id = 1L,
            serverId = 100L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Company Name",
            identifier = "company",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.T
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(textQuestion),
            answers = mapOf(100L to "Acme Corporation")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 100L }
        assertEquals("Acme Corporation", formField.value)
        assertEquals(QuestionType.T, formField.fieldType)
    }

    @Test
    fun `single-choice question with pre-answered value is pre-filled`() = runTest {
        val option1 = eu.pretix.libpretixsync.db.QuestionOption(1L, 1L, "red", "Red")
        val option2 = eu.pretix.libpretixsync.db.QuestionOption(2L, 2L, "blue", "Blue")

        val choiceQuestion = Question(
            id = 1L,
            serverId = 200L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Favorite Color",
            identifier = "color",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.C,
            options = listOf(option1, option2)
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(choiceQuestion),
            answers = mapOf(200L to "1")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 200L }
        assertEquals("1", formField.value)
        assertEquals(QuestionType.C, formField.fieldType)
    }

    @Test
    fun `multiple-choice question with pre-answered values are pre-filled`() = runTest {
        val option1 = eu.pretix.libpretixsync.db.QuestionOption(1L, 1L, "vegetarian", "Vegetarian")
        val option2 = eu.pretix.libpretixsync.db.QuestionOption(2L, 2L, "vegan", "Vegan")
        val option3 = eu.pretix.libpretixsync.db.QuestionOption(3L, 3L, "gluten-free", "Gluten-free")

        val multiChoiceQuestion = Question(
            id = 1L,
            serverId = 300L,
            eventSlug = "test-event",
            position = 0,
            required = false,
            question = "Dietary Requirements",
            identifier = "dietary",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.M,
            options = listOf(option1, option2, option3)
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(multiChoiceQuestion),
            answers = mapOf(300L to "1,3")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 300L }
        assertEquals("1,3", formField.value)
        assertEquals(QuestionType.M, formField.fieldType)
        assertEquals(listOf("1", "3"), formField.values)
    }

    @Test
    fun `date question with pre-answered value is pre-filled`() = runTest {
        val dateQuestion = Question(
            id = 1L,
            serverId = 400L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Birth Date",
            identifier = "birthdate",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.D
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(dateQuestion),
            answers = mapOf(400L to "2023-05-15")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 400L }
        assertEquals("2023-05-15", formField.value)
        assertEquals(QuestionType.D, formField.fieldType)
    }

    @Test
    fun `datetime question with pre-answered value is pre-filled`() = runTest {
        val datetimeQuestion = Question(
            id = 1L,
            serverId = 500L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Arrival Time",
            identifier = "arrival",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.W
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(datetimeQuestion),
            answers = mapOf(500L to "2023-05-15T14:30:00")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 500L }
        assertEquals("2023-05-15T14:30:00", formField.value)
        assertEquals(QuestionType.W, formField.fieldType)
    }

    @Test
    fun `time question with pre-answered value is pre-filled`() = runTest {
        val timeQuestion = Question(
            id = 1L,
            serverId = 600L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Preferred Time",
            identifier = "time",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.H
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(timeQuestion),
            answers = mapOf(600L to "14:30:00")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 600L }
        assertEquals("14:30:00", formField.value)
        assertEquals(QuestionType.H, formField.fieldType)
    }

    @Test
    fun `boolean question with pre-answered value is pre-filled`() = runTest {
        val boolQuestion = Question(
            id = 1L,
            serverId = 700L,
            eventSlug = "test-event",
            position = 0,
            required = false,
            question = "Newsletter Subscription",
            identifier = "newsletter",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.B
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(boolQuestion),
            answers = mapOf(700L to "True")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 700L }
        assertEquals("True", formField.value)
        assertEquals(QuestionType.B, formField.fieldType)
    }

    @Test
    fun `email question with pre-answered value is pre-filled`() = runTest {
        val emailQuestion = Question(
            id = 1L,
            serverId = 800L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Email Address",
            identifier = "email",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.EMAIL
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(emailQuestion),
            answers = mapOf(800L to "test@example.com")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 800L }
        assertEquals("test@example.com", formField.value)
        assertEquals(QuestionType.EMAIL, formField.fieldType)
    }

    @Test
    fun `number question with pre-answered value is pre-filled`() = runTest {
        val numberQuestion = Question(
            id = 1L,
            serverId = 900L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Number of Guests",
            identifier = "guests",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.N
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(numberQuestion),
            answers = mapOf(900L to "5")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 900L }
        assertEquals("5", formField.value)
        assertEquals(QuestionType.N, formField.fieldType)
    }

    @Test
    fun `country question with pre-answered value is pre-filled`() = runTest {
        val countryQuestion = Question(
            id = 1L,
            serverId = 1000L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Country of Residence",
            identifier = "country",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.CC
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(countryQuestion),
            answers = mapOf(1000L to "BE")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 1000L }
        assertEquals("BE", formField.value)
        assertEquals(QuestionType.CC, formField.fieldType)
    }

    @Test
    fun `phone question with pre-answered value is pre-filled with correct country code`() = runTest {
        val phoneQuestion = Question(
            id = 1L,
            serverId = 1100L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Phone Number",
            identifier = "phone",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.TEL
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(phoneQuestion),
            answers = mapOf(1100L to "+32474123456")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 1100L }
        assertEquals("+32474123456", formField.value)
        assertEquals("BE", formField.uiExtra)
        assertEquals(QuestionType.TEL, formField.fieldType)
    }

    @Test
    fun `mix of answered and unanswered questions only pre-fills answered ones`() = runTest {
        val question1 = Question(
            id = 1L,
            serverId = 100L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Company Name",
            identifier = "company",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.T
        )

        val question2 = Question(
            id = 2L,
            serverId = 200L,
            eventSlug = "test-event",
            position = 1,
            required = true,
            question = "Email",
            identifier = "email",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.EMAIL
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question1, question2),
            answers = mapOf(100L to "Acme Corporation")
        )

        viewModel.buildQuestionsForm(data)

        val formField1 = viewModel.form.value.first { it.id == 100L }
        assertEquals("Acme Corporation", formField1.value)

        val formField2 = viewModel.form.value.first { it.id == 200L }
        assertNull(formField2.value)
    }

    @Test
    fun `pre-filled text value passes validation when required`() = runTest {
        val textQuestion = Question(
            id = 1L,
            serverId = 100L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Company Name",
            identifier = "company",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.T
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(textQuestion),
            answers = mapOf(100L to "Acme Corporation")
        )

        viewModel.buildQuestionsForm(data)
        val isValid = viewModel.validateForConfirm()

        assertNull(isValid)
        val formField = viewModel.form.value.first { it.id == 100L }
        assertNull(formField.validation)
    }

    @Test
    fun `user can modify pre-filled value and submit new value`() = runTest {
        val textQuestion = Question(
            id = 1L,
            serverId = 100L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Company Name",
            identifier = "company",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.T
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(textQuestion),
            answers = mapOf(100L to "Acme Corporation")
        )

        viewModel.buildQuestionsForm(data)

        val formField1 = viewModel.form.value.first { it.id == 100L }
        assertEquals("Acme Corporation", formField1.value)

        viewModel.updateAnswer(100L, "New Company Name")

        val formField2 = viewModel.form.value.first { it.id == 100L }
        assertEquals("New Company Name", formField2.value)

        val answers = viewModel.getCurrentAnswers(data)
        assertEquals(1, answers.size)
        assertEquals("New Company Name", answers[0].value)
    }

    @Test
    fun `multiple-choice question without pre-answer should not use dependencyValues as initial values`() = runTest {
        val option1 = eu.pretix.libpretixsync.db.QuestionOption(1L, 1L, "vegetarian", "Vegetarian")
        val option2 = eu.pretix.libpretixsync.db.QuestionOption(2L, 2L, "vegan", "Vegan")
        val option3 = eu.pretix.libpretixsync.db.QuestionOption(3L, 3L, "gluten-free", "Gluten-free")

        val multiChoiceQuestion = Question(
            id = 1L,
            serverId = 300L,
            eventSlug = "test-event",
            position = 0,
            required = false,
            question = "Dietary Requirements",
            identifier = "dietary",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.M,
            options = listOf(option1, option2, option3),
            dependencyQuestionServerId = 100L,
            dependencyValues = listOf("1", "2")
        )

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(multiChoiceQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 300L }
        assertNull(formField.values, "values should be null when no answer is provided, not fall back to dependencyValues")
    }

    @Test
    fun `validateForConfirm returns true when non-required photo is empty`() = runTest {
        val photoQuestion = createPhotoQuestion(required = false)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(photoQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)

        val isValid = viewModel.validateForConfirm()
        assertNull(isValid)
    }

    @Test
    fun `validateForConfirm returns false when required photo is empty`() = runTest {
        val photoQuestion = createPhotoQuestion(required = true)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(photoQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)

        val isValid = viewModel.validateForConfirm()
        assertNotNull(isValid)

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.MISSING, formField.validation)
    }

    @Test
    fun `validateForConfirm returns true when required photo is provided`() = runTest {
        val photoQuestion = createPhotoQuestion(required = true)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(photoQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "/tmp/photo.jpg")

        val isValid = viewModel.validateForConfirm()
        assertNull(isValid)
    }

    @Test
    fun `updateAnswer formats file path with file protocol`() = runTest {
        val photoQuestion = createPhotoQuestion()

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(photoQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "/tmp/photo.jpg")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals("file:///tmp/photo.jpg", formField.value)
    }

    @Test
    fun `updateAnswer preserves URL for photo answer`() = runTest {
        val photoQuestion = createPhotoQuestion()

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(photoQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "https://example.com/photo.jpg")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals("https://example.com/photo.jpg", formField.value)
    }

    @Test
    fun `deleting photo answer sets value to null`() = runTest {
        val photoQuestion = createPhotoQuestion()

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(photoQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "/tmp/photo.jpg")
        viewModel.updateAnswer(1L, null)

        val formField = viewModel.form.value.first { it.id == 1L }
        assertNull(formField.value)
    }

    @Test
    fun `non-required photo does not block validation of other fields`() = runTest {
        val textQuestion = Question(
            id = 2L,
            serverId = 2L,
            eventSlug = "test-event",
            position = 0,
            required = true,
            question = "Name",
            identifier = "name",
            askDuringCheckIn = true,
            showDuringCheckIn = true,
            type = QuestionType.T
        )
        val photoQuestion = createPhotoQuestion(serverId = 3L, required = false)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(textQuestion, photoQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(2L, "John")

        val isValid = viewModel.validateForConfirm()
        assertNull(isValid)
    }

    @Test
    fun `getCurrentAnswers returns blank answer for unanswered non-required photo`() = runTest {
        val photoQuestion = createPhotoQuestion(required = false)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(photoQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)

        val answers = viewModel.getCurrentAnswers(data)
        assertEquals(1, answers.size)
        assertEquals("", answers[0].value)
    }

    @Test
    fun `pre-filled photo answer is preserved in form`() = runTest {
        val photoQuestion = createPhotoQuestion(required = false)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(photoQuestion),
            answers = mapOf(1L to "https://example.com/photo.jpg")
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals("https://example.com/photo.jpg", formField.value)
    }

    @Test
    fun `text exceeding maxLength is marked INVALID on validation`() = runTest {
        val textQuestion = createTextQuestion(serverId = 1L, type = QuestionType.S, required = false)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(textQuestion),
            answers = emptyMap(),
            questionMaxLengths = mapOf(1L to 10)
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "this exceeds the limit")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `text within maxLength passes validation`() = runTest {
        val textQuestion = createTextQuestion(serverId = 2L, type = QuestionType.T, required = false)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(textQuestion),
            answers = emptyMap(),
            questionMaxLengths = mapOf(2L to 50)
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(2L, "short")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 2L }
        assertNull(formField.validation)
    }

    @Test
    fun `null maxLength means no length restriction`() = runTest {
        val textQuestion = createTextQuestion(serverId = 3L, type = QuestionType.S, required = false)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(textQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(3L, "a".repeat(1000))
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 3L }
        assertNull(formField.validation)
        assertNull(formField.maxLength)
    }

    @Test
    fun `form field correctly receives maxLength from ResultStateData`() = runTest {
        val textQuestion = createTextQuestion(serverId = 4L, type = QuestionType.T, required = false)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(textQuestion),
            answers = emptyMap(),
            questionMaxLengths = mapOf(4L to 100)
        )

        viewModel.buildQuestionsForm(data)

        val formField = viewModel.form.value.first { it.id == 4L }
        assertEquals(100, formField.maxLength)
    }

    @Test
    fun `pre-filled value exceeding maxLength fails validation`() = runTest {
        val textQuestion = createTextQuestion(serverId = 5L, type = QuestionType.S, required = true)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(textQuestion),
            answers = mapOf(5L to "this is way too long for the limit"),
            questionMaxLengths = mapOf(5L to 5)
        )

        viewModel.buildQuestionsForm(data)
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 5L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `blank optional text with maxLength does not trigger INVALID`() = runTest {
        val textQuestion = createTextQuestion(serverId = 6L, type = QuestionType.T, required = false)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(textQuestion),
            answers = emptyMap(),
            questionMaxLengths = mapOf(6L to 10)
        )

        viewModel.buildQuestionsForm(data)
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 6L }
        assertNull(formField.validation)
    }

    @Test
    fun `number exceeding max is marked INVALID on input`() = runTest {
        val numberQuestion = createNumberQuestion(serverId = 1L)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(numberQuestion),
            answers = emptyMap(),
            questionNumberMax = mapOf(1L to "10")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "11")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `number below min is marked INVALID on input`() = runTest {
        val numberQuestion = createNumberQuestion(serverId = 1L)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(numberQuestion),
            answers = emptyMap(),
            questionNumberMin = mapOf(1L to "5")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "3")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `number within range has no validation error`() = runTest {
        val numberQuestion = createNumberQuestion(serverId = 1L)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(numberQuestion),
            answers = emptyMap(),
            questionNumberMin = mapOf(1L to "1"),
            questionNumberMax = mapOf(1L to "100")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "50")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertNull(formField.validation)
    }

    @Test
    fun `number range validated on submit`() = runTest {
        val numberQuestion = createNumberQuestion(serverId = 1L, required = true)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(numberQuestion),
            answers = emptyMap(),
            questionNumberMin = mapOf(1L to "10"),
            questionNumberMax = mapOf(1L to "20")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "25")
        val isValid = viewModel.validateForConfirm()

        assertNotNull(isValid)
        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `empty optional number with range constraints passes validation`() = runTest {
        val numberQuestion = createNumberQuestion(serverId = 1L, required = false)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(numberQuestion),
            answers = emptyMap(),
            questionNumberMin = mapOf(1L to "5"),
            questionNumberMax = mapOf(1L to "10")
        )

        viewModel.buildQuestionsForm(data)
        val isValid = viewModel.validateForConfirm()

        assertNull(isValid)
        val formField = viewModel.form.value.first { it.id == 1L }
        assertNull(formField.validation)
    }

    @Test
    fun `decimal input is accepted for number questions`() = runTest {
        val numberQuestion = createNumberQuestion(serverId = 1L)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(numberQuestion),
            answers = emptyMap(),
            questionNumberMin = mapOf(1L to "0"),
            questionNumberMax = mapOf(1L to "100")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "3.14")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals("3.14", formField.value)
        assertNull(formField.validation)
    }

    @Test
    fun `unparseable intermediate value like dash passes during input but fails on submit`() = runTest {
        val numberQuestion = createNumberQuestion(serverId = 1L, required = true)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(numberQuestion),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "-")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals("-", formField.value)

        val isValid = viewModel.validateForConfirm()
        assertNotNull(isValid)
        val formFieldAfterSubmit = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formFieldAfterSubmit.validation)
    }

    private fun createNumberQuestion(
        serverId: Long = 1L,
        required: Boolean = false
    ) = Question(
        id = serverId,
        serverId = serverId,
        eventSlug = "test-event",
        position = 0,
        required = required,
        question = "Number",
        identifier = "number-$serverId",
        askDuringCheckIn = true,
        showDuringCheckIn = true,
        type = QuestionType.N
    )

    private fun createTextQuestion(
        serverId: Long,
        type: QuestionType = QuestionType.S,
        required: Boolean = false
    ) = Question(
        id = serverId,
        serverId = serverId,
        eventSlug = "test-event",
        position = 0,
        required = required,
        question = "Text Question",
        identifier = "text-$serverId",
        askDuringCheckIn = true,
        showDuringCheckIn = true,
        type = type
    )

    private fun createTestQuestion() = Question(
        id = 1L,
        serverId = 1L,
        eventSlug = "test-event",
        position = 0,
        required = true,
        question = "Phone Number",
        identifier = "phone",
        askDuringCheckIn = true,
        showDuringCheckIn = true,
        type = QuestionType.TEL
    )

    private fun createPhotoQuestion(
        serverId: Long = 1L,
        required: Boolean = false
    ) = Question(
        id = serverId,
        serverId = serverId,
        eventSlug = "test-event",
        position = 0,
        required = required,
        question = "Photo",
        identifier = "photo",
        askDuringCheckIn = true,
        showDuringCheckIn = true,
        type = QuestionType.F
    )

    private fun createBooleanQuestion(
        serverId: Long = 1L,
        required: Boolean = true
    ) = Question(
        id = serverId,
        serverId = serverId,
        eventSlug = "test-event",
        position = 0,
        required = required,
        question = "Accept Terms",
        identifier = "terms-$serverId",
        askDuringCheckIn = true,
        showDuringCheckIn = true,
        type = QuestionType.B
    )

    private fun createEmailQuestion(
        serverId: Long = 1L,
        required: Boolean = true
    ) = Question(
        id = serverId,
        serverId = serverId,
        eventSlug = "test-event",
        position = 0,
        required = required,
        question = "Email Address",
        identifier = "email-$serverId",
        askDuringCheckIn = true,
        showDuringCheckIn = true,
        type = QuestionType.EMAIL
    )

    private fun createTimeQuestion(
        serverId: Long = 1L,
        required: Boolean = true
    ) = Question(
        id = serverId,
        serverId = serverId,
        eventSlug = "test-event",
        position = 0,
        required = required,
        question = "Preferred Time",
        identifier = "time-$serverId",
        askDuringCheckIn = true,
        showDuringCheckIn = true,
        type = QuestionType.H
    )

    @Test
    fun `required boolean with False value is marked MISSING`() = runTest {
        val question = createBooleanQuestion()
        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "False")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.MISSING, formField.validation)
    }

    @Test
    fun `required boolean with null value is marked MISSING`() = runTest {
        val question = createBooleanQuestion()
        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.MISSING, formField.validation)
    }

    @Test
    fun `required boolean with True value passes validation`() = runTest {
        val question = createBooleanQuestion()
        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "True")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertNull(formField.validation)
    }

    @Test
    fun `invalid email format is marked INVALID`() = runTest {
        val question = createEmailQuestion()
        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "not-an-email")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `valid email passes validation`() = runTest {
        val question = createEmailQuestion()
        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "test@example.com")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertNull(formField.validation)
    }

    @Test
    fun `required empty email is marked MISSING`() = runTest {
        val question = createEmailQuestion()
        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.MISSING, formField.validation)
    }

    @Test
    fun `invalid time format is marked INVALID`() = runTest {
        val question = createTimeQuestion()
        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "invalid")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `valid time passes validation`() = runTest {
        val question = createTimeQuestion()
        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "14:30")
        viewModel.formatAndValidateForm()

        val formField = viewModel.form.value.first { it.id == 1L }
        assertNull(formField.validation)
    }

    private fun createDateQuestion(
        serverId: Long = 1L,
        type: QuestionType = QuestionType.D,
        required: Boolean = false
    ) = Question(
        id = serverId,
        serverId = serverId,
        eventSlug = "test-event",
        position = 0,
        required = required,
        question = "Date",
        identifier = "date-$serverId",
        askDuringCheckIn = true,
        showDuringCheckIn = true,
        type = type
    )

    @Test
    fun `date before min is marked INVALID on input`() = runTest {
        val question = createDateQuestion(serverId = 1L)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap(),
            questionDateMin = mapOf(1L to "2024-06-01")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "2024-05-31")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `date after max is marked INVALID on input`() = runTest {
        val question = createDateQuestion(serverId = 1L)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap(),
            questionDateMax = mapOf(1L to "2024-06-30")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "2024-07-01")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `date within range has no validation error`() = runTest {
        val question = createDateQuestion(serverId = 1L)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap(),
            questionDateMin = mapOf(1L to "2024-06-01"),
            questionDateMax = mapOf(1L to "2024-06-30")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "2024-06-15")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertNull(formField.validation)
    }

    @Test
    fun `date range validated on submit`() = runTest {
        val question = createDateQuestion(serverId = 1L, required = true)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap(),
            questionDateMin = mapOf(1L to "2024-06-01"),
            questionDateMax = mapOf(1L to "2024-06-30")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "2024-07-15")
        val isValid = viewModel.validateForConfirm()

        assertNotNull(isValid)
        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `required blank date is marked MISSING on submit`() = runTest {
        val question = createDateQuestion(serverId = 1L, required = true)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        val isValid = viewModel.validateForConfirm()

        assertNotNull(isValid)
        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.MISSING, formField.validation)
    }

    @Test
    fun `datetime before min is marked INVALID on input`() = runTest {
        val question = createDateQuestion(serverId = 1L, type = QuestionType.W)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap(),
            questionDateTimeMin = mapOf(1L to "2024-06-15T10:00")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "2024-06-15T09:59")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `datetime after max is marked INVALID on input`() = runTest {
        val question = createDateQuestion(serverId = 1L, type = QuestionType.W)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap(),
            questionDateTimeMax = mapOf(1L to "2024-06-15T18:00")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "2024-06-15T18:01")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `datetime within range has no validation error`() = runTest {
        val question = createDateQuestion(serverId = 1L, type = QuestionType.W)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap(),
            questionDateTimeMin = mapOf(1L to "2024-06-15T10:00"),
            questionDateTimeMax = mapOf(1L to "2024-06-15T18:00")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "2024-06-15T14:00")

        val formField = viewModel.form.value.first { it.id == 1L }
        assertNull(formField.validation)
    }

    @Test
    fun `datetime range validated on submit`() = runTest {
        val question = createDateQuestion(serverId = 1L, type = QuestionType.W, required = true)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap(),
            questionDateTimeMin = mapOf(1L to "2024-06-15T10:00"),
            questionDateTimeMax = mapOf(1L to "2024-06-15T18:00")
        )

        viewModel.buildQuestionsForm(data)
        viewModel.updateAnswer(1L, "2024-06-15T19:00")
        val isValid = viewModel.validateForConfirm()

        assertNotNull(isValid)
        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.INVALID, formField.validation)
    }

    @Test
    fun `required blank datetime is marked MISSING on submit`() = runTest {
        val question = createDateQuestion(serverId = 1L, type = QuestionType.W, required = true)

        val data = ResultStateData(
            resultState = ResultState.DIALOG_QUESTIONS,
            requiredQuestions = listOf(question),
            answers = emptyMap()
        )

        viewModel.buildQuestionsForm(data)
        val isValid = viewModel.validateForConfirm()

        assertNotNull(isValid)
        val formField = viewModel.form.value.first { it.id == 1L }
        assertEquals(FieldValidationState.MISSING, formField.validation)
    }
}
