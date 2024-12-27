package screen.main.tickets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.ic_check_circle_white_24dp
import pretixscan.composeapp.generated.resources.ic_error_white_24dp

@Preview
@Composable
fun TicketHandlingview(modifier: Modifier = Modifier, secret: String?, onDismiss: () -> Unit) {

    val viewModel = koinViewModel<TicketHandlingViewModel>()
    val uiState by viewModel.uiState.collectAsState()

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
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth().background(uiState.resultState.colour()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(painter = painterResource(Res.drawable.ic_error_white_24dp), contentDescription = "")
                        Text(uiState.resultText ?: "")
                        Text(uiState.ticketAndVariationName ?: "")
                        Text(uiState.reasonExplanation ?: "")
                        Text(uiState.firstScanned ?: "")
                    }
                }
                ResultState.DIALOG -> TODO()
                ResultState.WARNING -> {Text(uiState.resultText ?: "")}
                ResultState.SUCCESS -> {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth().background(uiState.resultState.colour()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(painter = painterResource(Res.drawable.ic_check_circle_white_24dp), contentDescription = "")
                        Text(uiState.resultText ?: "")
                        Text(uiState.ticketAndVariationName ?: "")
                        Text(uiState.reasonExplanation ?: "")
                        Text(uiState.firstScanned ?: "")
                    }
                }
                ResultState.SUCCESS_EXIT -> {Text(uiState.resultText ?: "")}
            }
        }
    }
}

