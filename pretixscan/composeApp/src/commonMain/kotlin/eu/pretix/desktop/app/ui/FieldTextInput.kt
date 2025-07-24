package eu.pretix.desktop.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composeunstyled.TextField
import com.composeunstyled.TextInput
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.question_input_invalid
import pretixscan.composeapp.generated.resources.question_input_required

@Composable
fun FieldTextInput(
    label: String? = null,
    value: String,
    maxLines: Int = 1,
    onValueChange: (String) -> Unit,
    leading: @Composable (() -> Unit)? = null,
    validation: FieldValidationState? = null,
) {
    TextField(
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        maxLines = maxLines,
        singleLine = maxLines == 1,
    ) {
        if (label != null) {
            Text(label, modifier = Modifier.padding(bottom = 8.dp), fontWeight = FontWeight.SemiBold)
        }
        TextInput(
            Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            leading = leading
        )

        if (validation != null) {
            when (validation) {
                FieldValidationState.INVALID -> {
                    Text(
                        stringResource(Res.string.question_input_invalid),
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }

                FieldValidationState.MISSING -> {
                    Text(
                        stringResource(Res.string.question_input_required),
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }
}

