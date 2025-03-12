package settings.data

import app.ui.KeyValueOption

data class ConfigurableSettings(
    val version: String = "",
    val printers: List<KeyValueOption> = emptyList(),
    val printBadges: Boolean = false,
    val badgePrinter: KeyValueOption? = null,
    val badgeLayout: KeyValueOption? = null,
    val layouts: List<KeyValueOption> = emptyList(),
    val syncAuto: Boolean = false
)