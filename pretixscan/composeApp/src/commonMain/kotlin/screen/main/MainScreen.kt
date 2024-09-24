package screen.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import screen.main.selectevent.SelectEventDialog

@Composable
@Preview
fun MainScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MainViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    Text("Main Screen")

    when (uiState) {
        MainUiState.SelectEvent -> {
            SelectEventDialog(onDismissRequest = {

            })
        }

        MainUiState.Start -> {
            // nothing to do
        }

        MainUiState.SelectCheckInList -> TODO()
    }
}