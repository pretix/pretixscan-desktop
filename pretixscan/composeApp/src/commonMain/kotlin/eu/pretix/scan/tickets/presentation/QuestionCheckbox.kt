package eu.pretix.scan.tickets.presentation


import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun QuestionCheckbox(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onSelect: (String?) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { updatedChecked ->
                if (updatedChecked) {
                    onSelect("True")
                } else {
                    onSelect("False")
                }
            }
        )
        Text(
            label
        )
    }
}
