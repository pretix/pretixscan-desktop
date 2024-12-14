package screen.main.selectevent

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.pretix.libpretixsync.setup.RemoteEvent
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.error_no_available_events
import screen.setup.SetupUiState

@Composable
@Preview
fun SelectEventList(
    selectedEvent: RemoteEvent? = null,
    onSelectEvent: (RemoteEvent) -> Unit = {},
) {
    val viewModel = koinViewModel<SelectEventListViewModel>()
    val uiState by viewModel.uiState.collectAsState()


    when (uiState) {
        SelectEventListUiState.Empty -> {
            Text(stringResource(Res.string.error_no_available_events))
        }

        is SelectEventListUiState.Error -> {
            Text((uiState as SetupUiState.Error).exception)
        }

        SelectEventListUiState.Loading -> {
            CircularProgressIndicator()
        }

        is SelectEventListUiState.Selecting -> {
            val list = (uiState as SelectEventListUiState.Selecting).events

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(list) { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .selectableGroup(), // accessibility for radio group
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {

                        RadioButton(
                            selected = item.slug == selectedEvent?.slug,
                            onClick = { onSelectEvent(item) },
                        )

                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            if (item.date_to != null) {
                                Text(
                                    item.date_from.toLocalDate().toString() + " - " + item.date_to?.toLocalDate()
                                        .toString()
                                )
                            } else {
                                Text(item.date_from.toLocalDate().toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

