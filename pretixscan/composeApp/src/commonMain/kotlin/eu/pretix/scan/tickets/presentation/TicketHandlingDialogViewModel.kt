package eu.pretix.scan.tickets.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.printing.BadgeFactory
import eu.pretix.desktop.printing.BadgePrinterUnavailableException
import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.libpretixsync.db.NonceGenerator
import eu.pretix.scan.tickets.data.ResultState
import eu.pretix.scan.tickets.data.ResultStateData
import eu.pretix.scan.tickets.data.TicketCodeHandler
import eu.pretix.scan.tickets.data.isPreviouslyPrinted
import eu.pretix.scan.tickets.data.shouldAutoPrint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.json.JSONObject
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.badge_printing_no_printer_selected
import pretixscan.composeapp.generated.resources.badge_printing_selected_printer_not_available
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.logging.Level
import java.util.logging.Logger

class TicketHandlingDialogViewModel(
    private val tickerCodeHandler: TicketCodeHandler,
    private val badgeFactory: BadgeFactory,
    private val appConfig: DataStoreConfigStore,
    private val appCache: AppCache,
    private val api: PretixApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val log = Logger.getLogger("TicketHandlingDialogViewModel")

    private val printMutex = Mutex()

    private val _uiState = MutableStateFlow(ResultStateData(resultState = ResultState.EMPTY))
    val uiState = _uiState.asStateFlow()

    private val _uiBlinkSpecialTickets = MutableStateFlow(true)
    val uiBlinkSpecialTickets = _uiBlinkSpecialTickets.asStateFlow()

    fun resetTicketHandlingState() {
        _uiState.value = ResultStateData(resultState = ResultState.EMPTY)
        _localTicketHandlingErrors.value = TicketHandlingErrors.None
    }

    private val _localTicketHandlingErrors = MutableStateFlow<TicketHandlingErrors<String>>(TicketHandlingErrors.None)
    val localTicketHandlingErrors: StateFlow<TicketHandlingErrors<String>> = _localTicketHandlingErrors

    fun dismissError() {
        _localTicketHandlingErrors.update { TicketHandlingErrors.None }
    }

    suspend fun handleTicket(secret: String?, answers: List<Answer>? = null, ignoreUnpaid: Boolean = false) {
        log.info("Handling ticket")
        _uiBlinkSpecialTickets.value = !appConfig.uiReduceMotion
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
        log.info("Auto-print check: isPrintable=${result.isPrintable}, autoPrintBadgesPolicy=${appConfig.autoPrintBadges}, resultState=${result.resultState}, hasPosition=${result.position != null}, previouslyPrinted=${result.position?.let { isPreviouslyPrinted(it) }}")
        if (result.isPrintable && shouldAutoPrint(
                policy = appConfig.autoPrintBadges,
                resultState = result.resultState,
                position = result.position
            )
        ) {
            log.info("Auto-printing badge")
            printBadges()
        }
    }

    fun printBadges() {
        log.info("User requested to print a badge")
        val layout = _uiState.value.badgeLayout
        val position = _uiState.value.position
        val eventSlug = _uiState.value.eventSlug
        if (layout == null) {
            log.warning("No layout, aborting print")
            return
        }

        if (position == null) {
            log.warning("No position, aborting print")
            return
        }

        viewModelScope.launch {
            printMutex.withLock {
                try {
                    withContext(ioDispatcher) {
                        badgeFactory.setup()
                        badgeFactory.printBadges(layout, position)
                        logSuccessfulPrint(position, eventSlug)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: BadgePrinterUnavailableException) {
                    log.warning("Badge printer unavailable: ${e.message}")
                    showPrintError(position, e.userMessage())
                } catch (e: Exception) {
                    log.log(Level.WARNING, "Badge printing failed", e)
                }
            }
        }
    }

    private fun BadgePrinterUnavailableException.userMessage(): StringResource = when (this) {
        is BadgePrinterUnavailableException.NotSelected -> Res.string.badge_printing_no_printer_selected
        is BadgePrinterUnavailableException.NotFound -> Res.string.badge_printing_selected_printer_not_available
    }

    private fun showPrintError(position: JSONObject, message: StringResource) {
        if (_uiState.value.position !== position) {
            log.info("Discarding badge print error for a ticket that is no longer shown")
            return
        }
        _localTicketHandlingErrors.value = TicketHandlingErrors.Error(message)
    }

    private fun logSuccessfulPrint(position: JSONObject, eventSlug: String?) {
        val positionId = position.optLong("id", 0L)

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
    data class Error(val message: StringResource) : TicketHandlingErrors<Nothing>()
}