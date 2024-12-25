package screen.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import screen.main.search.MainTicketSearchView
import screen.main.selectevent.SelectEventDialog
import screen.main.selectlist.SelectCheckInListDialog
import screen.main.tickets.TicketHandlingview
import screen.main.toolbar.MainToolbar

@Composable
@Preview
fun MainScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MainViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

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

        is MainUiState.ReadyToScan -> {
            val data = (uiState as MainUiState.ReadyToScan<MainUiStateData>).data
            Column {
                MainToolbar(
                    viewModel = viewModel,
                    eventSelection = data.eventSelection
                )

                MainTicketSearchView(onSelectedSearchResult = {
                    coroutineScope.launch {
                        viewModel.onHandleSearchResult(it)
                    }
                })
            }
        }

        is MainUiState.HandlingTicket -> {
            val data = (uiState as MainUiState.HandlingTicket<MainUiStateData>).data
            Column {
                MainToolbar(
                    viewModel = viewModel,
                    eventSelection = data.eventSelection
                )

                MainTicketSearchView(onSelectedSearchResult = {
                    coroutineScope.launch {
                        viewModel.onHandleSearchResult(it)
                    }
                })
            }
            TicketHandlingview(
                secret = data.secret,
                onDismiss = viewModel::onHandleTicketHandlingDismissed
            )
        }
    }
}