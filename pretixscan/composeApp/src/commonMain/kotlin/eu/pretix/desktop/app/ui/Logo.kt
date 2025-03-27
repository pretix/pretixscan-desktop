package eu.pretix.desktop.app.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.app_name
import pretixscan.composeapp.generated.resources.ic_logo

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
            color = CustomColor.White.asColor()
        )
    }
}