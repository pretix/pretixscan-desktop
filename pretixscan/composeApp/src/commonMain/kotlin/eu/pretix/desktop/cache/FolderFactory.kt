package eu.pretix.desktop.cache

import java.io.File

expect fun getUserDataFolder(): String

expect fun getUserCacheFolder(): String

/**
Retrieves the path to the legacy database file.

@return A string representing the absolute path to the legacy database file.
 */
//internal fun getLegacyDatabasePath(): String {
//    val dataDir = getUserDataFolder()
//    val dbFile = File("$dataDir/data.sqlite")
//    return dbFile.absolutePath
//}

internal fun getDatabasePath(): File {
    val dataDir = getUserDataFolder()
    // make sure the path exists so we can later create files in it
    File(dataDir).mkdirs()
    val dbFile = File("$dataDir/db.sqlite")
    return dbFile
}

internal fun getUserDataDir(): File {
    val dataDir = getUserDataFolder()
    // make sure the path exists so we can later create files in it
    File(dataDir).mkdirs()
    return File(dataDir)
}