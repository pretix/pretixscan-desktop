package eu.pretix.scan.tickets.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.pretix.desktop.app.ui.FieldTextInput
import eu.pretix.scan.tickets.data.getDateFormat
import eu.pretix.scan.tickets.data.getDisplayDateFormat
import eu.pretix.scan.tickets.data.getDisplayTimeFormat
import eu.pretix.scan.tickets.data.getTimeFormat
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.cancel
import pretixscan.composeapp.generated.resources.ok
import java.util.*

/**
 * Creates a SelectableDates object that restricts date selection based on min/max bounds
 */
@OptIn(ExperimentalMaterial3Api::class)
fun createSelectableDates(minDate: Long?, maxDate: Long?): SelectableDates {
    return object : SelectableDates {
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
}

/**
 * Parses a date string and returns milliseconds, constrained by min/max bounds
 */
fun parseDateToMillis(value: String?, minDate: Long?, maxDate: Long?): Long? {
    val format = getDateFormat()
    return try {
        value?.let {
            val parsedDate = format.parse(it)
            parsedDate?.time?.coerceIn(minDate, maxDate)
        }
    } catch (e: Exception) {
        null
    } ?: minDate
}

/**
 * Converts a stored date value to display format using locale-aware formatting
 */
fun formatDateForDisplay(value: String?): String {
    if (value == null) return ""

    val format = getDateFormat()
    val displayFormat = getDisplayDateFormat()

    return try {
        val date = format.parse(value)
        displayFormat.format(date)
    } catch (e: Exception) {
        value
    }
}

/**
 * Converts a stored time value to display format using locale-aware formatting
 */
fun formatTimeForDisplay(value: String?): String {
    if (value == null) return ""

    val format = getTimeFormat()
    val displayFormat = getDisplayTimeFormat()

    return try {
        val time = format.parse(value)
        displayFormat.format(time)
    } catch (e: Exception) {
        value
    }
}

/**
 * A reusable date picker field with locale-aware display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String?,
    onValueChange: (String?) -> Unit,
    minDate: Long? = null,
    maxDate: Long? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {}
) {
    val format = getDateFormat()
    val displayValue = formatDateForDisplay(value)

    val dateMillis = parseDateToMillis(value, minDate, maxDate)
    val selectableDates = createSelectableDates(minDate, maxDate)

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateMillis,
        initialDisplayedMonthMillis = dateMillis,
        selectableDates = selectableDates
    )

    FieldTextInput(
        value = displayValue,
        onValueChange = {},
        enabled = false,
        modifier = modifier,
        onClick = if (enabled) ({ onExpandedChange(true) }) else null,
        trailing = {
            Icon(Icons.Default.DateRange, contentDescription = null)
        }
    )

    if (expanded) {
        DatePickerDialog(
            onDismissRequest = { onExpandedChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis != null) {
                            val date = Date(selectedDateMillis)
                            val dateString = format.format(date)
                            onValueChange(dateString)
                        }
                        onExpandedChange(false)
                    }
                ) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { onExpandedChange(false) }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}