package eu.pretix.desktop.cache

// Helper object to retrieve the version property from the gradle configuration
object Version {
    val version: String by lazy {
        // TODO: Obtain the version from build.gradle or properties file
        return@lazy "1.20.0"
    }


    val versionCode: Int by lazy {
        // TODO: Obtain the version from build.gradle or properties file
        return@lazy 1
    }
}