package eu.pretix.scan.tickets.data

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.libpretixsync.SentryInterface
import eu.pretix.libpretixsync.check.OnlineCheckProvider
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.libpretixsync.models.db.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import pretixscan.composeapp.generated.resources.*
import java.text.SimpleDateFormat
import java.util.logging.Logger

@OptIn(ExperimentalResourceApi::class)
class TicketCodeHandler(
    private val conf: DataStoreConfigStore,
    private val appCache: AppCache,
    private val checkProviderFactory: () -> TicketCheckProvider,
    private val audioPlayer: GadulkaPlayer,
    private val logHandler: SentryInterface,
    private val connectivityHelper: ConnectivityHelper,
    private val layoutFetcher: PrintLayoutFetcher
) {
    private val log = Logger.getLogger("TicketCodeHandler")

    suspend fun handleScanResult(
        rawResult: String?,
        answers: List<Answer>? = null,
        ignoreUnpaid: Boolean
    ): ResultStateData {
        val checkResult = handleScan(rawResult, answers, ignoreUnpaid)

        val scannedEvent = calculateScannedEvent(checkResult.eventSlug)

        val questions = checkResult.requiredAnswers?.map { it.question.toModel() } ?: emptyList()
        for (q in questions) {
            q.resolveDependency(questions)
        }

        val questionValues = mutableMapOf<Long, String>()
        checkResult.requiredAnswers?.forEach {
            val answer = it.currentValue
            if (!answer.isNullOrBlank()) {
                questionValues[it.question.server_id] = answer
            }
        }

        val questionMaxLengths = mutableMapOf<Long, Int>()
        val questionNumberMin = mutableMapOf<Long, String>()
        val questionNumberMax = mutableMapOf<Long, String>()
        val questionDateMin = mutableMapOf<Long, String>()
        val questionDateMax = mutableMapOf<Long, String>()
        val questionDateTimeMin = mutableMapOf<Long, String>()
        val questionDateTimeMax = mutableMapOf<Long, String>()
        checkResult.requiredAnswers?.forEach {
            try {
                val jsonData = JSONObject(it.question.json_data)
                if (jsonData.has("valid_string_length_max") && !jsonData.isNull("valid_string_length_max")) {
                    questionMaxLengths[it.question.server_id] = jsonData.getInt("valid_string_length_max")
                }
                if (jsonData.has("valid_number_min") && !jsonData.isNull("valid_number_min")) {
                    questionNumberMin[it.question.server_id] = jsonData.getString("valid_number_min")
                }
                if (jsonData.has("valid_number_max") && !jsonData.isNull("valid_number_max")) {
                    questionNumberMax[it.question.server_id] = jsonData.getString("valid_number_max")
                }
                if (jsonData.has("valid_date_min") && !jsonData.isNull("valid_date_min")) {
                    questionDateMin[it.question.server_id] = jsonData.getString("valid_date_min")
                }
                if (jsonData.has("valid_date_max") && !jsonData.isNull("valid_date_max")) {
                    questionDateMax[it.question.server_id] = jsonData.getString("valid_date_max")
                }
                if (jsonData.has("valid_datetime_min") && !jsonData.isNull("valid_datetime_min")) {
                    questionDateTimeMin[it.question.server_id] = jsonData.getString("valid_datetime_min")
                }
                if (jsonData.has("valid_datetime_max") && !jsonData.isNull("valid_datetime_max")) {
                    questionDateTimeMax[it.question.server_id] = jsonData.getString("valid_datetime_max")
                }
            } catch (_: Exception) { }
        }

        val itemServerId = checkResult.position?.optLong("item", 0L)?.takeIf { it > 0 }
        val badgeLayout = layoutFetcher.getForItemAtEvent(itemServerId, checkResult.eventSlug)
        val canPrintBadge =
            conf.printBadges && checkResult.scanType != TicketCheckProvider.CheckInType.EXIT && checkResult.position != null && badgeLayout != null

        val resultState = ResultStateData(
            resultState = checkResult.resultState(),
            resultText = checkResult.message,
            resultOffline = checkResult.offline,
            ticketAndVariationName = checkResult.ticketAndVariationName(),
            orderCodeAndPositionId = checkResult.orderCodeAndPositionId(),
            attendeeName = checkResult.formattedAttendeeName(conf.uiHideNames),
            seat = checkResult.formattedSeat(),
            reasonExplanation = checkResult.reasonExplanation(),
            questionAndAnswers = checkResult.formattedAnswers(),
            checkInTexts = checkResult.formattedCheckInTexts(),
            eventName = scannedEvent?.name,
            attention = checkResult.isRequireAttention,
            scanType = checkResult.scanType,
            firstScanned = checkResult.formattedFirstScanned(),
            requiredQuestions = questions,
            answers = questionValues,
            isPrintable = canPrintBadge,
            badgeLayout = badgeLayout,
            position = checkResult.position,
            eventSlug = checkResult.eventSlug,
            questionMaxLengths = questionMaxLengths,
            questionNumberMin = questionNumberMin,
            questionNumberMax = questionNumberMax,
            questionDateMin = questionDateMin,
            questionDateMax = questionDateMax,
            questionDateTimeMin = questionDateTimeMin,
            questionDateTimeMax = questionDateTimeMax
        )

        log.info("Scan result: resultState=${resultState.resultState}, isPrintable=${resultState.isPrintable}, hasBadgeLayout=${badgeLayout != null}, hasPosition=${checkResult.position != null}, printBadgesEnabled=${conf.printBadges}, scanType=${checkResult.scanType}")
        return resultState
    }

    suspend fun handleScan(
        rawResult: String?,
        answers: List<Answer>?,
        ignoreUnpaid: Boolean
    ): TicketCheckProvider.CheckResult {
        if (rawResult.isNullOrEmpty()) {
            connectivityHelper.recordError()
            return TicketCheckProvider.CheckResult(
                TicketCheckProvider.CheckResult.Type.INVALID,
                getString(Res.string.error_unknown_exception)
            )
        }

        if (conf.playSound && answers.isNullOrEmpty()) {
            withContext(Dispatchers.Main) {
                audioPlayer.play(Res.getUri("files/beep.m4a"))
            }
        }

        val scanType = when (conf.scanType) {
            "exit" -> TicketCheckProvider.CheckInType.EXIT
            else -> TicketCheckProvider.CheckInType.ENTRY
        }

        val sourceType = "barcode"

        val withBadgeData = conf.printBadges

        val allowQuestions = true

        val startedAt = System.currentTimeMillis()

        try {
            val effectiveIgnoreUnpaid = ignoreUnpaid || !conf.unpaidAsk

            val checkProvider = checkProviderFactory()
            log.info("Scanning with ${checkProvider::class.simpleName}")

            val checkResult = checkProvider.check(
                conf.eventSelectionToMap(),
                ticketid = rawResult,
                source_type = sourceType,
                answers = answers,
                ignore_unpaid = effectiveIgnoreUnpaid,
                with_badge_data = withBadgeData,
                scanType,
                allowQuestions = allowQuestions
            )

            log.info("Check result type: ${checkResult.type}")

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
                withContext(Dispatchers.Main) {
                    audioPlayer.play(checkResult.pathForSound())
                }
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
        if (!eventSlug.isNullOrBlank() && conf.eventSelections.size > 1) {
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

suspend fun TicketCheckProvider.CheckResult.formattedAnswers(): AnnotatedString? {
    val answers = shownAnswers
    if (scanType != TicketCheckProvider.CheckInType.EXIT && !answers.isNullOrEmpty()) {
        return buildAnnotatedString {
            answers.forEachIndexed { index, questionAnswer ->
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(questionAnswer.question.toModel().question + ":")
                }
                append(" ")
                val currentValue = questionAnswer.currentValue
                val question = questionAnswer.question.toModel()
                if (question.type == QuestionType.B && !currentValue.isNullOrBlank()) {
                    when (currentValue) {
                        "True" -> append(getString(Res.string.yes))
                        "False" -> append(getString(Res.string.no))
                        else -> append(currentValue)
                    }
                } else {
                    append(currentValue)
                }
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