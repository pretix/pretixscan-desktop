package eu.pretix.scan.tickets.presentation

import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.printing.BadgeFactory
import eu.pretix.desktop.printing.BadgePrinterUnavailableException
import eu.pretix.libpretixsync.models.BadgeLayout
import eu.pretix.scan.tickets.data.BadgePrintPolicy
import eu.pretix.scan.tickets.data.ResultState
import eu.pretix.scan.tickets.data.ResultStateData
import eu.pretix.scan.tickets.data.TicketCodeHandler
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.json.JSONObject
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.badge_printing_selected_printer_not_available
import java.awt.print.PrinterException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class TicketHandlingDialogViewModelTest {

    private lateinit var config: DataStoreConfigStore
    private lateinit var ticketCodeHandler: TicketCodeHandler
    private val testDispatcher = StandardTestDispatcher()

    private class FailingBadgeFactory(private val failure: Exception) : BadgeFactory {
        override suspend fun setup() {}

        override fun printBadges(layout: BadgeLayout?, position: JSONObject) {
            throw failure
        }
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        config = mockk(relaxed = true)
        every { config.uiReduceMotion } returns false
        every { config.autoPrintBadges } returns BadgePrintPolicy.WHEN_BUTTON_PRESSED
        ticketCodeHandler = mockk()
        coEvery { ticketCodeHandler.handleScanResult(any(), any(), any()) } answers {
            ResultStateData(
                resultState = ResultState.SUCCESS,
                badgeLayout = BadgeLayout.defaultWithLayout("[]"),
                position = JSONObject().put("id", 1L),
                eventSlug = "demo"
            )
        }
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(failure: Exception) = TicketHandlingDialogViewModel(
        tickerCodeHandler = ticketCodeHandler,
        badgeFactory = FailingBadgeFactory(failure),
        appConfig = config,
        appCache = mockk(relaxed = true),
        api = mockk(relaxed = true),
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `unavailable badge printer is shown with a localized message`() = runTest {
        val viewModel = createViewModel(BadgePrinterUnavailableException.NotFound("Printer"))

        viewModel.handleTicket("A")
        viewModel.printBadges()
        testScheduler.advanceUntilIdle()

        assertEquals(
            TicketHandlingErrors.Error(Res.string.badge_printing_selected_printer_not_available),
            viewModel.localTicketHandlingErrors.value
        )
    }

    @Test
    fun `printer job failure is not shown`() = runTest {
        val viewModel = createViewModel(PrinterException("jam"))

        viewModel.handleTicket("A")
        viewModel.printBadges()
        testScheduler.advanceUntilIdle()

        assertEquals(TicketHandlingErrors.None, viewModel.localTicketHandlingErrors.value)
    }

    @Test
    fun `print error is discarded when the ticket is scanned again before the print fails`() = runTest {
        val viewModel = createViewModel(BadgePrinterUnavailableException.NotSelected())

        viewModel.handleTicket("A")
        viewModel.printBadges()
        viewModel.handleTicket("A")
        testScheduler.advanceUntilIdle()

        assertEquals(TicketHandlingErrors.None, viewModel.localTicketHandlingErrors.value)
    }

    @Test
    fun `resetting the ticket state clears a shown print error`() = runTest {
        val viewModel = createViewModel(BadgePrinterUnavailableException.NotFound("Printer"))
        viewModel.handleTicket("A")
        viewModel.printBadges()
        testScheduler.advanceUntilIdle()
        assertIs<TicketHandlingErrors.Error>(viewModel.localTicketHandlingErrors.value)

        viewModel.resetTicketHandlingState()

        assertEquals(TicketHandlingErrors.None, viewModel.localTicketHandlingErrors.value)
    }
}
