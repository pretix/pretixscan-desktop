package eu.pretix.desktop.migration

import java.io.File
import java.util.logging.Logger

sealed class CleanupResult {
    data object Success : CleanupResult()
    data class Failure(val error: String) : CleanupResult()
}

object V1StorageCleaner {
    private val logger = Logger.getLogger(V1StorageCleaner::class.java.name)

    /**
     * Clean up V1 storage directories (both data and cache)
     */
    fun cleanup(): CleanupResult {
        logger.info("Starting V1 storage cleanup")
        val errors = mutableListOf<String>()

        // Clean V1 data directory
        if (V1DirectoryLocator.hasV1Data()) {
            val dataDir = V1DirectoryLocator.getV1DataDir()

            try {
                deleteRecursively(dataDir)
                logger.info("Deleted V1 data directory: ${dataDir.absolutePath}")
            } catch (e: Exception) {
                val error = "Failed to delete V1 data directory (${dataDir.absolutePath}): ${e.message}"
                logger.warning(error)
                errors.add(error)
            }
        }

        // Clean V1 cache directory
        if (V1DirectoryLocator.hasV1Cache()) {
            val cacheDir = V1DirectoryLocator.getV1CacheDir()

            try {
                deleteRecursively(cacheDir)
                logger.info("Deleted V1 cache directory: ${cacheDir.absolutePath}")
            } catch (e: Exception) {
                val error = "Failed to delete V1 cache directory (${cacheDir.absolutePath}): ${e.message}"
                logger.warning(error)
                errors.add(error)
            }
        }

        return when {
            errors.isEmpty() -> CleanupResult.Success
            else -> CleanupResult.Failure(errors.joinToString("; "))
        }
    }

    /**
     * Recursively delete a file or directory
     */
    private fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursively(it) }
        }
        if (!file.delete()) {
            throw Exception("Failed to delete: ${file.absolutePath}")
        }
    }
}
