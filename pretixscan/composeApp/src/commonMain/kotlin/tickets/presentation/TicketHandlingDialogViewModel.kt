package tickets.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tickets.data.ResultState
import tickets.data.ResultStateData
import tickets.data.TicketCodeHandler
import java.util.logging.Logger

class TicketHandlingDialogViewModel(
    private val tickerCodeHandler: TicketCodeHandler
) : ViewModel() {

    private val log = Logger.getLogger("tickets")

    private val _uiState = MutableStateFlow(ResultStateData(resultState = ResultState.EMPTY))
    val uiState = _uiState.asStateFlow()


    suspend fun handleTicket(secret: String?, ignoreUnpaid: Boolean = false) {
        log.info("Handling ticket $secret")
        _uiState.update {
            it.copy(resultState = ResultState.LOADING)
        }
        val result = tickerCodeHandler.handleScanResult(
            secret,
            answers = null,
            ignoreUnpaid = ignoreUnpaid
        )
        _uiState.update {
            result
        }
    }
}