package eu.pretix.scan.tickets.presentation


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

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

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

    val dateMillis = try {
        value?.let {
            val parsedDate = dateFormat.parse(it)
            parsedDate?.time?.coerceIn(minDate, maxDate)
        }
    } catch (e: Exception) {
        null
    } ?: minDate  // Default to minDate if parsing fails or value is null

    // Define selectable dates criteria
    val selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            if (minDate != null && utcTimeMillis < minDate) {
                return false
            }

            if (maxDate != null && utcTimeMillis > maxDate) {
                return false
            }

            return true
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateMillis,
        initialDisplayedMonthMillis = dateMillis,
        selectableDates = selectableDates
    )

    // Collect the selected date in milliseconds as state
    val selectedDateMillis = datePickerState.selectedDateMillis


    val hourValue = try {
        value?.let {
            val parsedDate = timeFormat.parse(it)
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate
            calendar.get(Calendar.HOUR_OF_DAY)
        }
    } catch (e: Exception) {
        null
    } ?: 0

    val minuteValue = try {
        value?.let {
            val parsedDate = timeFormat.parse(it)
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate
            calendar.get(Calendar.MINUTE)
        }
    } catch (e: Exception) {
        null
    } ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = hourValue,
        initialMinute = minuteValue,
        is24Hour = true
    )

    val selectedHour = timePickerState.hour
    val selectedMinute = timePickerState.minute

    var expansion by remember { mutableStateOf(QuestionDateTimePickerExpansion.NONE) }

    var visibleDate by remember { mutableStateOf<String?>(null) }
    var visibleTime by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        if (selectedDateMillis != null) {
            val dateString = calculateDateTime(selectedDateMillis, selectedHour, selectedMinute)
            visibleDate = calculateDate(dateString)
            visibleTime = calculateTime(dateString)
        } else {
            visibleDate = null
            visibleTime = null
        }
    }

    LaunchedEffect(selectedDateMillis, selectedHour, selectedMinute) {
        if (selectedDateMillis != null) {
            val dateString = calculateDateTime(selectedDateMillis, selectedHour, selectedMinute)
            visibleDate = calculateDate(dateString)
            visibleTime = calculateTime(dateString)
            onUpdate(dateString)
        } else {
            visibleDate = null
            visibleTime = null
            onUpdate(null)
        }
    }

    Column {
        Row {
            OutlinedTextField(
                value = visibleDate ?: "",
                onValueChange = {},
                enabled = false,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        expansion = if (expansion == QuestionDateTimePickerExpansion.DATE) {
                            QuestionDateTimePickerExpansion.NONE
                        } else {
                            QuestionDateTimePickerExpansion.DATE
                        }
                    },
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                }
            )

            OutlinedTextField(
                value = visibleTime ?: "",
                onValueChange = {},
                enabled = false,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        expansion = if (expansion == QuestionDateTimePickerExpansion.TIME) {
                            QuestionDateTimePickerExpansion.NONE
                        } else {
                            QuestionDateTimePickerExpansion.TIME
                        }
                    },
                trailingIcon = {
                    Icon(Icons.Outlined.DateRange, contentDescription = null)
                }
            )
        }

        when (expansion) {
            QuestionDateTimePickerExpansion.NONE -> {}
            QuestionDateTimePickerExpansion.DATE -> {
                DatePicker(
                    state = datePickerState
                )
            }

            QuestionDateTimePickerExpansion.TIME -> {
                TimePicker(
                    state = timePickerState,
                    layoutType = TimePickerLayoutType.Horizontal
                )
            }
        }
    }
}

val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")

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


fun calculateDateTime(dateMillis: Long?, hour: Int, minute: Int): String? {
    if (dateMillis == null) {
        // the value is empty without a date selection
        return null
    }

    // Validate input parameters
    require(hour in 0..23) { "Hour must be between 0 and 23 inclusive." }
    require(minute in 0..59) { "Minute must be between 0 and 59 inclusive." }

    // Define the system default time zone
    val zoneId = ZoneId.systemDefault()

    // Determine the base date
    val baseLocalDate: LocalDate = Instant.ofEpochMilli(dateMillis).atZone(zoneId).toLocalDate()

    // Create a LocalDateTime by combining the base date with the specified time
    val localDateTime = LocalDateTime.of(baseLocalDate, LocalTime.of(hour, minute))

    // Convert LocalDateTime to ZonedDateTime using the system default time zone
    val zonedDateTime = localDateTime.atZone(zoneId)

    // Convert ZonedDateTime to Instant
    val instant = zonedDateTime.toInstant()

    // Convert Instant to Date
    val date = Date.from(instant)
    if (date != null) {
        return format.format(date)
    }
    return null
}