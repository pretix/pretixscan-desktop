package eu.pretix.scan.tickets.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun QuestionDatepicker(
    modifier: Modifier = Modifier,
    minDate: Long?,
    maxDate: Long?,
    value: String?,
    onUpdate: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    DatePickerField(
        value = value,
        onValueChange = onUpdate,
        minDate = minDate,
        maxDate = maxDate,
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    )
}


