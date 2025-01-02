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

    private val log = Logger.getLogger("TicketHandlingViewModel")

    private val _uiState = MutableStateFlow(ResultStateData(resultState = ResultState.EMPTY))
    val uiState = _uiState.asStateFlow()


    suspend fun handleTicket(secret: String?) {
        log.info("Handling ticket $secret")
        _uiState.update {
            it.copy(resultState = ResultState.LOADING)
        }
        val result = tickerCodeHandler.handleScanResult(secret)
        _uiState.update {
            result
        }
    }
}