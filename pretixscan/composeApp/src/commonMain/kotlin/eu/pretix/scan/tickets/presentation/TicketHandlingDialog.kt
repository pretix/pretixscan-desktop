package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import eu.pretix.scan.tickets.data.ResultState
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Preview
@Composable
fun TicketHandlingDialog(modifier: Modifier = Modifier, secret: String?, onDismiss: () -> Unit) {

    val viewModel = koinViewModel<TicketHandlingDialogViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(secret) {
        viewModel.handleTicket(secret)
    }


    Dialog(properties = DialogProperties(
        dismissOnBackPress = true
    ), onDismissRequest = { onDismiss() }) {
        // Custom shape, background, and layout for the dialog
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            when(uiState.resultState) {
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
                    onCancel = {
                        viewModel.cancelQuestions()
                        onDismiss()
                    }
                )
                ResultState.WARNING -> {Text(uiState.resultText ?: "")}
                ResultState.SUCCESS -> {
                    TicketSuccess(
                        data = uiState,
                        onPrintBadges = {
                            coroutineScope.launch {
                                viewModel.printBadges()
                            }
                        }
                    )
                }
                ResultState.SUCCESS_EXIT -> {Text(uiState.resultText ?: "")}
            }
        }
    }
}

