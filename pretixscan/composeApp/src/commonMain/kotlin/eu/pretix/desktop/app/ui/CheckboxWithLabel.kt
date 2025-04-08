package eu.pretix.desktop.app.ui


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun CheckboxWithLabel(
    label: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.toggleable(
            value = checked,
            onValueChange = onCheckedChange,
            role = Role.Checkbox
        )
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                label,
                fontWeight = FontWeight.SemiBold
            )
            if (!description.isNullOrBlank()) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Light
                )
            }
        }
        Checkbox(
            checked = checked,
            onCheckedChange = null, // recommended for accessibility reasons, the .toggleable modifier on the row handles checks
            modifier = Modifier.padding(start = 16.dp).padding(vertical = 16.dp)
        )
    }
}
