package app.ui.modifiers

import androidx.compose.ui.Modifier

/**
 Applies the provided modifier when the condition evaluates to `true`
 */
fun Modifier.conditional(condition : Boolean, modifier : Modifier.() -> Modifier) : Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}