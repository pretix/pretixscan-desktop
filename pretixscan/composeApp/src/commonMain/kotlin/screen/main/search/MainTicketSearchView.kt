package screen.main.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import eu.pretix.libpretixsync.check.TicketCheckProvider
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.searchfield_prompt
import screen.components.CustomSearchBar
import tickets.TicketCodeHandler


@OptIn(ExperimentalResourceApi::class)
@Composable
fun MainTicketSearchView(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<MainTicketSearchViewModel>()

    val searchQuery by viewModel.searchText.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchSuggestions by viewModel.searchSuggestsions.collectAsStateWithLifecycle()
    val player = koinInject<GadulkaPlayer>()

    val coroutineScope = rememberCoroutineScope()
    val codeHandler = koinInject<TicketCodeHandler>()

    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(top = 16.dp)) {
            CustomSearchBar(
                value = searchQuery,
                hint = stringResource(Res.string.searchfield_prompt),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onSearch = viewModel::onSearchTextChange
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
            SearcResultsView(
                searchSuggestions,
                onSelectedSearchResult = {
                    coroutineScope.launch {
                        codeHandler.handleScan(it.secret)
                    }
                }
            )
        }
    }
}

