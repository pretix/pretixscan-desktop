package eu.pretix.scan.tickets.presentation

import androidx.lifecycle.ViewModel
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.printing.BadgeFactory
import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.libpretixsync.db.NonceGenerator
import eu.pretix.scan.tickets.data.ResultState
import eu.pretix.scan.tickets.data.ResultStateData
import eu.pretix.scan.tickets.data.TicketCodeHandler
import eu.pretix.scan.tickets.data.shouldAutoPrint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.logging.Logger

class TicketHandlingDialogViewModel(
    private val tickerCodeHandler: TicketCodeHandler,
    private val badgeFactory: BadgeFactory,
    private val appConfig: DataStoreConfigStore,
    private val appCache: AppCache,
    private val api: PretixApi,
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
            logSuccessfulPrint()
        } catch (e: Exception) {
            _localTicketHandlingErrors.update { TicketHandlingErrors.Error(e.localizedMessage) }
        }
    }

    private fun logSuccessfulPrint() {
        val positionId = _uiState.value.position?.optLong("id", 0L) ?: 0L
        val eventSlug = _uiState.value.eventSlug

        if (positionId <= 0L || eventSlug.isNullOrBlank()) {
            log.warning("Cannot log print: positionId=$positionId eventSlug=$eventSlug")
            return
        }

        try {
            val logbody = JSONObject()
            logbody.put("source", "pretixSCAN")
            logbody.put("type", "badge")
            logbody.put("info", JSONObject())
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")
            logbody.put("datetime", df.format(Date()))

            appCache.db.queuedCallQueries.insert(
                body = logbody.toString(),
                idempotency_key = NonceGenerator.nextNonce(),
                url = api.eventResourceUrl(eventSlug, "orderpositions") + positionId + "/printlog/",
            )
        } catch (e: Exception) {
            log.warning("Failed to queue printlog: ${e.message}")
        }
    }
}

sealed class TicketHandlingErrors<out T> {
    object None : TicketHandlingErrors<Nothing>()
    data class Error(val exception: String) : TicketHandlingErrors<Nothing>()
}