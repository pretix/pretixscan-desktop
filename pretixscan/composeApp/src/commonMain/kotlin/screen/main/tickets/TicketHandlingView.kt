package screen.main.tickets

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
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.Res

@Preview
@Composable
fun TicketHandlingview(modifier: Modifier = Modifier, secret: String?, onDismiss: () -> Unit) {

    val viewModel = koinViewModel<TicketHandlingViewModel>()
    val isLoading by viewModel.isLoading.collectAsState()

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
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                CircularProgressIndicator()
            }
        }
    }
}

