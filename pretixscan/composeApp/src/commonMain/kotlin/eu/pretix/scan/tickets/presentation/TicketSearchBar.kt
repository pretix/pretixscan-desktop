package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.pretix.libpretixsync.check.TicketCheckProvider
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.searchfield_prompt
import java.util.logging.Logger

private val log = Logger.getLogger("TicketSearchBar")


@Composable
fun TicketSearchBar(
    modifier: Modifier = Modifier,
    onSelectedSearchResult: (TicketCheckProvider.SearchResult) -> Unit,
    onDirectScan: (String) -> Unit = {}
) {
    val viewModel = koinViewModel<TicketSearchBarViewModel>()

    val searchQuery by viewModel.searchText.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()

    val barcodePattern = remember { Regex("[a-zA-Z0-9=+/]{5,}") }

    Column {
        Row(modifier = Modifier.padding(top = 16.dp).padding(horizontal = 16.dp)) {
            SearchTextField(
                value = searchQuery,
                hint = stringResource(Res.string.searchfield_prompt),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onSearch = viewModel::onSearchTextChange,
                onDirectScan = onDirectScan,
                onEnterPressed = {
                    if (searchSuggestions.isNotEmpty()) {
                        log.info("AutoScan: Enter pressed with search results, selecting first result")
                        viewModel.clearSearch()
                        onSelectedSearchResult(searchSuggestions.first())
                    } else if (searchQuery.matches(barcodePattern)) {
                        log.info("AutoScan: Enter pressed, barcode pattern detected, triggering direct scan")
                        onDirectScan(searchQuery)
                        viewModel.clearSearch()
                    }
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            SearchResultsView(
                searchSuggestions,
                onSelectedSearchResult = { result ->
                    // Clear search before handling result (matches old implementation)
                    viewModel.clearSearch()
                    onSelectedSearchResult(result)
                }
            )
        }
    }
}

