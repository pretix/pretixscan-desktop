package eu.pretix.scan.status.presentation

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import eu.pretix.desktop.app.ui.ScreenContentRoot
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.scan.settings.presentation.Toolbar
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.already_scanned
import pretixscan.composeapp.generated.resources.currently_inside
import pretixscan.composeapp.generated.resources.total_tickets_sold


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
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val listState = rememberLazyListState()
                        val data = (uiState as StatusUiState.Success<TicketCheckProvider.StatusResult>).data
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            state = listState
                        ) {
                            item {
                                CardRow {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                        Text(data.eventName ?: "", style = MaterialTheme.typography.titleMedium)
                                    }

                                    Row(
                                        Modifier.padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CardMetricItem(
                                            data.totalTickets.toString(),
                                            stringResource(Res.string.total_tickets_sold)
                                        )

                                        Spacer(Modifier.width(128.dp))

                                        CardMetricItem(
                                            data.alreadyScanned.toString(),
                                            stringResource(Res.string.already_scanned)
                                        )
                                    }

                                    if (data.currentlyInside != null) {
                                        Row(horizontalArrangement = Arrangement.Center) {
                                            CardMetricItem(
                                                data.currentlyInside.toString(),
                                                stringResource(Res.string.currently_inside)
                                            )
                                        }
                                    }

                                }
                            }

                            items(
                                items = data.items ?: emptyList<TicketCheckProvider.StatusResultItem>(),
                                key = { iot -> iot.id }) { item ->
                                CardRow {
                                    CardHeading(item.name ?: "", item.checkins.toString() + "/" + item.total.toString())
                                    item.variations?.forEach { variation ->
                                        CardFact(
                                            variation.name ?: "",
                                            variation.checkins.toString() + "/" + variation.total.toString()
                                        )
                                    }
                                }
                            }
                        }

                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(
                                scrollState = listState
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardRow(content: @Composable () -> Unit) {
    OutlinedCard(Modifier.padding(horizontal = 16.dp)) {
        Box {
            Column(
                Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                content()
            }
        }
    }
}

@Composable
fun CardMetricItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics {
            // This will be read as: "Label: Value"
            contentDescription = "$label: $value"
        }
    ) {
        Text(value, style = MaterialTheme.typography.headlineLarge)
        Text(label)
    }
}

@Composable
fun CardHeading(titleStart: String, titleFill: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(titleStart, style = MaterialTheme.typography.titleMedium)
        Text(
            titleFill,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun CardFact(titleStart: String, titleFill: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(titleStart, style = MaterialTheme.typography.titleSmall)
        Text(
            titleFill,
            style = MaterialTheme.typography.titleSmall
        )
    }
}