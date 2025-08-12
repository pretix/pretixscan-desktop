package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.core.*
import eu.pretix.scan.tickets.data.ResultState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Preview
@Composable
fun TicketHandlingDialog(secret: String?, onDismiss: () -> Unit) {

    val viewModel = koinViewModel<TicketHandlingDialogViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val dialogState = rememberDialogState(initiallyVisible = true)

    LaunchedEffect(secret) {
        viewModel.resetTicketHandlingState()
        viewModel.handleTicket(secret)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetTicketHandlingState()
        }
    }



    Dialog(
        state = dialogState,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        onDismiss = onDismiss
    ) {
        Scrim()
        DialogPanel(
            modifier = Modifier
                .displayCutoutPadding()
                .systemBarsPadding()
                .widthIn(min = 280.dp, max = 560.dp)
                .padding(20.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE4E4E4), RoundedCornerShape(12.dp))
                .background(Color.White),
        ) {
            when (uiState.resultState) {
                ResultState.EMPTY -> {}
                ResultState.LOADING -> {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        CircularProgressIndicator()
                    }
                }

                ResultState.ERROR -> {
                    TicketFailure(data = uiState)
                }

                ResultState.DIALOG_UNPAID -> UnpaidDialogView(data = uiState, onCancel = onDismiss, onCheckInAnyway = {
                    coroutineScope.launch {
                        viewModel.handleTicket(secret, ignoreUnpaid = true)
                    }
                })

                ResultState.DIALOG_QUESTIONS -> QuestionsDialogView(
                    data = uiState,
                    onConfirm = { answers ->
                        coroutineScope.launch {
                            viewModel.handleTicket(secret, answers = answers, ignoreUnpaid = true)
                        }
                    },
                    onCancel = onDismiss
                )

                ResultState.WARNING -> {
                    Text(uiState.resultText ?: "")
                }

                ResultState.SUCCESS -> {
                    TicketSuccess(
                        data = uiState,
                        onPrintBadges = {
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.printBadges()
                            }
                        }
                    )
                }

                ResultState.SUCCESS_EXIT -> {
                    Text(uiState.resultText ?: "")
                }
            }
        }
    }
}

