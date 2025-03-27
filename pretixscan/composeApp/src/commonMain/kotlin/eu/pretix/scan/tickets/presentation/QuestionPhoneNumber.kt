package eu.pretix.scan.tickets.presentation


import androidx.compose.foundation.layout.Row
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vanniktech.locale.Country
import eu.pretix.desktop.app.ui.FieldSpinner
import eu.pretix.desktop.app.ui.KeyValueOption
import java.util.*


@Composable
fun QuestionPhoneNumber(
    modifier: Modifier = Modifier,
    selectedValue: String?,
    onSelect: (String?) -> Unit
) {
    var country by remember { mutableStateOf<Country>(calculateDefaultCountry(selectedValue)) }

    LaunchedEffect(Unit, selectedValue) {
        country = calculateDefaultCountry(selectedValue)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FieldSpinner(
            modifier = Modifier.weight(1f),
            selectedValue = country.code,
            availableOptions = Country.entries.map { country -> KeyValueOption("${country.emoji} ${country.name}", country.code) },
            onSelect = {
                if (it == null) {
                    // nothing to do
                } else {
                    val newCountry = Country.entries.firstOrNull { c -> c.code == it.value }
                    if (newCountry != null) {
                        country = newCountry
                    }
                }
            }
        )
        TextField(
            modifier = Modifier.weight(3f),
            value = selectedValue ?: "",
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() || it == '+' }) {
                    onSelect(newValue)
                }
            },
            singleLine = true
        )
    }
}

fun calculateDefaultCountry(value: String?): Country {
    val currentLocale: Locale = Locale.getDefault()
    val userCountry = Country.fromOrNull(currentLocale.country) ?: Country.ENGLAND
    if (value.isNullOrBlank()) {
        return userCountry
    }
    // guess the country from the calling code
    val matchingCountry = Country.entries.firstOrNull { c ->
        c.callingCodes.any { code ->
            value.startsWith(code)
        }
    }
    return matchingCountry ?: userCountry
}
