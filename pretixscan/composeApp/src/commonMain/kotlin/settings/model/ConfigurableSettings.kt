package settings.model

data class ConfigurableSettings(
    val version: String
) {
    companion object
}

fun ConfigurableSettings.Companion.empty(): ConfigurableSettings {
    return ConfigurableSettings(
        version = ""
    )
}