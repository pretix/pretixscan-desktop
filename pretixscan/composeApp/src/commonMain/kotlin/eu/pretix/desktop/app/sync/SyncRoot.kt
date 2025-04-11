package eu.pretix.desktop.app.sync


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun SyncRoot(
    navHostController: NavHostController,
    content: @Composable () -> Unit
) {

    val viewModel = koinViewModel<SyncRootService>()

    val syncState by viewModel.syncState.collectAsState()

    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // call a VM method when route changes
    LaunchedEffect(currentRoute) {
        currentRoute?.let { viewModel.onRouteChanged(it) }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.skipFutureSyncs()
        }
    }

    val showMainSyncProgress by viewModel.showMainSyncProgress.collectAsState()
    print("Current sync state: $syncState")

    Box(Modifier.fillMaxSize()) {
        // render child content
        CompositionLocalProvider(LocalSyncRootService provides viewModel) {
            content() // Provide service to all children
        }

        // display notifications or other UI elements depending on the sync state
        if (showMainSyncProgress) {
            SyncProgressPanel(modifier = Modifier.align(Alignment.BottomStart)) {
                SyncProgressView(syncState)
            }
        }
    }
}