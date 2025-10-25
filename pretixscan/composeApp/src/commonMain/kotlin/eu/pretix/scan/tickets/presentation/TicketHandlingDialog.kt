package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.composables.core.*
import eu.pretix.scan.tickets.data.ResultState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import java.util.logging.Logger

private val log = Logger.getLogger("TicketHandlingDialog")

@Preview
@Composable
fun TicketHandlingDialog(secret: String?, onDismiss: () -> Unit) {

    val viewModel = koinViewModel<TicketHandlingDialogViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val dialogState = rememberDialogState(initiallyVisible = true)
    var remainingTimeProgress by remember { mutableStateOf(1.0f) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(secret) {
        viewModel.resetTicketHandlingState()
        viewModel.handleTicket(secret)
    }

    // Request focus when transitioning to success states so Space key works
    // Keyed on resultState so it re-runs after questions/unpaid dialogs
    LaunchedEffect(uiState.resultState) {
        if (uiState.resultState == ResultState.SUCCESS ||
            uiState.resultState == ResultState.SUCCESS_EXIT) {
            log.info("AutoScan: Requesting focus for success state: ${uiState.resultState}")
            focusRequester.requestFocus()
        }
    }

    // Auto-dismiss countdown for successful scans (30 seconds)
    LaunchedEffect(uiState.resultState) {
        if (uiState.resultState == ResultState.SUCCESS || uiState.resultState == ResultState.SUCCESS_EXIT) {
            val totalDuration = 30000L // 30 seconds in milliseconds
            val updateInterval = 100L // Update every 100ms for smooth animation
            var elapsed = 0L

            while (elapsed < totalDuration) {
                kotlinx.coroutines.delay(updateInterval)
                elapsed += updateInterval
                remainingTimeProgress = 1.0f - (elapsed.toFloat() / totalDuration.toFloat())
            }

            // Time's up, dismiss the dialog
            log.info("AutoScan: 30s auto-dismiss timer expired")
            onDismiss()
        } else {
            // Reset progress for non-success states
            remainingTimeProgress = 1.0f
        }
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
                .background(Color.White)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    // Handle Space key to manually dismiss dialog
                    // Enter key is NOT handled here to allow continuous scanning
                    if (keyEvent.type == KeyEventType.KeyDown &&
                        keyEvent.key == Key.Spacebar) {
                        log.info("AutoScan: Space key pressed, dismissing dialog")
                        onDismiss()
                        true  // Consume Space key
                    } else {
                        false  // Let other keys (including Enter) propagate for continuous scanning
                    }
                },
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
                        },
                        remainingTimeProgress = remainingTimeProgress
                    )
                }

                ResultState.SUCCESS_EXIT -> {
                    Text(uiState.resultText ?: "")
                }
            }
        }
    }
}

