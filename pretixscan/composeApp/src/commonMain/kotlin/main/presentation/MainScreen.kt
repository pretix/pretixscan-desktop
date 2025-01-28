package main.presentation

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
import app.navigation.Route
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tickets.presentation.TicketSearchBar
import main.presentation.selectevent.SelectEventDialog
import main.presentation.selectlist.SelectCheckInListDialog
import tickets.presentation.TicketHandlingDialog
import main.presentation.toolbar.MainToolbar

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
            SelectEventDialog(onSelectEvent = viewModel::selectEvent)
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
                    eventSelection = data.eventSelection,
                    onOpenSettings = {
                        navHostController.navigate(route = Route.Settings.route)
                    }
                )

                TicketSearchBar(onSelectedSearchResult = {
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

                TicketSearchBar(onSelectedSearchResult = {
                    coroutineScope.launch {
                        viewModel.onHandleSearchResult(it)
                    }
                })
            }
            TicketHandlingDialog(
                secret = data.secret,
                onDismiss = viewModel::onHandleTicketHandlingDismissed
            )
        }
    }
}