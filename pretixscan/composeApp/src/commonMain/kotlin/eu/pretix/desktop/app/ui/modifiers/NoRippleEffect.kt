package eu.pretix.desktop.app.ui.modifiers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

// Modifier to suppress the Material "ripple" effect when handling clicks on surfaces

@Composable
fun Modifier.noRippleEffect(onClick: () -> Unit) = clickable(
    interactionSource = remember { MutableInteractionSource() }, indication = null
) {
    onClick()
}