package eu.pretix.scan.settings.data

import eu.pretix.desktop.app.ui.SelectableValue

data class ConfigurableSettings(
    val version: String = "",
    val printers: List<SelectableValue> = emptyList(),
    val printBadges: Boolean = false,
    val badgePrinter: SelectableValue? = null,
    val badgeLayout: SelectableValue? = null,
    val layouts: List<SelectableValue> = emptyList(),
    val syncAuto: Boolean = false,
    val playSounds: Boolean = false,
)