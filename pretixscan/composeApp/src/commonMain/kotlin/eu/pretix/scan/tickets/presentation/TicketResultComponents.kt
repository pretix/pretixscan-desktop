package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.first_scanned

@Composable
fun TicketResultHeader(icon: DrawableResource, data: ResultStateData) {
    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(icon),
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
}

@Composable
fun TicketResultDetails(data: ResultStateData) {
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
}
