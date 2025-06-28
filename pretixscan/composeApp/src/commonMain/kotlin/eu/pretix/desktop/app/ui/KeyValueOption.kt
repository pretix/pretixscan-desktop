package eu.pretix.desktop.app.ui

import androidx.compose.runtime.Composable

data class KeyValueOption(val key: String, val value: String)

data class SelectableValue(
    val value: String,
    val label: String,
    val content: (@Composable () -> Unit)? = null,
    val buttonContent: (@Composable () -> Unit)? = null,
)