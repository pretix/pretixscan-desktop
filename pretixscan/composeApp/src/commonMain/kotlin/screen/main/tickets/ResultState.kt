package screen.main.tickets

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import eu.pretix.libpretixsync.check.TicketCheckProvider
import ui.CustomColor
import ui.asColour

enum class ResultState {
    EMPTY,
    LOADING,
    ERROR,
    DIALOG,
    WARNING,
    SUCCESS,
    SUCCESS_EXIT
}

fun ResultState.colour(): Color {
    return when (this) {
        ResultState.EMPTY, ResultState.DIALOG, ResultState.LOADING -> CustomColor.BrandLightGray.asColour()
        ResultState.ERROR -> CustomColor.BrandRed.asColour()
        ResultState.WARNING -> CustomColor.BrandOrange.asColour()
        ResultState.SUCCESS, ResultState.SUCCESS_EXIT -> CustomColor.BrandGreen.asColour()
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
    val scanType: TicketCheckProvider.CheckInType = TicketCheckProvider.CheckInType.ENTRY
)