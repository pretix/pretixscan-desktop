package eu.pretix.scan.tickets.presentation


import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp


@Composable
fun QuestionCheckbox(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onSelect: (String?) -> Unit
) {
    Row(
        modifier = modifier
            .padding(vertical = 4.dp)
            .selectable(
                selected = checked,
                onClick = {
                    if (checked) {
                        onSelect("False")
                    } else {
                        onSelect("True")
                    }
                },
                indication = LocalIndication.current,
                interactionSource = null,
                role = Role.Checkbox
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            label
        )
    }
}
