package screen.main.tickets

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tickets.TicketCodeHandler
import java.util.logging.Logger

class TicketHandlingViewModel(
    private val tickerCodeHandler: TicketCodeHandler
) : ViewModel() {

    private val log = Logger.getLogger("TicketHandlingViewModel")

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    suspend fun handleTicket(secret: String?) {
        log.info("Handling ticket $secret")
        val result = tickerCodeHandler.handleScan(secret)
        println("Result ${result.type}")
    }
}