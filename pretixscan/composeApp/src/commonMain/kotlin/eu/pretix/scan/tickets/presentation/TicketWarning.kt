package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.asColor
import eu.pretix.scan.tickets.data.ResultStateData
import eu.pretix.scan.tickets.data.color
import org.jetbrains.compose.ui.tooling.preview.Preview
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.ic_warning_white_24dp

@Preview
@Composable
fun TicketWarning(
    modifier: Modifier = Modifier,
    data: ResultStateData,
    remainingTimeProgress: Float = 1.0f
) {
    Column(
        modifier = Modifier.background(data.resultState.color()),
    ) {
        TicketResultHeader(icon = Res.drawable.ic_warning_white_24dp, data = data)

        TicketResultDetails(data = data)

        LinearProgressIndicator(
            progress = { remainingTimeProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = CustomColor.White.asColor().copy(alpha = 0.8f),
            trackColor = data.resultState.color().copy(alpha = 0.3f)
        )
    }
}
