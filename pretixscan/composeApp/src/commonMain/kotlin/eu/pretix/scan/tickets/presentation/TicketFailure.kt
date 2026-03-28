package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.asColor
import eu.pretix.scan.tickets.data.ResultStateData
import eu.pretix.scan.tickets.data.color
import org.jetbrains.compose.ui.tooling.preview.Preview
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.ic_error_white_24dp

@Preview
@Composable
fun TicketFailure(modifier: Modifier = Modifier, data: ResultStateData) {
    Column(
        modifier = Modifier.background(data.resultState.color()),
    ) {
        TicketResultHeader(icon = Res.drawable.ic_error_white_24dp, data = data)

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
