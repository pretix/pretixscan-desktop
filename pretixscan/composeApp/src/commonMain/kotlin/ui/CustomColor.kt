package ui

import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.compose.ui.graphics.Color

// color resources from xml are not supported by compose
// while a custom theme may be a solution, using hardcoded values as a workaround

sealed class CustomColor(val hex: String) {
    data object BrandDark : CustomColor("#281333")
    data object BrandLight : CustomColor("#3B1C4A")
    data object BrandGreen : CustomColor("#5CB85C")

    data object BrandRed : CustomColor("#D36060")

    data object BrandOrange: CustomColor("#FFB419")

    data object White: CustomColor("#FFFFFF")
}


// from https://stackoverflow.com/questions/60247480/color-from-hex-string-in-jetpack-compose
@ColorInt
fun parseColor(@Size(min = 1) colorString: String): Int {
    if (colorString[0] == '#') { // Use a long to avoid rollovers on #ffXXXXXX
        var color = colorString.substring(1).toLong(16)
        if (colorString.length == 7) { // Set the alpha value
            color = color or -0x1000000
        } else require(colorString.length == 9) { "Unknown color" }
        return color.toInt()
    }
    throw IllegalArgumentException("Unknown color")
}


fun CustomColor.asColour(): Color {
    return Color(parseColor(hex))
}