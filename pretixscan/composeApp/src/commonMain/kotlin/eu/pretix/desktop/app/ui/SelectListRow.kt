package eu.pretix.desktop.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectListRow(content: @Composable () -> Unit) {
    Box {
        Row(modifier = Modifier.fillMaxWidth().padding(PaddingValues(all = 16.dp))) {
            content()
        }
    }
}