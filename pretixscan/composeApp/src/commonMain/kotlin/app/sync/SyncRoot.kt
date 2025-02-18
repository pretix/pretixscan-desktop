package app.sync


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.ui.CustomColor
import app.ui.asColor
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
        content()

        // display notifications or other UI elements depending on the sync state
        if (showMainSyncProgress) {
            when (syncState) {
                is SyncState.Error -> {
                    SyncProgressPanel(modifier = Modifier.align(Alignment.BottomCenter)) {
                        Text("Last synchronization failed: ${(syncState as SyncState.Error).message}")
                    }
                }

                is SyncState.InProgress -> {
                    SyncProgressPanel(modifier = Modifier.align(Alignment.BottomCenter)) {
                        Text("Sync ${(syncState as SyncState.InProgress).message}")
                        LinearProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                is SyncState.Success -> {
                    // show nothing
                }

                SyncState.Idle -> {
                    // show nothing
                }
            }
        }
    }
}

@Composable
fun SyncProgressPanel(modifier: Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        shadowElevation = 8.dp
    ) {
        AnimatedVisibility(true) {
            Row(
                modifier = Modifier
                    .background(CustomColor.White.asColor())
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}