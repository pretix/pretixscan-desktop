package screen.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import screen.main.selectevent.SelectEventDialog
import screen.main.selectlist.SelectCheckInListDialog

@Composable
@Preview
fun MainScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MainViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {

        MainUiState.SelectEvent -> {
            SelectEventDialog(onSelectEvent = {
                viewModel.selectEvent(it)
            })
        }

        MainUiState.SelectCheckInList -> {
            SelectCheckInListDialog(onSelectCheckInList = {
                viewModel.selectCheckInList(it)
            })
        }

        MainUiState.Loading -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is MainUiState.Success -> {
            Column {
                Button(onClick = {
                    viewModel.beginEventSelection()
                }) {
                    Text((uiState as MainUiState.Success<MainUiStateData>).data.eventSelection.eventName)
                }
            }
        }
    }
}