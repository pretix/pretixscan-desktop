package eu.pretix.scan.main.presentation

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import eu.pretix.desktop.app.navigation.Route
import eu.pretix.desktop.app.ui.ScreenContentRoot
import eu.pretix.scan.main.presentation.selectevent.SelectEventDialog
import eu.pretix.scan.main.presentation.selectlist.SelectCheckInListDialog
import eu.pretix.scan.main.presentation.toolbar.MainToolbar
import eu.pretix.scan.tickets.presentation.TicketHandlingDialog
import eu.pretix.scan.tickets.presentation.TicketSearchBar
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun MainScreen(
    navHostController: NavHostController,
) {
    val viewModel = koinViewModel<MainViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()


    when (uiState) {
        MainUiState.SelectEvent -> {
            SelectEventDialog(onSelectEvent = {
                coroutineScope.launch {
                    viewModel.selectEvent(it)
                }
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
                    eventSelection = data.eventSelection,
                    onOpenSettings = {
                        navHostController.navigate(route = Route.Settings.route)
                    },
                    onOpenStatistics = {
                        navHostController.navigate(route = Route.EventStats.route)
                    }
                )

                ScreenContentRoot {
                    TicketSearchBar(onSelectedSearchResult = {
                        coroutineScope.launch {
                            viewModel.onHandleSearchResult(it)
                        }
                    })
                }

            }
        }

        is MainUiState.HandlingTicket -> {
            val data = (uiState as MainUiState.HandlingTicket<MainUiStateData>).data
            Column {
                MainToolbar(
                    viewModel = viewModel,
                    eventSelection = data.eventSelection
                )

                ScreenContentRoot {
                    TicketSearchBar(onSelectedSearchResult = {
                        coroutineScope.launch {
                            viewModel.onHandleSearchResult(it)
                        }
                    })
                }
            }
            TicketHandlingDialog(
                secret = data.secret,
                onDismiss = viewModel::onHandleTicketHandlingDismissed
            )
        }
    }

}