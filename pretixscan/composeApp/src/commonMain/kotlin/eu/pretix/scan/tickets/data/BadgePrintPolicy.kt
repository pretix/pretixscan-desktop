package eu.pretix.scan.tickets.data

enum class BadgePrintPolicy(val storageValue: String) {
    WHEN_BUTTON_PRESSED("button"),
    ONCE_IF_NOT_PRINTED("once"),
    ALWAYS("always");

    companion object {
        fun fromStorageValue(value: String?): BadgePrintPolicy =
            entries.firstOrNull { it.storageValue == value } ?: ONCE_IF_NOT_PRINTED
    }
}
