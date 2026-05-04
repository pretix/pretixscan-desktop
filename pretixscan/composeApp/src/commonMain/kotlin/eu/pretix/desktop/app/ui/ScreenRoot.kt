package eu.pretix.desktop.app.ui


import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable function that acts as a container for main screens,
 * applying consistent styling such as background color, padding, and rounded corners.
 *
 * @param content A composable lambda that represents the content to be displayed inside the container.
 */
@Composable
@Preview
fun ScreenRoot(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .background(CustomColor.BrandDark.asColor())
            .padding(4.dp),
    ) {
        content()
    }
}