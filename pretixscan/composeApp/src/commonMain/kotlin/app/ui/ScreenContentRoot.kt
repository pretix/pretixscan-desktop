package app.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable function that acts as a container for the main content on a screen,
 * that is content excluding any toolbars.
 *
 * @param content A composable lambda that represents the content to be displayed inside the container.
 */
@Composable
@Preview
fun ScreenContentRoot(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.background(CustomColor.BrandDark.asColor()),
        shape = RoundedCornerShape(8.dp),
    ) {
        content()
    }
}