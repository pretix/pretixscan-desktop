package eu.pretix.desktop.app.ui

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ListDivider(currentIndex: Int? = null, lastIndex: Int? = null) {
    if (lastIndex != null && currentIndex != null && currentIndex < lastIndex) {
        // show divider between rows except the last row
        HorizontalDivider(Modifier, thickness = 1.dp, color = Color.Gray)
    } else if (lastIndex == null && currentIndex == null) {
        // always show when no index is set
        HorizontalDivider(Modifier, thickness = 1.dp, color = Color.Gray)
    }
}