package eu.pretix.scan.tickets.presentation

import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.roundToInt

fun Modifier.blinking(
    enabled: Boolean = true,
    periodMillis: Int = 1500,
    motionScale: Float = 1f,
    alphaEnabled: Boolean = true,
    bgEnabled: Boolean = false,
    bg: Color? = null,        // null = no base bg
    bgBlink: Color? = null    // null = no blink target
): Modifier = composed {
    if (!enabled || motionScale <= 0f) {
        return@composed if (bg != null) this.then(Modifier.drawBehind { drawRect(bg) }) else this
    }

    val half = (periodMillis / 2f / motionScale.coerceAtLeast(0.0001f)).roundToInt()
    val transition = rememberInfiniteTransition(label = "blink")
    val t by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = half, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "t"
    )

    val a = if (alphaEnabled) 1f - t else 1f
    val bgColor: Color? = when {
        bgEnabled && bg != null && bgBlink != null -> lerp(bg, bgBlink, t)
        bg != null -> bg
        else -> null
    }

    var m = this
    if (alphaEnabled) m = m.then(Modifier.alpha(a))
    if (bgColor != null) m = m.then(Modifier.drawBehind { drawRect(bgColor) })
    m
}