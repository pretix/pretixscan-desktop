package eu.pretix.desktop.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.question_input_invalid
import pretixscan.composeapp.generated.resources.question_input_required

@Composable
fun FieldValidationText(validation: FieldValidationState?, validationMessage: String? = null) {
    if (validation != null) {
        when (validation) {
            FieldValidationState.INVALID -> {
                Text(
                    validationMessage ?: stringResource(Res.string.question_input_invalid),
                    color = CustomColor.BrandRed.asColor(),
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }
            FieldValidationState.MISSING -> {
                Text(
                    stringResource(Res.string.question_input_required),
                    color = CustomColor.BrandRed.asColor(),
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }
        }
    }
}
