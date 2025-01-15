package tickets.presentation


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDatepicker(
    modifier: Modifier = Modifier,
    minDate: Long?,
    maxDate: Long?,
    value: String?,
    onUpdate: (String?) -> Unit
) {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val dateMillis = try {
        value?.let {
            val parsedDate = format.parse(it)
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

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDateMillis) {
        if (selectedDateMillis != null) {
            // Convert milliseconds to Date
            val date = Date(selectedDateMillis)

            // Format the date to a string
            val dateString = format.format(date)

            // Update the formattedDate state
            onUpdate(dateString)
        } else {
            onUpdate(null)
        }
    }

    Box {
        OutlinedTextField(
            value = value ?: "",
            onValueChange = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            trailingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        )

        AnimatedVisibility(expanded) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}


