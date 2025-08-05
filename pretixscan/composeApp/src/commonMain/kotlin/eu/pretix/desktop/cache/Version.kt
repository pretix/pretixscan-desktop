package eu.pretix.desktop.cache

import eu.pretix.desktop.generated.AppVersion

// Helper object to retrieve the version property from the gradle configuration
object Version {
    val version: String by lazy {
        AppVersion.VERSION_NAME
    }

    val versionCode: Int by lazy {
        AppVersion.VERSION_CODE
    }
}