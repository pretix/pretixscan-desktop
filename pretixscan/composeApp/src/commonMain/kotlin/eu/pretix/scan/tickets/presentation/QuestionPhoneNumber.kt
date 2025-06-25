package eu.pretix.scan.tickets.presentation


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vanniktech.locale.Country
import eu.pretix.desktop.app.ui.*
import java.util.*


@Composable
fun QuestionPhoneNumber(
    selectedValue: String?,
    validation: FieldValidationState?,
    onSelect: (String?, String?) -> Unit
) {
    var country by remember { mutableStateOf(calculateDefaultCountry(selectedValue)) }
    country.callingCodes.first()

    LaunchedEffect(Unit, selectedValue) {
        country = calculateDefaultCountry(selectedValue)
    }

    Row(
        Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        FieldSpinner(
            selectedValue = country.code,
            availableOptions = Country.entries.map { country ->
                SelectableValue(
                    country.code,
                    country.name,
                    buttonContent = {
                        FieldSpinnerItem("${country.emoji} ${country.callingCodes.first()}")
                    },
                    content = {
                        FieldSpinnerItem(
                            "${country.emoji} ${
                                country.name.replace(
                                    "_",
                                    " "
                                )
                            } (${country.callingCodes.first()})"
                        )
                    }
                )
            },
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

        FieldTextInput(
            value = selectedValue ?: "",
            onValueChange = { newValue ->
                onSelect(newValue, country.code)
            },
            validation = validation
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
