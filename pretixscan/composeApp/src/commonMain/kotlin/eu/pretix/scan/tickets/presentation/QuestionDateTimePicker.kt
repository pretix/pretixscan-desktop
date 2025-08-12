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
import pretixscan.composeapp.generated.resources.cancel
import pretixscan.composeapp.generated.resources.ok
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

enum class QuestionDateTimePickerExpansion {
    NONE,
    DATE,
    TIME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDateTimePicker(
    modifier: Modifier = Modifier,
    minDate: Long?,
    maxDate: Long?,
    value: String?,
    onUpdate: (String?) -> Unit
) {
    // Parse the date and time from the datetime value (format: yyyy-MM-dd'T'HH:mm)
    var dateValue by remember(value) { mutableStateOf(calculateDate(value)) }
    var timeValue by remember(value) { mutableStateOf(calculateTime(value) ?: "00:00") }

    // Parse time for the time picker
    val (hourValue, minuteValue) = try {
        val parts = timeValue.split(":")
        if (parts.size == 2) {
            parts[0].toInt() to parts[1].toInt()
        } else {
            0 to 0
        }
    } catch (_: Exception) {
        0 to 0
    }

    val timePickerState = rememberTimePickerState(
        initialHour = hourValue,
        initialMinute = minuteValue,
        is24Hour = true
    )

    val selectedHour = timePickerState.hour
    val selectedMinute = timePickerState.minute

    var expansion by remember { mutableStateOf(QuestionDateTimePickerExpansion.NONE) }

    // Update the combined datetime value when either date or time changes
    LaunchedEffect(dateValue, selectedHour, selectedMinute) {
        if (dateValue != null) {
            // Combine date and time into datetime format
            val time = LocalTime.of(selectedHour, selectedMinute)
            val timeString = time.format(DateTimeFormatter.ofPattern("HH:mm"))
            val dateTimeString = "$dateValue" + "T" + timeString
            onUpdate(dateTimeString)
        } else {
            onUpdate(null)
        }
    }

    // Convert stored values to display format
    val timeString = LocalTime.of(selectedHour, selectedMinute).format(DateTimeFormatter.ofPattern("HH:mm"))
    val displayTime = formatTimeForDisplay(timeString)

    Row(modifier = modifier) {
        // Use the shared DatePickerField for the date portion
        DatePickerField(
            value = dateValue,
            onValueChange = { newDate ->
                dateValue = newDate
            },
            minDate = minDate,
            maxDate = maxDate,
            modifier = Modifier.weight(1f),
            expanded = expansion == QuestionDateTimePickerExpansion.DATE,
            onExpandedChange = { isExpanded ->
                expansion =
                    if (isExpanded) QuestionDateTimePickerExpansion.DATE else QuestionDateTimePickerExpansion.NONE
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        FieldTextInput(
            value = displayTime,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.weight(1f),
            onClick = {
                expansion = QuestionDateTimePickerExpansion.TIME
            },
            trailing = {
                Icon(Icons.Default.Schedule, contentDescription = null)
            }
        )
    }

    // Only show time dialog, date dialog is handled by DatePickerField
    if (expansion == QuestionDateTimePickerExpansion.TIME) {
        Dialog(
            onDismissRequest = { expansion = QuestionDateTimePickerExpansion.NONE }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(
                        state = timePickerState,
                        layoutType = TimePickerLayoutType.Vertical
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { expansion = QuestionDateTimePickerExpansion.NONE }) {
                            Text(stringResource(Res.string.cancel))
                        }
                        TextButton(onClick = { expansion = QuestionDateTimePickerExpansion.NONE }) {
                            Text(stringResource(Res.string.ok))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Extracts the date part from a date-time string in the format "yyyy-MM-dd'T'HH:mm".
 *
 * @param value The input date-time string. Example: "2023-04-24T14:30"
 * @return The date part in "yyyy-MM-dd" format. Returns null if input is null or invalid.
 */
fun calculateDate(value: String?): String? {
    // Return null if input is null
    if (value == null) return null

    return try {
        // Define the input formatter
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

        // Parse the input string to LocalDateTime
        val dateTime = LocalDateTime.parse(value, inputFormatter)

        // Extract the date part
        val date = dateTime.toLocalDate()

        // Define the output formatter (optional if you want to enforce the format)
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Format the date to string
        date.format(outputFormatter)
    } catch (e: DateTimeParseException) {
        // Return null if parsing fails
        e.printStackTrace()
        null
    }
}

/**
 * Extracts the time part from a date-time string in the format "yyyy-MM-dd'T'HH:mm".
 *
 * @param value The input date-time string. Example: "2023-04-24T14:30"
 * @return The time part in "HH:mm" format. Returns null if input is null or invalid.
 */
fun calculateTime(value: String?): String? {
    // Return null if input is null
    if (value == null) return null

    return try {
        // Define the input formatter
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

        // Parse the input string to LocalDateTime
        val dateTime = LocalDateTime.parse(value, inputFormatter)

        // Extract the time part
        val time = dateTime.toLocalTime()

        // Define the output formatter (optional if you want to enforce the format)
        val outputFormatter = DateTimeFormatter.ofPattern("HH:mm")

        // Format the time to string
        time.format(outputFormatter)
    } catch (e: DateTimeParseException) {
        // Return null if parsing fails
        null
    }
}

