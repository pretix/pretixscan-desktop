package tickets.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import app.ui.CustomColor
import app.ui.asColour
import app.ui.modifiers.bottomBorder
import eu.pretix.libpretixsync.check.TicketCheckProvider
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.*

@Composable
fun SearcResultsView(
    searchSuggestions: List<TicketCheckProvider.SearchResult>,
    onSelectedSearchResult: (TicketCheckProvider.SearchResult) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        val focusManager = LocalFocusManager.current
        val state = rememberLazyListState()
        val focusedIndex = remember { mutableStateOf<Int?>(null) }

        LazyColumn(
            Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
            .onPreviewKeyEvent {
                if (searchSuggestions.isEmpty()) {
                    // nothing to do
                    false
                } else if (it.key == Key.DirectionDown) {
                    focusManager.moveFocus(FocusDirection.Next)
                    true
                } else if (it.key == Key.DirectionUp) {
                    focusManager.moveFocus(FocusDirection.Previous)
                    true
                } else {
                    false
                }
            }, state
        ) {
            itemsIndexed(searchSuggestions) { index, item ->
                FocusableRowItem(index, item, onSelectedSearchResult, onFocused = {
                    println("Focused row $index")
                    focusedIndex.value = index
                }, focusedIndex = focusedIndex.value)
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

@Composable
fun RowItem(
    modifier: Modifier = Modifier,
    item: TicketCheckProvider.SearchResult
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 64.dp)
            .bottomBorder(1.dp, MaterialTheme.colorScheme.outline),
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

@Composable
fun FocusableRowItem(
    index: Int,
    item: TicketCheckProvider.SearchResult,
    onSelectedSearchResult: (TicketCheckProvider.SearchResult) -> Unit = {},
    onFocused: () -> Unit = {},
    focusedIndex: Int? = null,
) {
    val keyPressedState = remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocusedState = interactionSource.collectIsFocusedAsState().value
    val isFocused = index == focusedIndex

    val backgroundColor = if (isFocusedState || isFocused) {
        if (keyPressedState.value)
            lerp(MaterialTheme.colorScheme.secondary, Color(64, 64, 64), 0.3f)
        else
        // the cell is focused
            MaterialTheme.colorScheme.secondary
    } else {
        // the cell is highlighted but not focused
        CustomColor.White.asColour()
    }

    // on changes to focus state, call onFocused
    LaunchedEffect(isFocusedState) {
        if (isFocusedState) {
            onFocused()
        }
    }

    Box(
        modifier = Modifier
            .background(backgroundColor)
            .padding(all = 8.dp)
            .clickable(onClickLabel = stringResource(Res.string.text_action_select), onClick = {
                onSelectedSearchResult(item)
            })
            .onPreviewKeyEvent {
                if (
                    it.key == Key.Enter ||
                    it.key == Key.Spacebar
                ) {
                    when (it.type) {
                        KeyEventType.KeyDown -> {
                            keyPressedState.value = true
                        }

                        KeyEventType.KeyUp -> {
                            keyPressedState.value = false
                            onSelectedSearchResult.invoke(item)
                        }
                    }
                }
                false
            }
            .focusable(interactionSource = interactionSource),
        contentAlignment = Alignment.Center
    ) {
        RowItem(modifier = Modifier.background(backgroundColor), item)
    }
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


fun TicketCheckProvider.SearchResult.ticketName(): String {
    if (this.variation?.isNotBlank() == true) {
        return "${this.ticket} - ${this.variation}"
    }
    return this.ticket ?: ""
}


fun TicketCheckProvider.SearchResult.ticketStatusColour(): Color {
    if (this.isRedeemed) {
        return CustomColor.BrandOrange.asColour()
    }

    if (this.status == TicketCheckProvider.SearchResult.Status.PAID) {
        return CustomColor.BrandGreen.asColour()
    }
    return CustomColor.BrandRed.asColour()
}