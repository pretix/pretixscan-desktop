package eu.pretix.scan.main.presentation

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import eu.pretix.desktop.app.navigation.Route
import eu.pretix.desktop.app.scan.GlobalScanSetup
import eu.pretix.desktop.app.ui.ScreenContentRoot
import eu.pretix.scan.main.presentation.selectevent.SelectEventDialog
import eu.pretix.scan.main.presentation.selectlist.SelectCheckInListDialog
import eu.pretix.scan.main.presentation.selectlist.SelectCheckInListForMultiEventDialog
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

    GlobalScanSetup(
        stateFlow = viewModel.uiState,
        onHandleDirectScan = { secret ->
            coroutineScope.launch {
                viewModel.onHandleDirectScan(secret)
            }
        }
    )

    when (uiState) {
        MainUiState.SelectEvent -> {
            SelectEventDialog(
                onSelectEvent = {
                    coroutineScope.launch {
                        viewModel.selectEvent(it)
                    }
                },
                onSelectMultipleEvents = { events ->
                    coroutineScope.launch {
                        viewModel.selectMultipleEvents(events)
                    }
                }
            )
        }

        MainUiState.SelectCheckInList -> {
            SelectCheckInListDialog(onSelectCheckInList = {
                viewModel.selectCheckInList(it)
            })
        }

        is MainUiState.SelectCheckInListsForMultipleEvents -> {
            val state = uiState as MainUiState.SelectCheckInListsForMultipleEvents
            SelectCheckInListForMultiEventDialog(
                currentEvent = state.events[state.currentEventIndex],
                currentEventIndex = state.currentEventIndex,
                totalEvents = state.events.size,
                onSelectCheckInList = { listId ->
                    viewModel.selectCheckInListForCurrentEvent(listId)
                }
            )
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
                    onOpenSettings = {
                        navHostController.navigate(route = Route.Settings.route)
                    },
                    onOpenStatistics = {
                        navHostController.navigate(route = Route.EventStats.route)
                    }
                )

                ScreenContentRoot {
                    TicketSearchBar(
                        onSelectedSearchResult = {
                            coroutineScope.launch {
                                viewModel.onHandleSearchResult(it)
                            }
                        },
                        onDirectScan = { secret ->
                            coroutineScope.launch {
                                viewModel.onHandleDirectScan(secret)
                            }
                        }
                    )
                }

            }
        }

        is MainUiState.HandlingTicket -> {
            val data = (uiState as MainUiState.HandlingTicket<MainUiStateData>).data
            Column {
                MainToolbar(
                    viewModel = viewModel
                )

                ScreenContentRoot {
                    TicketSearchBar(
                        onSelectedSearchResult = {
                            coroutineScope.launch {
                                viewModel.onHandleSearchResult(it)
                            }
                        },
                        onDirectScan = { secret ->
                            coroutineScope.launch {
                                viewModel.onHandleDirectScan(secret)
                            }
                        }
                    )
                }
            }
            TicketHandlingDialog(
                secret = data.secret,
                scanTimestamp = data.scanTimestamp,
                onDismiss = viewModel::onHandleTicketHandlingDismissed,
                onResultStateChanged = viewModel::onTicketResultDetermined
            )
        }
    }

}