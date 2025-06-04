package eu.pretix.desktop.cache

import net.harawata.appdirs.AppDirsFactory


/**
 * Retrieves the directory path where the user data for a specific application is stored.
 *
 * @return A string representing the path to the user data directory for the "pretixscan" application.
 */
actual fun getUserDataFolder(): String {
    val appDirsInstance = AppDirsFactory.getInstance()!!
    return appDirsInstance.getUserDataDir("pretixscan", "1", "pretix")
}

/**
 * Returns the user's cache folder path as a string.
 *
 * @return The cache folder path for the application as determined by the AppDirsFactory instance.
 *
 * It fetches the user cache directory for the application with the specified parameters.
 * The cache directory is specific to the application name "pretixscan" with a version "1"
 * and the application category "pretix".
 *
 * @throws NullPointerException if AppDirsFactory instance cannot be obtained.
 */
actual fun getUserCacheFolder(): String {
    val appDirsInstance = AppDirsFactory.getInstance()!!
    return appDirsInstance.getUserCacheDir("pretixscan", "1", "pretix")
}