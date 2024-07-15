package screen.setup


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import navigation.Route
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import pretixscan.composeapp.generated.resources.*
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.headline_setup
import pretixscan.composeapp.generated.resources.welcome_disclaimer1
import pretixscan.composeapp.generated.resources.welcome_text
import ui.CustomColor
import ui.parseColor

@Composable
@Preview
fun SetupScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<SetupViewModel>()

    val statusMessage by viewModel.text.collectAsState("")
    var url by remember { mutableStateOf("https://pretix.eu") }
    var token by remember { mutableStateOf("") }
    val focusRequester = FocusRequester()

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
            Text(statusMessage)
        }

        Row(
            Modifier.fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(parseColor(CustomColor.BrandGreen.hex))),
                onClick = {
                    viewModel.verifyToken(token = token, url = url)
                }) {
                Text(stringResource(Res.string.connect_check_token))
            }
        }
    }
}