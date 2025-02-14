package app.sync


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
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
//
//    DisposableEffect(Unit) {
//        onDispose {
//            viewModel.pauseSync()
//        }
//    }

    Box(Modifier.fillMaxSize()) {
        // render child content
        content()

        // display notifications or other UI elements depending on the sync state
        when (syncState) {
            is SyncState.Error -> Text("Sync failed.")
            is SyncState.InProgress -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
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
                            Text("Sync ${(syncState as SyncState.InProgress).message}")
                            LinearProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            is SyncState.Success -> {}
            else -> {
            //idle
            }
        }
    }
}