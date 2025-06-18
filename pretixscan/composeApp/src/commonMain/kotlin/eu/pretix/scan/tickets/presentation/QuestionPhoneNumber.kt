package eu.pretix.scan.tickets.presentation


import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.vanniktech.locale.Country
import eu.pretix.desktop.app.ui.FieldSpinner
import eu.pretix.desktop.app.ui.FieldSpinnerItem
import eu.pretix.desktop.app.ui.FieldTextInput
import eu.pretix.desktop.app.ui.SelectableValue
import java.util.*


@Composable
fun QuestionPhoneNumber(
    selectedValue: String?,
    onSelect: (String?) -> Unit
) {
    var country by remember { mutableStateOf(calculateDefaultCountry(selectedValue)) }
    country.callingCodes.first()

    LaunchedEffect(Unit, selectedValue) {
        country = calculateDefaultCountry(selectedValue)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
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
                if (newValue.all { it.isDigit() || it == '+' }) {
                    onSelect(newValue)
                }
            }
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
