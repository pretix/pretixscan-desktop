package tickets.presentation


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.ui.CustomColor
import app.ui.asColor
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.first_scanned
import pretixscan.composeapp.generated.resources.ic_error_white_24dp
import tickets.data.ResultStateData
import tickets.data.color

@Preview
@Composable
fun TicketFailure(modifier: Modifier = Modifier, data: ResultStateData) {
    Column(
        modifier = Modifier.background(data.resultState.color()),
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_error_white_24dp),
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CustomColor.White.asColor())
                .padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(data.orderCodeAndPositionId ?: "", style = MaterialTheme.typography.bodyLarge)
        }
    }

}