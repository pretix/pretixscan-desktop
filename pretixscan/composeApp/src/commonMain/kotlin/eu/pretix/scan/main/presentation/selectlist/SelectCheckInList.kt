package eu.pretix.scan.main.presentation.selectlist


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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.pretix.libpretixsync.sqldelight.CheckInList
import eu.pretix.scan.setup.SetupUiState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.error_no_available_events

@Composable
@Preview
fun SelectCheckInList(
    selectedCheckInList: CheckInList? = null,
    onSelectCheckInList: (CheckInList) -> Unit = {},
) {
    val viewModel = koinViewModel<SelectCheckInListViewModel>()
    val uiState by viewModel.uiState.collectAsState()


    when (uiState) {
        SelectCheckInListUiState.Empty -> {
            Text(stringResource(Res.string.error_no_available_events))
        }

        is SelectCheckInListUiState.Error -> {
            Text((uiState as SetupUiState.Error).exception)
        }

        SelectCheckInListUiState.Loading -> {
            CircularProgressIndicator()
        }

        is SelectCheckInListUiState.Selecting -> {
            val list = (uiState as SelectCheckInListUiState.Selecting).lists

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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {

                        RadioButton(
                            selected = item.id == selectedCheckInList?.id,
                            onClick = { onSelectCheckInList(item) },
                        )

                        Column {
                            Text(item.name ?: "", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

