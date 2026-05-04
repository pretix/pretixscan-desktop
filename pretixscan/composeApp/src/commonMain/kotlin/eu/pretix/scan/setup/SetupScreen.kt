package eu.pretix.scan.setup


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import eu.pretix.desktop.app.navigation.Route
import eu.pretix.desktop.app.ui.*
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

    var url by remember { mutableStateOf("https://pretix.eu") }
    var token by remember { mutableStateOf("") }
    val focusRequester = FocusRequester()

    val coroutineScope = rememberCoroutineScope()

    // Auto-parse handshake JSON if token field contains it
    LaunchedEffect(token) {
        if (token.trim().startsWith("{")) {
            SetupViewModel.parseHandshakeQR(token)?.let { (parsedUrl, parsedToken) ->
                url = parsedUrl
                token = parsedToken
                coroutineScope.launch {
                    viewModel.verifyTokenAndSetup(token = parsedToken, url = parsedUrl)
                }
            }
        }
    }

    // Check and execute migration if needed on screen load
    LaunchedEffect(Unit) {
        viewModel.checkAndExecuteMigration()
    }

    Column {
        Toolbar()



        ScreenContentRoot {

            Box {
                Row {
                    Column(
                        modifier = Modifier.fillMaxHeight().padding(start = 64.dp, end = 32.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.pretix_logo_dark_angled),
                            contentDescription = "Pretix logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.width(320.dp).padding(bottom = 64.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxHeight().padding(start = 32.dp, end = 64.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(modifier = Modifier.padding(bottom = 16.dp)) {
                            Text(
                                stringResource(Res.string.connect_instruction_heading),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                        InstructionBullet(
                            style = MaterialTheme.typography.bodyLarge,
                            items = listOf(
                                stringResource(Res.string.connect_instruction_step1),
                                stringResource(Res.string.connect_instruction_step2),
                                stringResource(Res.string.connect_instruction_step3)
                            )
                        )

                        Row(modifier = Modifier.padding(vertical = 16.dp)) {
                            FieldTextInput(
                                value = url,
                                onValueChange = { url = it },
                                label = stringResource(Res.string.hint_url),
                                maxLines = 1,
                                required = true,
                                enabled = uiState != SetupUiState.Loading
                            )
                        }

                        Row(modifier = Modifier.padding(vertical = 16.dp)) {
                            FieldTextInput(
                                value = token,
                                onValueChange = { token = it },
                                label = stringResource(Res.string.hint_token),
                                maxLines = 1,
                                required = true,
                                enabled = uiState != SetupUiState.Loading,
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                            )
                        }

                        Row(
                            Modifier.fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState == SetupUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(end = 16.dp).size(24.dp)
                                )
                            }
                            Button(
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
                                // Loading indicator shown in button row
                            }

                            is SetupUiState.Success -> {
                                LaunchedEffect(Unit) {
                                    navHostController.navigate(route = Route.Main.route)
                                }
                            }

                            is SetupUiState.Error -> {
                                ErrorDialog(
                                    title = stringResource(Res.string.error_connecting_token),
                                    message = (uiState as SetupUiState.Error).exception,
                                    onDismiss = {
                                        viewModel.dismissLoginError()
                                    }
                                )
                            }

                            SetupUiState.Start -> {
                                LaunchedEffect(Unit) {
                                    focusRequester.requestFocus()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Source - https://stackoverflow.com/a
// Posted by juhopekka, modified by community. See post 'Timeline' for change history
// Retrieved 2025-11-18, License - CC BY-SA 4.0

@Composable
fun InstructionBullet(
    modifier: Modifier = Modifier,
    style: TextStyle,
    indent: Dp = 16.dp,
    lineSpacing: Dp = 0.dp,
    items: List<String>,
) {
    Column(modifier = modifier) {
        items.forEach {
            Row {
                Text(
                    text = "\u2022",
                    style = style.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.width(indent),
                )
                Text(
                    text = it,
                    style = style,
                    modifier = Modifier.weight(1f, fill = true),
                )
            }
            if (lineSpacing > 0.dp && it != items.last()) {
                Spacer(modifier = Modifier.height(lineSpacing))
            }
        }
    }
}

@Composable
private fun Toolbar(
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(CustomColor.BrandDark.asColor())
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Logo()
        Spacer(Modifier.weight(1f))
    }
}
