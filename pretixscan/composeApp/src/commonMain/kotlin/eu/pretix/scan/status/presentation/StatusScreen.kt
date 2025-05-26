package eu.pretix.scan.status.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import eu.pretix.desktop.app.ui.ScreenContentRoot
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.scan.settings.presentation.Toolbar
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel


@Composable
@Preview
fun StatusScreen(
    navHostController: NavHostController,
) {
    val viewModel = koinViewModel<StatusScreenViewModel>()

    val uiState by viewModel.status.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }

    Column {
        Toolbar(onGoBack = {
            navHostController.popBackStack()
        })

        ScreenContentRoot {
            when (uiState) {
                StatusUiState.FailedToLoadStats -> {
                    Column(Modifier.fillMaxSize().padding(16.dp)) {
                        // FIXME: Improve error message
                        Text("There was a problem loading stats for this event.")
                    }
                }

                StatusUiState.Loading -> {
                    Column(
                        Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is StatusUiState.Success -> {
                    val data = (uiState as StatusUiState.Success<TicketCheckProvider.StatusResult>).data
                    Column(Modifier.fillMaxSize().padding(16.dp)) {
                        Text(data.eventName ?: "", style = MaterialTheme.typography.titleMedium)

                        Text(data.alreadyScanned.toString() + "/" + data.totalTickets.toString())

                        if (data.currentlyInside != null) {
                            Text(data.currentlyInside.toString())
                        }
                        data.items?.forEach { item ->
                            Row {
                                Text(item.name ?: "", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    item.checkins.toString() + "/" + item.total.toString(),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            item.variations?.forEach { variation ->
                                Row {
                                    Text(variation.name ?: "", style = MaterialTheme.typography.titleSmall, fontStyle = FontStyle.Italic)
                                    Text(
                                        variation.checkins.toString() + "/" + variation.total.toString(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}