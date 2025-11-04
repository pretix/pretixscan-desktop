package eu.pretix.scan.tickets.presentation

import eu.pretix.desktop.app.ui.FieldValidationState
import eu.pretix.desktop.cache.DataStoreConfigStore
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
    private lateinit var viewModel: QuestionsDialogViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        config = mockk(relaxed = true)
        every { config.uiReduceMotion } returns false
        every { config.uiHideNames } returns false
        viewModel = QuestionsDialogViewModel(config)
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
        assertTrue(isValid)
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
        assertFalse(isValid)
    }

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
}
