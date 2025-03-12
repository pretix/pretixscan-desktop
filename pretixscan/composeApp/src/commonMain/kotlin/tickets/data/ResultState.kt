package tickets.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import app.ui.CustomColor
import app.ui.asColor
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.models.BadgeLayout
import eu.pretix.libpretixsync.models.Question
import org.json.JSONObject

enum class ResultState {
    EMPTY,
    LOADING,
    ERROR,
    DIALOG_UNPAID,
    DIALOG_QUESTIONS,
    WARNING,
    SUCCESS,
    SUCCESS_EXIT
}

fun ResultState.color(): Color {
    return when (this) {
        ResultState.EMPTY, ResultState.DIALOG_UNPAID, ResultState.DIALOG_QUESTIONS, ResultState.LOADING -> CustomColor.BrandLightGray.asColor()
        ResultState.ERROR -> CustomColor.BrandRed.asColor()
        ResultState.WARNING -> CustomColor.BrandOrange.asColor()
        ResultState.SUCCESS, ResultState.SUCCESS_EXIT -> CustomColor.BrandGreen.asColor()
    }
}

data class ResultStateData(
    val resultState: ResultState,
    val resultText: String? = null,
    val resultOffline: Boolean = false,
    val ticketAndVariationName: String? = null,
    val orderCodeAndPositionId: String? = null,
    val firstScanned: String? = null,
    val attendeeName: String? = null,
    val seat: String? = null,
    val reasonExplanation: String? = null,
    val questionAndAnswers: AnnotatedString? = null,
    val checkInTexts: String? = null,
    val eventName: String? = null,
    val attention: Boolean = false,
    val scanType: TicketCheckProvider.CheckInType = TicketCheckProvider.CheckInType.ENTRY,
    val requiredQuestions: List<Question> = emptyList(),
    val answers: Map<Question, String> = emptyMap(),
    val isPrintable: Boolean = false,
    val badgeLayout: BadgeLayout? = null,
    val position: JSONObject? = null
)