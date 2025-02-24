package app.ui


import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.navigation.Route
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tickets.presentation.TicketSearchBar
import main.presentation.selectevent.SelectEventDialog
import main.presentation.selectlist.SelectCheckInListDialog
import tickets.presentation.TicketHandlingDialog
import main.presentation.toolbar.MainToolbar

@Composable
@Preview
fun ScreenRoot(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        content()
    }
}