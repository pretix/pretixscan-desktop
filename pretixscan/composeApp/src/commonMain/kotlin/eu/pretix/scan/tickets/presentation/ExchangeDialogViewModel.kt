package eu.pretix.scan.tickets.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.pretix.desktop.nfc.NfcReadEvent
import eu.pretix.desktop.nfc.NfcReaderService
import eu.pretix.desktop.nfc.NfcState
import eu.pretix.desktop.nfc.stringResource
import eu.pretix.libpretixsync.db.MediaPolicy
import eu.pretix.libpretixsync.db.ReusableMediaType
import eu.pretix.scan.tickets.data.ExchangeSupport
import eu.pretix.scan.tickets.data.exchangeSupport
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.reusable_media_exchange_nfc_needs_nfc_mf0aes
import pretixscan.composeapp.generated.resources.reusable_media_exchange_nfc_needs_nfc_uid
import pretixscan.composeapp.generated.resources.reusable_media_exchange_nfc_needs_nfc_unknown
import pretixscan.composeapp.generated.resources.reusable_media_exchange_unknown_type
import java.util.logging.Logger

data class ExchangeDialogUiState(
    val support: ExchangeSupport = ExchangeSupport.NOT_IMPLEMENTED,
    val nfcState: NfcState = NfcState.STOPPED,
    val error: StringResource? = null,
    val scannedMedium: Pair<String, ReusableMediaType>? = null
)

class ExchangeDialogViewModel(
    private val nfcReaderService: NfcReaderService
) : ViewModel() {

    private val log = Logger.getLogger("ExchangeDialogViewModel")

    private val _uiState = MutableStateFlow(ExchangeDialogUiState())
    val uiState: StateFlow<ExchangeDialogUiState> = _uiState.asStateFlow()

    private var readerJob: Job? = null

    fun start(requiredMediaType: ReusableMediaType?, requiredMediaPolicy: MediaPolicy?) {
        readerJob?.cancel()

        val nfcState = nfcReaderService.state.value
        val support = exchangeSupport(requiredMediaType, requiredMediaPolicy, nfcState)
        _uiState.value = ExchangeDialogUiState(support = support, nfcState = nfcState)
        log.info("Medium exchange into $requiredMediaType under $requiredMediaPolicy is $support")

        if (support != ExchangeSupport.SUPPORTED) {
            return
        }

        readerJob = viewModelScope.launch {
            launch {
                nfcReaderService.state.collect { state ->
                    _uiState.update { it.copy(nfcState = state) }
                }
            }
            nfcReaderService.events.collect { event ->
                onChipRead(event, requiredMediaType)
            }
        }
    }

    fun stop() {
        readerJob?.cancel()
        readerJob = null
        _uiState.value = ExchangeDialogUiState()
    }

    private fun onChipRead(event: NfcReadEvent, requiredMediaType: ReusableMediaType?) {
        when (event) {
            is NfcReadEvent.Success -> {
                val unusableType = event.mediaType == ReusableMediaType.NONE ||
                    event.mediaType == ReusableMediaType.UNSUPPORTED
                if (unusableType) {
                    _uiState.update { it.copy(error = Res.string.reusable_media_exchange_unknown_type) }
                    return
                }
                if (event.mediaType != requiredMediaType) {
                    _uiState.update { it.copy(error = mismatchError(requiredMediaType)) }
                    return
                }
                _uiState.update {
                    it.copy(error = null, scannedMedium = event.identifier to event.mediaType)
                }
            }

            is NfcReadEvent.Error -> _uiState.update { it.copy(error = event.error.stringResource()) }
        }
    }

    private fun mismatchError(requiredMediaType: ReusableMediaType?): StringResource =
        when (requiredMediaType) {
            ReusableMediaType.NFC_UID -> Res.string.reusable_media_exchange_nfc_needs_nfc_uid
            ReusableMediaType.NFC_MF0AES -> Res.string.reusable_media_exchange_nfc_needs_nfc_mf0aes
            else -> Res.string.reusable_media_exchange_nfc_needs_nfc_unknown
        }
}
