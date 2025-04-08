package eu.pretix.scan.setup


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import eu.pretix.desktop.app.navigation.Route
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.ScreenContentRoot
import eu.pretix.desktop.app.ui.asColor
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.*

@Composable
@Preview
fun SetupScreen(
    navHostController: NavHostController,
) {
    val viewModel = koinViewModel<SetupViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    // Local state to control the visibility of the alert dialog
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var url by remember { mutableStateOf("https://pretix.eu") }
    var token by remember { mutableStateOf("") }
    val focusRequester = FocusRequester()

    val coroutineScope = rememberCoroutineScope()
    ScreenContentRoot {

        Row(
            modifier = Modifier.fillMaxSize().padding(64.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {

            Row(modifier = Modifier.padding(top = 64.dp)) {
                Image(
                    painter = painterResource(Res.drawable.pretix_logo_dark_angled),
                    contentDescription = "Pretix logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(320.dp)
                )

                Column(modifier = Modifier.padding(horizontal = 64.dp)) {
                    Text(
                        stringResource(Res.string.connect_instruction_heading),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        stringResource(Res.string.connect_instruction_step1),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        stringResource(Res.string.connect_instruction_step2),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        stringResource(Res.string.connect_instruction_step3),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
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

                    Row(
                        Modifier.fillMaxWidth()
                            .padding(vertical = 32.dp),
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
                            enabled = uiState != SetupUiState.Loading && token.isNotEmpty() && url.isNotEmpty()
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
                            title = stringResource(Res.string.error_unknown_exception),
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
        }
    }
}