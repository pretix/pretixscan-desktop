package tickets.data

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.SentryInterface
import eu.pretix.libpretixsync.check.OnlineCheckProvider
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.libpretixsync.models.db.toModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import pretixscan.composeapp.generated.resources.*
import java.text.SimpleDateFormat

@OptIn(ExperimentalResourceApi::class)
class TicketCodeHandler(
    private val conf: AppConfig,
    private val appCache: AppCache,
    private val checkProvider: TicketCheckProvider,
    private val audioPlayer: GadulkaPlayer,
    private val logHandler: SentryInterface,
    private val connectivityHelper: ConnectivityHelper
) {

    suspend fun handleScanResult(rawResult: String?, answers: List<Answer>? = null, ignoreUnpaid: Boolean): ResultStateData {
        val checkResult = handleScan(rawResult, answers, ignoreUnpaid)

        val scannedEvent = calculateScannedEvent(checkResult.eventSlug)

        val questions = checkResult.requiredAnswers?.map { it.question.toModel() } ?: emptyList()
        for (q in questions) {
            q.resolveDependency(questions)
        }

        // FIXME: There may be provided answers
        val v = mutableMapOf<eu.pretix.libpretixsync.models.Question, String>()
        checkResult.requiredAnswers?.forEach {
            val answer = it.currentValue
            if (!answer.isNullOrBlank()) {
                v[it.question.toModel()] = answer
            }
        }
        val questionValues = v.toMap()

        val resultState = ResultStateData(
            resultState = checkResult.resultState(),
            resultText = checkResult.message,
            resultOffline = checkResult.offline,
            ticketAndVariationName = checkResult.ticketAndVariationName(),
            orderCodeAndPositionId = checkResult.orderCodeAndPositionId(),
            attendeeName = checkResult.formattedAttendeeName(conf.hideNames),
            seat = checkResult.formattedSeat(),
            reasonExplanation = checkResult.reasonExplanation(),
            questionAndAnswers = checkResult.formattedAnswers(),
            checkInTexts = checkResult.formattedCheckInTexts(),
            eventName = scannedEvent?.name,
            attention = checkResult.isRequireAttention,
            scanType = checkResult.scanType,
            firstScanned = checkResult.formattedFirstScanned(),
            requiredQuestions = questions,
            answers = questionValues
        )

        return resultState
    }

    suspend fun handleScan(rawResult: String?, ignoreUnpaid: Boolean): TicketCheckProvider.CheckResult =
        handleScan(rawResult, null, ignoreUnpaid)

    suspend fun handleScan(rawResult: String?, answers: List<Answer>?, ignoreUnpaid: Boolean): TicketCheckProvider.CheckResult {
        if (rawResult.isNullOrEmpty()) {
            connectivityHelper.recordError()
            return TicketCheckProvider.CheckResult(
                TicketCheckProvider.CheckResult.Type.INVALID,
                getString(Res.string.error_unknown_exception)
            )
        }

        if (answers.isNullOrEmpty()) {
            audioPlayer.play(Res.getUri("files/beep.m4a"))
        }

        val scanType = when (conf.scanType) {
            "exit" -> TicketCheckProvider.CheckInType.EXIT
            else -> TicketCheckProvider.CheckInType.ENTRY
        }

        val sourceType = "barcode"

        val withBadgeData = conf.autoPrintBadges

        val allowQuestions = true

        val startedAt = System.currentTimeMillis()

        try {
            val checkResult = checkProvider.check(
                conf.eventSelectionToMap(),
                ticketid = rawResult,
                source_type = sourceType,
                answers = answers,
                ignore_unpaid = ignoreUnpaid,
                with_badge_data = withBadgeData,
                scanType,
                allowQuestions = allowQuestions
            )
            
            println("Check result type: ${checkResult.type}")

            if (checkProvider is OnlineCheckProvider) {
                if (checkResult.type == TicketCheckProvider.CheckResult.Type.ERROR) {
                    connectivityHelper.recordError()
                } else {
                    connectivityHelper.recordSuccess(System.currentTimeMillis() - startedAt)
                }
            }

            if (checkResult.message == null) {
                checkResult.message = when (checkResult.type) {
                    TicketCheckProvider.CheckResult.Type.INVALID -> getString(Res.string.scan_result_invalid)
                    TicketCheckProvider.CheckResult.Type.VALID -> when (checkResult.scanType) {
                        TicketCheckProvider.CheckInType.EXIT -> getString(Res.string.scan_result_exit)
                        TicketCheckProvider.CheckInType.ENTRY -> getString(Res.string.scan_result_valid)
                    }

                    TicketCheckProvider.CheckResult.Type.USED -> getString(Res.string.scan_result_used)
                    TicketCheckProvider.CheckResult.Type.RULES -> getString(Res.string.scan_result_rules)
                    TicketCheckProvider.CheckResult.Type.AMBIGUOUS -> getString(Res.string.scan_result_ambiguous)
                    TicketCheckProvider.CheckResult.Type.REVOKED -> getString(Res.string.scan_result_revoked)
                    TicketCheckProvider.CheckResult.Type.UNAPPROVED -> getString(Res.string.scan_result_unapproved)
                    TicketCheckProvider.CheckResult.Type.INVALID_TIME -> getString(Res.string.scan_result_invalid_time)
                    TicketCheckProvider.CheckResult.Type.BLOCKED -> getString(Res.string.scan_result_blocked)
                    TicketCheckProvider.CheckResult.Type.UNPAID -> getString(Res.string.scan_result_unpaid)
                    TicketCheckProvider.CheckResult.Type.CANCELED -> getString(Res.string.scan_result_canceled)
                    TicketCheckProvider.CheckResult.Type.PRODUCT -> getString(Res.string.scan_result_product)
                    else -> null
                }
            }

            if (conf.playSound) {
                audioPlayer.play(checkResult.pathForSound())
            }


            return checkResult
        } catch (e: Exception) {
            logHandler.captureException(e)
            e.printStackTrace()
            connectivityHelper.recordError()
        }

        return TicketCheckProvider.CheckResult(
            TicketCheckProvider.CheckResult.Type.INVALID,
            getString(Res.string.error_unknown_exception)
        )
    }

    private fun calculateScannedEvent(eventSlug: String?): eu.pretix.libpretixsync.models.Event? {
        if (!eventSlug.isNullOrBlank() && conf.eventSelection.size > 1) {
            return appCache.db.eventQueries.selectBySlug(eventSlug).executeAsOneOrNull()?.toModel()
        }

        return null
    }
}

@OptIn(ExperimentalResourceApi::class)
fun TicketCheckProvider.CheckResult.pathForSound(): String =
    when (type) {
        TicketCheckProvider.CheckResult.Type.VALID -> when (scanType) {
            TicketCheckProvider.CheckInType.ENTRY ->
                if (isRequireAttention) {
                    Res.getUri("files/attention.m4a")
                } else {
                    Res.getUri("files/enter.m4a")
                }

            TicketCheckProvider.CheckInType.EXIT -> Res.getUri("files/exit.m4a")
        }

        null,
        TicketCheckProvider.CheckResult.Type.USED,
        TicketCheckProvider.CheckResult.Type.ERROR,
        TicketCheckProvider.CheckResult.Type.UNPAID,
        TicketCheckProvider.CheckResult.Type.BLOCKED,
        TicketCheckProvider.CheckResult.Type.INVALID_TIME,
        TicketCheckProvider.CheckResult.Type.CANCELED,
        TicketCheckProvider.CheckResult.Type.PRODUCT,
        TicketCheckProvider.CheckResult.Type.RULES,
        TicketCheckProvider.CheckResult.Type.ANSWERS_REQUIRED,
        TicketCheckProvider.CheckResult.Type.AMBIGUOUS,
        TicketCheckProvider.CheckResult.Type.REVOKED,
        TicketCheckProvider.CheckResult.Type.UNAPPROVED,
        TicketCheckProvider.CheckResult.Type.INVALID -> Res.getUri("files/error.m4a")
    }


fun TicketCheckProvider.CheckResult.resultState(): ResultState =
    when (type) {
        TicketCheckProvider.CheckResult.Type.INVALID -> ResultState.ERROR
        TicketCheckProvider.CheckResult.Type.VALID -> {
            when (scanType) {
                TicketCheckProvider.CheckInType.EXIT -> ResultState.SUCCESS_EXIT
                TicketCheckProvider.CheckInType.ENTRY -> ResultState.SUCCESS
            }
        }

        TicketCheckProvider.CheckResult.Type.USED -> ResultState.WARNING
        null,
        TicketCheckProvider.CheckResult.Type.ERROR,
        TicketCheckProvider.CheckResult.Type.BLOCKED,
        TicketCheckProvider.CheckResult.Type.INVALID_TIME,
        TicketCheckProvider.CheckResult.Type.CANCELED,
        TicketCheckProvider.CheckResult.Type.PRODUCT,
        TicketCheckProvider.CheckResult.Type.RULES,
        TicketCheckProvider.CheckResult.Type.AMBIGUOUS,
        TicketCheckProvider.CheckResult.Type.REVOKED,
        TicketCheckProvider.CheckResult.Type.UNAPPROVED -> ResultState.ERROR

        TicketCheckProvider.CheckResult.Type.ANSWERS_REQUIRED -> ResultState.DIALOG_QUESTIONS

        TicketCheckProvider.CheckResult.Type.UNPAID -> {
            if (isCheckinAllowed) {
                ResultState.DIALOG_UNPAID
            } else {
                ResultState.ERROR
            }
        }
    }

fun TicketCheckProvider.CheckResult.ticketAndVariationName(): String? {
    if (ticket != null) {
        return if (variation != null) {
            "$ticket – $variation"
        } else {
            ticket
        }
    }

    return null
}

fun TicketCheckProvider.CheckResult.reasonExplanation(): String? {
    if (reasonExplanation.isNullOrBlank()) {
        return null
    }

    return reasonExplanation
}

suspend fun TicketCheckProvider.CheckResult.formattedFirstScanned(): String? {
    if (firstScanned != null) {
        val df = SimpleDateFormat(getString(Res.string.short_datetime_format))
        return df.format(firstScanned)
    }
    return null
}

fun TicketCheckProvider.CheckResult.formattedAttendeeName(hideNames: Boolean): String? {
    if (hideNames || attendee_name.isNullOrBlank()) {
        return null
    }
    return attendee_name
}

fun TicketCheckProvider.CheckResult.formattedSeat(): String? {
    if (scanType != TicketCheckProvider.CheckInType.EXIT) {
        return seat
    }
    return null
}
fun TicketCheckProvider.CheckResult.formattedAnswers(): AnnotatedString? {
    val answers = shownAnswers
    if (scanType != TicketCheckProvider.CheckInType.EXIT && !answers.isNullOrEmpty()) {
        buildAnnotatedString {
            answers.forEachIndexed { index, questionAnswer ->
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(questionAnswer.question.toModel().question + ":")
                }
                append(" ")
                append(questionAnswer.currentValue) // FIXME: yes/no is written here as true/false
                if (index != answers.lastIndex) {
                    append("\n")
                }
            }
        }
    }

    return null
}


fun TicketCheckProvider.CheckResult.formattedCheckInTexts(): String? {
    val texts = checkinTexts?.filterNot { it.isBlank() }
    if (scanType == TicketCheckProvider.CheckInType.EXIT || texts.isNullOrEmpty()) {
        return null
    }
    return texts.joinToString("\n") { it.trim() }.trim()
}