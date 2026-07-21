package eu.pretix.scan.main.presentation.toolbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.Tooltip
import eu.pretix.desktop.app.ui.asColor
import eu.pretix.scan.main.presentation.EventSelectionDisplay
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.events_selected
import pretixscan.composeapp.generated.resources.operation_select_event

@Composable
fun EventSelectionButton(
    display: EventSelectionDisplay,
    onClick: () -> Unit
) {
    val primaryText: String
    val secondaryText: String
    val tooltipText: String

    when (display) {
        is EventSelectionDisplay.Single -> {
            primaryText = display.eventName
            secondaryText = display.listName
            tooltipText = "${display.eventName} - ${display.listName}"
        }
        is EventSelectionDisplay.Multiple -> {
            primaryText = stringResource(Res.string.events_selected, display.selections.size)
            secondaryText = ""
            tooltipText = display.selections.joinToString("\n") { (eventName, listName) ->
                "$eventName - $listName"
            }
        }
    }

    Tooltip(tooltipText) {
        Button(
            modifier = Modifier.padding(horizontal = 16.dp),
            onClick = onClick
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.widthIn(max = 200.dp)) {
                    Text(
                        text = primaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (secondaryText.isNotEmpty()) {
                        Text(
                            text = secondaryText,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(Res.string.operation_select_event),
                    tint = CustomColor.White.asColor()
                )
            }
        }
    }
}
