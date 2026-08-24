package eu.pretix.desktop.app.ui.modifiers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

// Modifier to suppress the Material "ripple" effect when handling clicks on surfaces

@Composable
fun Modifier.noRippleEffect(onClick: () -> Unit) = clickable(
    interactionSource = remember { MutableInteractionSource() }, indication = null
) {
    onClick()
}

@Composable
fun Modifier.noRippleToggleable(
    value: Boolean,
    enabled: Boolean = true,
    role: Role? = null,
    onValueChange: (Boolean) -> Unit
) = toggleable(
    value = value,
    onValueChange = onValueChange,
    enabled = enabled,
    role = role,
    interactionSource = remember { MutableInteractionSource() },
    indication = null
)