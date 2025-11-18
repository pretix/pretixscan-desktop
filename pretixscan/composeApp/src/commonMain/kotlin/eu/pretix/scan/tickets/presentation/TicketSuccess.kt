package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.asColor
import eu.pretix.scan.tickets.data.ResultStateData
import eu.pretix.scan.tickets.data.color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.first_scanned
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
                    .padding(16.dp)
            ) {
                Button(onClick = onPrintBadges) {
                    Text(stringResource(Res.string.settings_label_print_badges))
                }
            }
        }
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_check_circle_white_24dp),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(64.dp)
            )
            Text(
                data.resultText ?: "",
                style = MaterialTheme.typography.headlineMedium,
                color = CustomColor.White.asColor()
            )
            Text(
                data.ticketAndVariationName ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = CustomColor.White.asColor()
            )
            Text(
                data.reasonExplanation ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = CustomColor.White.asColor()
            )
            if (data.firstScanned != null) {
                Row {
                    Text(
                        stringResource(Res.string.first_scanned),
                        style = MaterialTheme.typography.bodyLarge,
                        color = CustomColor.White.asColor()
                    )

                    Text(
                        data.firstScanned ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CustomColor.White.asColor()
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CustomColor.White.asColor())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (data.attendeeName != null) {
                    Text(
                        text = data.attendeeName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (data.orderCodeAndPositionId != null) {
                    Text(
                        text = data.orderCodeAndPositionId,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (data.seat != null || data.questionAndAnswers != null || data.checkInTexts != null) {
                Spacer(modifier = Modifier.height(4.dp))

                Column {
                    if (data.seat != null) {
                        Text(
                            text = data.seat,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (data.questionAndAnswers != null) {
                        Text(
                            text = data.questionAndAnswers,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }

                    if (data.checkInTexts != null) {
                        Text(
                            text = data.checkInTexts,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Countdown progress indicator
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