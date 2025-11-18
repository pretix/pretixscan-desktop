package eu.pretix.scan.main.presentation.selectlist


import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Divider
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.SelectListRow
import eu.pretix.libpretixsync.sqldelight.CheckInList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.error_no_available_events

@Composable
@Preview
fun SelectCheckInList(
    eventSlug: String,
    subEventId: Long?,
    selectedCheckInList: CheckInList? = null,
    onSelectCheckInList: (CheckInList) -> Unit = {},
) {
    val viewModel: SelectCheckInListViewModel = koinViewModel(
        key = "SelectCheckInList$eventSlug",
        parameters = { parametersOf(eventSlug, subEventId) }
    )

    val uiState by viewModel.uiState.collectAsState()
    val state = rememberLazyListState()


    when (uiState) {
        SelectCheckInListUiState.Empty -> {
            SelectListRow {
                Text(stringResource(Res.string.error_no_available_events))
            }
        }

        is SelectCheckInListUiState.Error -> {
            SelectListRow {
                Text((uiState as SelectCheckInListUiState.Error).exception)
            }
        }

        SelectCheckInListUiState.Loading -> {
            SelectListRow {
                Row(horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        is SelectCheckInListUiState.Selecting -> {
            val list = (uiState as SelectCheckInListUiState.Selecting).lists

            Box {
                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    itemsIndexed(list) { index, item ->
                        Row(
                            Modifier
                                .selectable(
                                    selected = item.id == selectedCheckInList?.id,
                                    onClick = { onSelectCheckInList(item) },
                                    indication = LocalIndication.current,
                                    interactionSource = null,
                                    role = Role.Switch
                                )
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .selectableGroup(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {

                            Checkbox(
                                checked = item.id == selectedCheckInList?.id,
                                onCheckedChange = { onSelectCheckInList(item) }
                            )

                            Column {
                                Text(item.name ?: "", fontWeight = FontWeight.Bold)
                            }
                        }
                        if (index < list.lastIndex) {
                            Divider(color = Color.Gray, thickness = 1.dp)
                        }
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = state
                    )
                )
            }
        }
    }
}

