package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.ok
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionTimePicker(
    modifier: Modifier = Modifier,
    value: String?,
    onUpdate: (String?) -> Unit,
    label: String
) {
    val format = SimpleDateFormat("HH:mm", Locale.US)

    val hourValue = try {
        value?.let {
            val parsedDate = format.parse(it)
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate
            calendar.get(Calendar.HOUR_OF_DAY)
        }
    } catch (e: Exception) {
        null
    } ?: 0

    val minuteValue = try {
        value?.let {
            val parsedDate = format.parse(it)
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate
            calendar.get(Calendar.MINUTE)
        }
    } catch (e: Exception) {
        null
    } ?: 0

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


    Box(modifier = modifier) {
        OutlinedTextField(
            value = value ?: "",
            onValueChange = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            trailingIcon = {
                Icon(Icons.Outlined.DateRange, contentDescription = null)
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
                           Button(onClick = {expanded = false}) {
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