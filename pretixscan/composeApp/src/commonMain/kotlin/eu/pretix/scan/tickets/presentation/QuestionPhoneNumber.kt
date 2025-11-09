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
                    onSelect(selectedValue, it.value)
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


