package tickets.presentation

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.pretix.libpretixsync.check.TicketCheckProvider
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.*
import app.ui.modifiers.bottomBorder
import app.ui.CustomColor
import app.ui.asColour

@Composable
fun SearcResultsView(
    searchSuggestions: List<TicketCheckProvider.SearchResult>,
    onSelectedSearchResult: (TicketCheckProvider.SearchResult) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        val state = rememberLazyListState()

        LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state) {
            items(searchSuggestions) { item ->
                Column(
                    modifier = Modifier.defaultMinSize(minHeight = 64.dp)
                        .bottomBorder(1.dp, MaterialTheme.colorScheme.outline)
                        .clickable(onClickLabel = stringResource(Res.string.text_action_select), onClick = {
                            onSelectedSearchResult(item)
                        }),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Spacer(Modifier.height(8.dp))
                    Text(item.secret ?: "-", style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(item.ticketName(), style = MaterialTheme.typography.bodyLarge)
                        Spacer(
                            modifier = Modifier.weight(1f)
                        )
                        Text(item.ticketStatus(), style = MaterialTheme.typography.bodyLarge, color = item.ticketStatusColour())
                    }
                    Text(item.orderCode ?: "-", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state
            )
        )
    }
}

fun TicketCheckProvider.SearchResult.ticketName(): String {
    if (this.variation?.isNotBlank() == true) {
        return "${this.ticket} - ${this.variation}"
    }
    return this.ticket ?: ""
}

@Composable
fun TicketCheckProvider.SearchResult.ticketStatus(): String {
    if (this.isRedeemed) {
        return stringResource(Res.string.status_redeemed)
    }

    return when (this.status) {
        TicketCheckProvider.SearchResult.Status.PAID -> stringResource(Res.string.status_valid)
        TicketCheckProvider.SearchResult.Status.CANCELED -> stringResource(Res.string.status_canceled)
        TicketCheckProvider.SearchResult.Status.PENDING -> stringResource(Res.string.status_unpaid)
        null -> TODO()
    }
}

fun TicketCheckProvider.SearchResult.ticketStatusColour(): Color {
    if(this.isRedeemed) {
        return CustomColor.BrandOrange.asColour()
    }

    if(this.status == TicketCheckProvider.SearchResult.Status.PAID) {
        return CustomColor.BrandGreen.asColour()
    }
    return CustomColor.BrandRed.asColour()
}