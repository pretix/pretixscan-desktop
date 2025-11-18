package eu.pretix.scan.tickets.presentation


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vanniktech.locale.Country
import eu.pretix.desktop.app.ui.*
import eu.pretix.scan.tickets.data.calculateDefaultCountry


@Composable
fun QuestionPhoneNumber(
    selectedValue: String?,
    validation: FieldValidationState?,
    uiExtra: String?,
    onSelect: (String?, String?) -> Unit
) {
    var isCountryExplicitlySelected by remember(uiExtra, selectedValue) {
        val detectedCountry = calculateDefaultCountry(selectedValue)
        val providedCountry = if (!uiExtra.isNullOrBlank()) {
            Country.entries.firstOrNull { it.code == uiExtra }
        } else {
            null
        }
        mutableStateOf(providedCountry != null && providedCountry.code != detectedCountry.code)
    }

    val country = remember(uiExtra, selectedValue) {
        when {
            !uiExtra.isNullOrBlank() -> Country.entries.firstOrNull { it.code == uiExtra }
                ?: calculateDefaultCountry(selectedValue)
            else -> calculateDefaultCountry(selectedValue)
        }
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
                    country.readableName(),
                    buttonContent = {
                        FieldSpinnerItem("${country.emoji} ${country.callingCodes.first()}")
                    },
                    content = {
                        FieldSpinnerItem(
                            "${country.emoji} ${
                                country.readableName()
                            } (${country.callingCodes.first()})"
                        )
                    }
                )
            },
            onSelect = {
                if (it != null) {
                    isCountryExplicitlySelected = true
                    onSelect("", it.value)
                }
            }
        )

        FieldTextInput(
            value = selectedValue ?: "",
            onValueChange = { newValue ->
                if (!isCountryExplicitlySelected && !newValue.isNullOrBlank() && newValue.startsWith("+")) {
                    val detectedCountry = calculateDefaultCountry(newValue)
                    if (detectedCountry.code != country.code) {
                        onSelect(newValue, detectedCountry.code)
                    } else {
                        onSelect(newValue, country.code)
                    }
                } else {
                    onSelect(newValue, country.code)
                }
            },
            validation = validation
        )
    }
}


