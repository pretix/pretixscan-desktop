package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import eu.pretix.desktop.app.ui.ErrorDialog
import eu.pretix.scan.tickets.data.DismissBehavior
import eu.pretix.scan.tickets.data.ResultState
import eu.pretix.scan.tickets.data.dismissBehavior
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.badge_printing_not_available
import java.util.logging.Logger

private val log = Logger.getLogger("TicketHandlingDialog")

@Preview
@Composable
fun TicketHandlingDialog(
    secret: String?,
    scanTimestamp: Long,
    onDismiss: () -> Unit,
    onResultStateChanged: (ResultState) -> Unit = {}
) {

    val viewModel = koinViewModel<TicketHandlingDialogViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val localTicketHandlingErrors by viewModel.localTicketHandlingErrors.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val dialogState = rememberDialogState(initiallyVisible = true)
    var remainingTimeProgress by remember { mutableStateOf(1.0f) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(secret, scanTimestamp) {
        viewModel.resetTicketHandlingState()
        viewModel.handleTicket(secret)
    }

    LaunchedEffect(uiState.resultState) {
        if (uiState.resultState != ResultState.EMPTY && uiState.resultState != ResultState.LOADING) {
            focusRequester.requestFocus()
            onResultStateChanged(uiState.resultState)
        }

        if (uiState.resultState.dismissBehavior() == DismissBehavior.AutoDismiss) {
            val totalDuration = 30000L
            val updateInterval = 100L
            var elapsed = 0L

            while (elapsed < totalDuration) {
                kotlinx.coroutines.delay(updateInterval)
                elapsed += updateInterval
                remainingTimeProgress = 1.0f - (elapsed.toFloat() / totalDuration.toFloat())
            }

            log.info("AutoScan: 30s auto-dismiss timer expired")
            onDismiss()
        } else {
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
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (uiState.resultState.dismissBehavior()) {
                        DismissBehavior.AutoDismiss -> {
                            if (keyEvent.key == Key.Spacebar || keyEvent.key == Key.Escape) {
                                onDismiss(); true
                            } else false
                        }
                        DismissBehavior.RequiresUserInteraction -> {
                            if (keyEvent.key == Key.Escape) {
                                onDismiss(); true
                            } else false
                        }
                        DismissBehavior.Transient -> false
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
                    TicketWarning(data = uiState, remainingTimeProgress = remainingTimeProgress)
                }

                ResultState.SUCCESS -> {
                    TicketSuccess(
                        data = uiState,
                        onPrintBadges = {
                            coroutineScope.launch {
                                viewModel.printBadges()
                            }
                        },
                        remainingTimeProgress = remainingTimeProgress
                    )
                }

                ResultState.SUCCESS_EXIT -> {
                    TicketSuccess(
                        data = uiState,
                        onPrintBadges = {
                            coroutineScope.launch {
                                viewModel.printBadges()
                            }
                        },
                        remainingTimeProgress = remainingTimeProgress
                    )
                }
            }
        }
    }


    when (localTicketHandlingErrors) {
        is TicketHandlingErrors.None -> {}
        is TicketHandlingErrors.Error -> {
            ErrorDialog(
                title = stringResource(Res.string.badge_printing_not_available),
                message = (localTicketHandlingErrors as TicketHandlingErrors.Error).exception,
                onDismiss = {
                    viewModel.dismissError()
                }
            )
        }
    }
}

