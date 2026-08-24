package eu.pretix.desktop.app.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AppearInstantly: EnterTransition = fadeIn(animationSpec = tween(durationMillis = 0))
val DisappearInstantly: ExitTransition = fadeOut(animationSpec = tween(durationMillis = 0))

val NoPadding = PaddingValues(0.dp)

val LocalContentColor = compositionLocalOf { Color.Black }