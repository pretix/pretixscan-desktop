package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.asColor
import eu.pretix.scan.tickets.data.ResultStateData
import eu.pretix.scan.tickets.data.color
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.ic_check_circle_white_24dp
import pretixscan.composeapp.generated.resources.settings_label_print_badges

@Preview
@Composable
fun TicketSuccess(
    modifier: Modifier = Modifier,
    data: ResultStateData,
    onPrintBadges: () -> Unit,
    remainingTimeProgress: Float = 1.0f
) {
    Column(
        modifier = Modifier.background(data.resultState.color()),
    ) {
        if (data.isPrintable) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onPrintBadges) {
                    Text(stringResource(Res.string.settings_label_print_badges))
                }
            }
        }

        TicketResultHeader(icon = Res.drawable.ic_check_circle_white_24dp, data = data)

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
