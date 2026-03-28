package eu.pretix.scan.tickets.presentation

import androidx.lifecycle.ViewModel
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.printing.BadgeFactory
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.scan.tickets.data.ResultState
import eu.pretix.scan.tickets.data.ResultStateData
import eu.pretix.scan.tickets.data.TicketCodeHandler
import eu.pretix.scan.tickets.data.shouldAutoPrint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.logging.Logger

class TicketHandlingDialogViewModel(
    private val tickerCodeHandler: TicketCodeHandler,
    private val badgeFactory: BadgeFactory,
    private val appConfig: DataStoreConfigStore,
) : ViewModel() {

    private val log = Logger.getLogger("TicketHandlingDialogViewModel")

    private val _uiState = MutableStateFlow(ResultStateData(resultState = ResultState.EMPTY))
    val uiState = _uiState.asStateFlow()

    fun resetTicketHandlingState() {
        _uiState.value = ResultStateData(resultState = ResultState.EMPTY)
    }

    private val _localTicketHandlingErrors = MutableStateFlow<TicketHandlingErrors<String>>(TicketHandlingErrors.None)
    val localTicketHandlingErrors: StateFlow<TicketHandlingErrors<String>> = _localTicketHandlingErrors

    fun dismissError() {
        _localTicketHandlingErrors.update { TicketHandlingErrors.None }
    }

    suspend fun handleTicket(secret: String?, answers: List<Answer>? = null, ignoreUnpaid: Boolean = false) {
        log.info("Handling ticket")
        _uiState.update {
            ResultStateData(resultState = ResultState.LOADING)
        }
        val result = tickerCodeHandler.handleScanResult(
            secret,
            answers = answers,
            ignoreUnpaid = ignoreUnpaid
        )
        _uiState.update {
            result
        }
        if (result.isPrintable && shouldAutoPrint(
                autoPrintBadges = appConfig.autoPrintBadges,
                resultState = result.resultState,
                position = result.position
            )
        ) {
            log.info("Auto-printing badge")
            printBadges()
        }
    }

    suspend fun printBadges() {
        log.info("User requested to print a badge")
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

        try {
            badgeFactory.setup()
            badgeFactory.printBadges(layout, position)
        } catch (e: Exception) {
            _localTicketHandlingErrors.update { TicketHandlingErrors.Error(e.localizedMessage) }
        }
    }
}

sealed class TicketHandlingErrors<out T> {
    object None : TicketHandlingErrors<Nothing>()
    data class Error(val exception: String) : TicketHandlingErrors<Nothing>()
}