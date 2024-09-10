package eu.pretix.desktop.cache

import java.util.Properties

// Helper object to retrive the version property from the gradle configuration
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