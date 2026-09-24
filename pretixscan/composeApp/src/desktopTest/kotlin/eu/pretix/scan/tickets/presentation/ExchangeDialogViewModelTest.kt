package eu.pretix.scan.tickets.presentation

import eu.pretix.desktop.app.ui.SelectableValue
import eu.pretix.desktop.nfc.NfcReadEvent
import eu.pretix.desktop.nfc.NfcReaderService
import eu.pretix.desktop.nfc.NfcState
import eu.pretix.libpretixnfc.communication.ChipReadError
import eu.pretix.libpretixsync.db.MediaPolicy
import eu.pretix.libpretixsync.db.ReusableMediaType
import eu.pretix.scan.tickets.data.ExchangeSupport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.nfc_read_error
import pretixscan.composeapp.generated.resources.reusable_media_exchange_nfc_needs_nfc_uid
import pretixscan.composeapp.generated.resources.reusable_media_exchange_unknown_type
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeDialogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val readerService = FakeNfcReaderService()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        readerService.state.value = NfcState.RUNNING
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a chip of the required type is accepted`() = runTest {
        val viewModel = ExchangeDialogViewModel(readerService)
        viewModel.start(ReusableMediaType.NFC_UID, MediaPolicy.REUSE)
        testScheduler.advanceUntilIdle()

        assertEquals(ExchangeSupport.SUPPORTED, viewModel.uiState.value.support)

        readerService.events.emit(NfcReadEvent.Success("04AABBCC", ReusableMediaType.NFC_UID))
        testScheduler.advanceUntilIdle()

        assertEquals("04AABBCC" to ReusableMediaType.NFC_UID, viewModel.uiState.value.scannedMedium)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `a chip of another type is rejected with the mismatch message`() = runTest {
        val viewModel = ExchangeDialogViewModel(readerService)
        viewModel.start(ReusableMediaType.NFC_UID, MediaPolicy.REUSE)
        testScheduler.advanceUntilIdle()

        readerService.events.emit(NfcReadEvent.Success("1234", ReusableMediaType.NFC_MF0AES))
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.scannedMedium)
        assertEquals(Res.string.reusable_media_exchange_nfc_needs_nfc_uid, viewModel.uiState.value.error)
    }

    @Test
    fun `a chip without a media type usable by the server is rejected`() = runTest {
        val viewModel = ExchangeDialogViewModel(readerService)
        viewModel.start(ReusableMediaType.NFC_UID, MediaPolicy.REUSE)
        testScheduler.advanceUntilIdle()

        readerService.events.emit(NfcReadEvent.Success("04AABBCC", ReusableMediaType.NONE))
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.scannedMedium)
        assertEquals(Res.string.reusable_media_exchange_unknown_type, viewModel.uiState.value.error)
    }

    @Test
    fun `a chip read error is shown`() = runTest {
        val viewModel = ExchangeDialogViewModel(readerService)
        viewModel.start(ReusableMediaType.NFC_UID, MediaPolicy.REUSE)
        testScheduler.advanceUntilIdle()

        readerService.events.emit(NfcReadEvent.Error(ChipReadError.IO_ERROR))
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.scannedMedium)
        assertEquals(Res.string.nfc_read_error, viewModel.uiState.value.error)
    }

    @Test
    fun `encoding a new chip is not implemented and no chip is read`() = runTest {
        val viewModel = ExchangeDialogViewModel(readerService)
        viewModel.start(ReusableMediaType.NFC_MF0AES, MediaPolicy.NEW)
        testScheduler.advanceUntilIdle()

        assertEquals(ExchangeSupport.NOT_IMPLEMENTED, viewModel.uiState.value.support)

        readerService.events.emit(NfcReadEvent.Success("1234", ReusableMediaType.NFC_MF0AES))
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.scannedMedium)
    }

    @Test
    fun `stop ends the chip read collection`() = runTest {
        val viewModel = ExchangeDialogViewModel(readerService)
        viewModel.start(ReusableMediaType.NFC_UID, MediaPolicy.REUSE)
        testScheduler.advanceUntilIdle()

        viewModel.stop()
        testScheduler.advanceUntilIdle()

        readerService.events.emit(NfcReadEvent.Success("04AABBCC", ReusableMediaType.NFC_UID))
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.scannedMedium)
    }

    @Test
    fun `stop forgets the chip of the finished exchange`() = runTest {
        val viewModel = ExchangeDialogViewModel(readerService)
        viewModel.start(ReusableMediaType.NFC_UID, MediaPolicy.REUSE)
        testScheduler.advanceUntilIdle()

        readerService.events.emit(NfcReadEvent.Success("04AABBCC", ReusableMediaType.NFC_UID))
        testScheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.scannedMedium)

        viewModel.stop()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.scannedMedium)
    }
}

private class FakeNfcReaderService : NfcReaderService {
    override val state = MutableStateFlow(NfcState.STOPPED)

    override val events = MutableSharedFlow<NfcReadEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun start() = Unit

    override fun stop() = Unit

    override fun listReaders(): List<SelectableValue> = emptyList()
}
