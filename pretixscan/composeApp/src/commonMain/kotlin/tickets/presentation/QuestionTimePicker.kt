package tickets.presentation


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionTimePicker(
    modifier: Modifier = Modifier,
    value: String?,
    onUpdate: (String?) -> Unit
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

        AnimatedVisibility(expanded) {
            TimePicker(
                state = timePickerState,
                layoutType = TimePickerLayoutType.Horizontal
            )
        }
    }
}

fun formatTime(hour: Int, minute: Int): String {
    val time = LocalTime.of(hour, minute)
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return time.format(formatter)
}