package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eu.pretix.desktop.app.ui.FieldTextInput
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.ok
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionTimePicker(
    modifier: Modifier = Modifier,
    value: String?,
    onUpdate: (String?) -> Unit,
    label: String
) {
    // Parse time value (format: HH:mm)
    val (hourValue, minuteValue) = if (value != null) {
        try {
            val parts = value.split(":")
            if (parts.size == 2) {
                parts[0].toInt() to parts[1].toInt()
            } else {
                0 to 0
            }
        } catch (e: Exception) {
            0 to 0
        }
    } else {
        0 to 0
    }

    var expanded by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = hourValue,
        initialMinute = minuteValue,
        is24Hour = true
    )

    val selectedHour = timePickerState.hour
    val selectedMinute = timePickerState.minute

    LaunchedEffect(selectedHour, selectedMinute) {
        onUpdate(formatTime(selectedHour, selectedMinute))
    }

    // Convert stored value to display format
    val displayValue = formatTimeForDisplay(value)

    Box(modifier = modifier) {
        FieldTextInput(
            value = displayValue,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = !expanded },
            trailing = {
                Icon(Icons.Default.Schedule, contentDescription = null)
            }
        )

        if (expanded) {
            Dialog(onDismissRequest = {
                expanded = false
            }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            label,
                            maxLines = 2
                        )
                        TimePicker(
                            state = timePickerState,
                            layoutType = TimePickerLayoutType.Horizontal
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(onClick = { expanded = false }) {
                                Text(stringResource(Res.string.ok))
                            }
                        }
                    }
                }

            }
        }
    }
}

fun formatTime(hour: Int, minute: Int): String {
    val time = LocalTime.of(hour, minute)
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return time.format(formatter)
}