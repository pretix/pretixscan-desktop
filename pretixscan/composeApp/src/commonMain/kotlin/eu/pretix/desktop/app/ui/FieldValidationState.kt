package eu.pretix.desktop.app.ui

enum class FieldValidationState {
    // A value has been set but doesn't meet our requirements
    INVALID,

    // A required value is not set
    MISSING
}