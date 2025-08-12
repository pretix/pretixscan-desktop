package eu.pretix.desktop.app.ui

import androidx.compose.runtime.Composable

data class SelectableValue(
    val value: String,
    val label: String,
    val content: (@Composable () -> Unit)? = null,
    val buttonContent: (@Composable () -> Unit)? = null,
)