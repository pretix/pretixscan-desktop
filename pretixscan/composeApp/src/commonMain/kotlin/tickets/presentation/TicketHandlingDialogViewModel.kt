package tickets.presentation

import androidx.lifecycle.ViewModel
import eu.pretix.desktop.printing.BadgeFactory
import eu.pretix.libpretixsync.db.Answer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tickets.data.ResultState
import tickets.data.ResultStateData
import tickets.data.TicketCodeHandler
import java.util.logging.Logger

class TicketHandlingDialogViewModel(
    private val tickerCodeHandler: TicketCodeHandler,
    private val badgeFactory: BadgeFactory
) : ViewModel() {

    private val log = Logger.getLogger("TicketHandlingDialogViewModel")

    private val _uiState = MutableStateFlow(ResultStateData(resultState = ResultState.EMPTY))
    val uiState = _uiState.asStateFlow()

    fun cancelQuestions() {
        _uiState.value = ResultStateData(resultState = ResultState.EMPTY)
    }

    suspend fun handleTicket(secret: String?, answers: List<Answer>? = null, ignoreUnpaid: Boolean = false) {
        log.info("Handling ticket $secret")
        _uiState.update {
            it.copy(resultState = ResultState.LOADING)
        }
        val result = tickerCodeHandler.handleScanResult(
            secret,
            answers = answers,
            ignoreUnpaid = ignoreUnpaid
        )
        _uiState.update {
            result
        }
    }

    suspend fun printBadges() {
        log.info("Printing badges")
        val layout = _uiState.value.badgeLayout
        val position = _uiState.value.position
        if (layout == null) {
            log.warning("No layout, aborting print")
            return
        }

        if (position == null) {
            log.warning("No position, aborting print")
            return
        }


        badgeFactory.setup()
        badgeFactory.printBadges(layout, position)
    }
}