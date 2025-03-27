package eu.pretix.scan.settings.presentation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.Logo
import eu.pretix.desktop.app.ui.asColor

@Composable
fun Toolbar(
    onGoBack: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(CustomColor.BrandDark.asColor())
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Logo()
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onGoBack) {
            Text("Back")
        }
    }
}