package eu.pretix.scan.main.presentation

import eu.pretix.desktop.app.sync.SyncRootService
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.cache.EventSelection
import eu.pretix.scan.tickets.data.ResultState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var appConfig: DataStoreConfigStore
    private lateinit var syncViewModel: SyncRootService
    private lateinit var appCache: AppCache
    private lateinit var viewModel: MainViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testEventSelection = EventSelection(
        eventSlug = "test-event",
        eventName = "Test Event",
        subEventId = null,
        checkInListId = 1L,
        checkInListName = "Main List",
        dateFrom = null,
        dateTo = null
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        appConfig = mockk(relaxed = true)
        syncViewModel = mockk(relaxed = true)
        appCache = mockk(relaxed = true)

        every { appConfig.activeEvent } returns testEventSelection
        every { appConfig.scanType } returns "entry"
        every { appConfig.eventSelections } returns listOf(testEventSelection)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MainViewModel {
        return MainViewModel(appConfig, syncViewModel, appCache)
    }

    @Test
    fun `loadViewModel transitions to ReadyToScan when active event exists`() = runTest {
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MainUiState.ReadyToScan, "Expected ReadyToScan but got ${state::class.simpleName}")
        assertEquals(testEventSelection, (state as MainUiState.ReadyToScan).data.eventSelection)
    }

    @Test
    fun `onHandleDirectScan transitions from ReadyToScan to HandlingTicket`() = runTest {
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertTrue(initialState is MainUiState.ReadyToScan, "Expected initial state ReadyToScan")

        viewModel.onHandleDirectScan("TEST123")
        testScheduler.advanceUntilIdle()

        val newState = viewModel.uiState.value
        assertTrue(newState is MainUiState.HandlingTicket, "Expected HandlingTicket but got ${newState::class.simpleName}")
        assertEquals("TEST123", (newState as MainUiState.HandlingTicket).data.secret)
    }

    @Test
    fun `onHandleDirectScan blocks new scan when current dialog requires user interaction - DIALOG_QUESTIONS`() = runTest {
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("FIRST_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterFirstScan = viewModel.uiState.value
        assertTrue(stateAfterFirstScan is MainUiState.HandlingTicket)

        viewModel.onTicketResultDetermined(ResultState.DIALOG_QUESTIONS)
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("SECOND_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterSecondScan = viewModel.uiState.value
        assertTrue(stateAfterSecondScan is MainUiState.HandlingTicket, "Should still be HandlingTicket")
        assertEquals("FIRST_SCAN", (stateAfterSecondScan as MainUiState.HandlingTicket).data.secret,
            "Secret should NOT change - scan should be blocked")
    }

    @Test
    fun `onHandleDirectScan blocks new scan when current dialog requires user interaction - DIALOG_UNPAID`() = runTest {
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("FIRST_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterFirstScan = viewModel.uiState.value
        assertTrue(stateAfterFirstScan is MainUiState.HandlingTicket)

        viewModel.onTicketResultDetermined(ResultState.DIALOG_UNPAID)
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("SECOND_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterSecondScan = viewModel.uiState.value
        assertTrue(stateAfterSecondScan is MainUiState.HandlingTicket, "Should still be HandlingTicket")
        assertEquals("FIRST_SCAN", (stateAfterSecondScan as MainUiState.HandlingTicket).data.secret,
            "Secret should NOT change - scan should be blocked")
    }

    @Test
    fun `onHandleDirectScan blocks new scan when current dialog requires user interaction - ANSWERS_REQUIRED`() = runTest {
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("FIRST_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterFirstScan = viewModel.uiState.value
        assertTrue(stateAfterFirstScan is MainUiState.HandlingTicket)

        viewModel.onTicketResultDetermined(ResultState.DIALOG_QUESTIONS)
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("SECOND_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterSecondScan = viewModel.uiState.value
        assertTrue(stateAfterSecondScan is MainUiState.HandlingTicket, "Should still be HandlingTicket")
        assertEquals("FIRST_SCAN", (stateAfterSecondScan as MainUiState.HandlingTicket).data.secret,
            "Secret should NOT change - scan should be blocked")
    }

    @Test
    fun `onHandleDirectScan blocks new scan when dialog is still loading - null resultState`() = runTest {
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("FIRST_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterFirstScan = viewModel.uiState.value
        assertTrue(stateAfterFirstScan is MainUiState.HandlingTicket)
        assertNull((stateAfterFirstScan as MainUiState.HandlingTicket).data.resultState,
            "resultState should be null before dialog determines its result")

        viewModel.onHandleDirectScan("SECOND_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterSecondScan = viewModel.uiState.value
        assertTrue(stateAfterSecondScan is MainUiState.HandlingTicket, "Should still be HandlingTicket")
        assertEquals("FIRST_SCAN", (stateAfterSecondScan as MainUiState.HandlingTicket).data.secret,
            "Secret should NOT change - scan should be blocked while dialog is loading")
    }

    @Test
    fun `onHandleDirectScan allows interruption when current dialog is auto-dismissible - SUCCESS`() = runTest {
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("FIRST_SCAN")
        testScheduler.advanceUntilIdle()

        viewModel.onTicketResultDetermined(ResultState.SUCCESS)
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("SECOND_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterSecondScan = viewModel.uiState.value
        assertTrue(stateAfterSecondScan is MainUiState.HandlingTicket, "Should be HandlingTicket")
        assertEquals("SECOND_SCAN", (stateAfterSecondScan as MainUiState.HandlingTicket).data.secret,
            "Secret SHOULD change - scan should interrupt SUCCESS dialog")
    }

    @Test
    fun `onHandleDirectScan allows interruption when current dialog is auto-dismissible - SUCCESS_EXIT`() = runTest {
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("FIRST_SCAN")
        testScheduler.advanceUntilIdle()

        viewModel.onTicketResultDetermined(ResultState.SUCCESS_EXIT)
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("SECOND_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterSecondScan = viewModel.uiState.value
        assertTrue(stateAfterSecondScan is MainUiState.HandlingTicket, "Should be HandlingTicket")
        assertEquals("SECOND_SCAN", (stateAfterSecondScan as MainUiState.HandlingTicket).data.secret,
            "Secret SHOULD change - scan should interrupt SUCCESS_EXIT dialog")
    }

    @Test
    fun `onHandleDirectScan allows interruption when current dialog is auto-dismissible - ERROR`() = runTest {
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("FIRST_SCAN")
        testScheduler.advanceUntilIdle()

        viewModel.onTicketResultDetermined(ResultState.ERROR)
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("SECOND_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterSecondScan = viewModel.uiState.value
        assertTrue(stateAfterSecondScan is MainUiState.HandlingTicket, "Should be HandlingTicket")
        assertEquals("SECOND_SCAN", (stateAfterSecondScan as MainUiState.HandlingTicket).data.secret,
            "Secret SHOULD change - scan should interrupt ERROR dialog")
    }

    @Test
    fun `onHandleDirectScan allows interruption when current dialog is auto-dismissible - WARNING`() = runTest {
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("FIRST_SCAN")
        testScheduler.advanceUntilIdle()

        viewModel.onTicketResultDetermined(ResultState.WARNING)
        testScheduler.advanceUntilIdle()

        viewModel.onHandleDirectScan("SECOND_SCAN")
        testScheduler.advanceUntilIdle()

        val stateAfterSecondScan = viewModel.uiState.value
        assertTrue(stateAfterSecondScan is MainUiState.HandlingTicket, "Should be HandlingTicket")
        assertEquals("SECOND_SCAN", (stateAfterSecondScan as MainUiState.HandlingTicket).data.secret,
            "Secret SHOULD change - scan should interrupt WARNING dialog")
    }
}
