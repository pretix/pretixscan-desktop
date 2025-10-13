package eu.pretix.desktop.migration

import net.harawata.appdirs.AppDirsFactory
import java.io.File

/**
 * Note: The V1 of the app, the settings folder may depend on java build options.
 */

object V1DirectoryLocator {
    /**
     * Get V1 data directory path (version "1" instead of "2")
     */
    fun getV1DataDir(): File {
        val appDirs = AppDirsFactory.getInstance()!!
        return File(appDirs.getUserDataDir("pretixscan", "1", "pretix"))
    }

    /**
     * Get V1 cache directory path (version "1" instead of "2")
     */
    fun getV1CacheDir(): File {
        val appDirs = AppDirsFactory.getInstance()!!
        return File(appDirs.getUserCacheDir("pretixscan", "1", "pretix"))
    }

    /**
     * Check if V1 data exists (directory exists and contains files)
     */
    fun hasV1Data(): Boolean {
        val dataDir = getV1DataDir()
        return dataDir.exists() && dataDir.isDirectory && (dataDir.listFiles()?.isNotEmpty() == true)
    }

    /**
     * Check if V1 cache exists (directory exists and contains files)
     */
    fun hasV1Cache(): Boolean {
        val cacheDir = getV1CacheDir()
        return cacheDir.exists() && cacheDir.isDirectory && (cacheDir.listFiles()?.isNotEmpty() == true)
    }
}
