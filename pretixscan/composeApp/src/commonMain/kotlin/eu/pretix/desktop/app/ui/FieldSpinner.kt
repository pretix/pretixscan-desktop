package eu.pretix.desktop.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.choose_option


@Composable
fun FieldSpinner(
    modifier: Modifier = Modifier,
    selectedValue: String?,
    availableOptions: List<KeyValueOption>,
    onSelect: (KeyValueOption?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    fun lookUpSelectedValue(value: String?): String? {
        if (value != null) {
            return availableOptions.firstOrNull { it.value == value }?.key
        }
        return null
    }

    Box(modifier = modifier, contentAlignment = Alignment.TopStart) {
        TextButton(
            onClick = { expanded = true },
            enabled = availableOptions.isNotEmpty(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            shape = RectangleShape
        ) {
            Row {
                Text(
                    lookUpSelectedValue(selectedValue) ?: stringResource(Res.string.choose_option),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = CustomColor.BrandDark.asColor()
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            availableOptions.forEachIndexed { _, option ->
                DropdownMenuItem(
                    text = {
                        Text(option.key)
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
