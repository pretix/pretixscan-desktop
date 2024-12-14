package screen.main.toolbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.app_name
import pretixscan.composeapp.generated.resources.ic_logo
import screen.main.MainUiState
import screen.main.MainUiStateData
import screen.main.MainViewModel
import ui.CustomColor
import ui.asColour

@Composable
fun MainToolbar(modifier: Modifier = Modifier, viewModel: MainViewModel, uiState: MainUiState<MainUiStateData>) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(CustomColor.BrandDark.asColour())
            .padding(16.dp)
    ) {
        Logo()
        Button(
            modifier = Modifier.padding(horizontal = 16.dp),
            onClick = {
            viewModel.beginEventSelection()
        }) {
            Text((uiState as MainUiState.Success<MainUiStateData>).data.eventSelection.eventName)
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun Logo(modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(Res.drawable.ic_logo),
            contentDescription = "Pretix logo"
        )
        Text(
            stringResource(Res.string.app_name),
            style = MaterialTheme.typography.bodyLarge,
            color = CustomColor.White.asColour()
        )
    }
}