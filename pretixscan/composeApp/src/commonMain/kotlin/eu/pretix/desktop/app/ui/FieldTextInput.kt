package eu.pretix.desktop.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    minLines: Int = 1,
    onValueChange: (String) -> Unit,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    required: Boolean = false,
    validation: FieldValidationState? = null,
    validationMessage: String? = null,
    maxLength: Int? = null,
    showLimitCounter: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val isMultiline = maxLines > 1
    val focusManager = LocalFocusManager.current

    TextField(
        value = value,
        editable = enabled,
        onValueChange = {
            if (!enabled) return@TextField
            if (isMultiline && it.count { c -> c == '\n' } >= maxLines) return@TextField
            if (maxLength != null && it.length > maxLength) return@TextField
            onValueChange(it)
        },
        maxLines = maxLines,
        minLines = minLines,
        singleLine = !isMultiline,
        modifier = modifier.then(
            if (isMultiline) {
                Modifier.onPreviewKeyEvent { event ->
                    if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                        val direction = if (event.isShiftPressed) FocusDirection.Previous else FocusDirection.Next
                        focusManager.moveFocus(direction)
                        true
                    } else {
                        false
                    }
                }
            } else {
                Modifier
            }
        ),
    ) {
        if (label != null) {
            RequiredTextLabel(label = label, required = required, fontWeight = FontWeight.SemiBold)
        }
        val inputModifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
            .background(if (enabled) Color.White else Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)

        TextInput(
            inputModifier,
            leading = leading,
            trailing = trailing
        )

        if (maxLength != null && showLimitCounter) {
            Text(
                "${value.length}/$maxLength",
                color = if (value.length >= maxLength) CustomColor.BrandRed.asColor() else CustomColor.BrandDark.asColor(),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }

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
}

