package eu.pretix.desktop.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tooltip(text: String, control: @Composable () -> Unit) {
    TooltipArea(tooltip = {
        Surface(
            color = CustomColor.White.asColor(),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }) {
        control()
    }
}