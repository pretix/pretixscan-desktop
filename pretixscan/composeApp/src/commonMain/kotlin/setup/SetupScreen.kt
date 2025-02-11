package setup


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.navigation.Route
import app.ui.CustomColor
import app.ui.asColor
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.*

@Composable
@Preview
fun SetupScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<SetupViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    // Local state to control the visibility of the alert dialog
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var url by remember { mutableStateOf("https://pretix.eu") }
    var token by remember { mutableStateOf("") }
    val focusRequester = FocusRequester()

    var coroutineScope = rememberCoroutineScope()

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        Row(
            Modifier.fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(stringResource(Res.string.connect_instruction_heading))
                Text(stringResource(Res.string.connect_instruction_step1), textAlign = TextAlign.Center)
                Text(stringResource(Res.string.connect_instruction_step2), textAlign = TextAlign.Center)
                Text(stringResource(Res.string.connect_instruction_step3), textAlign = TextAlign.Center)
            }
        }

        Row(
            Modifier.fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(Res.string.hint_url)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text(stringResource(Res.string.hint_token)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )
            }
        }

        Row(
            Modifier.fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = CustomColor.BrandGreen.asColor()),
                onClick = {
                    val apiToken = token
                    val apiUrl = url
                    coroutineScope.launch {
                        viewModel.verifyTokenAndSetup(token = apiToken, url = apiUrl)
                    }
                },
                enabled = uiState != SetupUiState.Loading
            ) {
                Text(stringResource(Res.string.connect_check_token))
            }
        }

        when (uiState) {
            is SetupUiState.Loading -> {
                // Show a loading indicator
                CircularProgressIndicator()
            }

            is SetupUiState.Success -> {
                LaunchedEffect(Unit) {
                    navHostController.navigate(route = Route.Main.route)
                }
            }

            is SetupUiState.Error -> {
                // Show error message in a dialog
                errorMessage = (uiState as SetupUiState.Error).exception
                showErrorDialog = true
            }

            SetupUiState.Start -> {
                // Do nothing
            }
        }

        // Show an alert dialog if there's an error
        if (showErrorDialog) {
            ErrorDialog(
                message = errorMessage,
                onDismiss = {
                    errorMessage = ""
                    showErrorDialog = false
                    viewModel.dismissLoginError()
                }
            )
        }
    }


}