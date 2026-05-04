package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.asColor
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.ic_warning_white_24dp
import pretixscan.composeapp.generated.resources.ticket_attention

@Composable
fun AttentionTicketBar(modifier: Modifier) {
    Row(
        modifier = modifier
            .background(CustomColor.BrandBlue.asColor())
            .fillMaxWidth()
            .padding(PaddingValues(16.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(stringResource(Res.string.ticket_attention))
        Image(
            painter = painterResource(Res.drawable.ic_warning_white_24dp),
            contentDescription = stringResource(Res.string.ticket_attention),
        )
    }
}

