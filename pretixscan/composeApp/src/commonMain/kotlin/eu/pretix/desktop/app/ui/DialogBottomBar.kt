package eu.pretix.desktop.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DialogBottomBar(
    modifier: Modifier,
    cancelLabel: String? = null,
    primaryLabel: String,
    onCancel: (() -> Unit)? = null,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean = true) {
    Surface(
        modifier = modifier,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CustomColor.White.asColor())
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cancelLabel != null) {
                Button(onClick = {onCancel?.invoke()}) {
                    Text(cancelLabel)
                }
            }
            Spacer(modifier = Modifier.weight(1.0f))
            Button(
                onClick = onPrimary,
                enabled = primaryEnabled
            ) {
                Text(primaryLabel)
            }
        }
    }
}

@Composable
fun DialogBottomBarContent(
    modifier: Modifier,
    content: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CustomColor.White.asColor())
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}