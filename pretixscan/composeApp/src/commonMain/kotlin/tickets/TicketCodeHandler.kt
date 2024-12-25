package tickets

import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.SentryInterface
import eu.pretix.libpretixsync.check.OnlineCheckProvider
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.Answer
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import pretixscan.composeapp.generated.resources.Res

import pretixscan.composeapp.generated.resources.error_unknown_exception

@OptIn(ExperimentalResourceApi::class)
class TicketCodeHandler(
    private val conf: AppConfig,
    private val checkProvider: TicketCheckProvider,
    private val audioPlayer: GadulkaPlayer,
    private val logHandler: SentryInterface,
    private val connectivityHelper: ConnectivityHelper
) {


    suspend fun handleScan(raw_result: String?): TicketCheckProvider.CheckResult {
        return handleScan(raw_result, null)
    }

    suspend fun handleScan(raw_result: String?, answers: List<Answer>?): TicketCheckProvider.CheckResult {
        if (raw_result.isNullOrEmpty()) {
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

        val ignoreUnpaid = false

        val withBadgeData = conf.autoPrintBadges

        val allowQuestions = true

        val startedAt = System.currentTimeMillis()

        try {
            val checkResult = checkProvider.check(
                conf.eventSelectionToMap(),
                ticketid = raw_result,
                source_type = sourceType,
                answers = answers,
                ignore_unpaid = ignoreUnpaid,
                with_badge_data = withBadgeData,
                scanType,
                allowQuestions = allowQuestions
            )

            if (checkProvider is OnlineCheckProvider) {
                if (checkResult.type == TicketCheckProvider.CheckResult.Type.ERROR) {
                    connectivityHelper.recordError()
                } else {
                    connectivityHelper.recordSuccess(System.currentTimeMillis() - startedAt)
                }
            }

            println("Check result type: ${checkResult.type}")
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
}