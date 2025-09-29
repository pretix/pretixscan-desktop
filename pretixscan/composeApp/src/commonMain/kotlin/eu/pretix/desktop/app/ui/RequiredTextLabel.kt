// ABOUTME: Questions with custom components sometimes use stand-alone Text for their label

package eu.pretix.desktop.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RequiredTextLabel(
    label: String,
    required: Boolean = false,
    fontWeight: FontWeight? = null,
) {
    val labelText = if (required) "$label *" else label
    val accessibilityText = if (required) "$label, required" else label

    Text(
        text = labelText,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .semantics {
                contentDescription = accessibilityText
            },
        fontWeight = fontWeight
    )
}