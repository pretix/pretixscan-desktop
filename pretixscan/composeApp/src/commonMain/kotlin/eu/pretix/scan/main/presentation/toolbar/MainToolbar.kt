package eu.pretix.scan.main.presentation.toolbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.Logo
import eu.pretix.desktop.app.ui.Tooltip
import eu.pretix.desktop.app.ui.asColor
import eu.pretix.desktop.cache.EventSelection
import eu.pretix.scan.main.presentation.MainViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainToolbar(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    eventSelection: EventSelection,
    onOpenSettings: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(CustomColor.BrandDark.asColor())
            .padding(16.dp)
    ) {
        Logo()
        Button(
            modifier = Modifier.padding(horizontal = 16.dp),
            onClick = {
                viewModel.beginEventSelection()
            }) {
            Row {
                Text(eventSelection.eventName)
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(Res.string.operation_select_event),
                    tint = CustomColor.White.asColor()
                )
            }
        }
        Spacer(Modifier.weight(1f))

        Tooltip(stringResource(Res.string.action_sync)) {
            IconButton(onClick = {
                viewModel.performFullSync()
            }) {
                Image(
                    painter = painterResource(Res.drawable.ic_refresh_white_24dp),
                    contentDescription = stringResource(Res.string.action_sync)
                )
            }
        }

        Tooltip(stringResource(Res.string.action_label_settings)) {
            IconButton(onClick = onOpenSettings) {
                Image(
                    painter = painterResource(Res.drawable.ic_settings_white_24dp),
                    contentDescription = stringResource(Res.string.action_label_settings)
                )
            }
        }
    }
}

