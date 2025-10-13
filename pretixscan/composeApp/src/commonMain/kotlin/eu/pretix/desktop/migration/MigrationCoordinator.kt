package eu.pretix.desktop.migration

import eu.pretix.desktop.cache.ConfigMigration
import java.util.logging.Logger

sealed class MigrationResult {
    object Success : MigrationResult()
    data class Failure(val error: String, val canRetry: Boolean) : MigrationResult()
}

class MigrationCoordinator(
    private val configMigration: ConfigMigration,
    private val tokenRoller: TokenRoller,
    private val v1DirectoryLocator: V1DirectoryLocator = V1DirectoryLocator
) {
    private val logger = Logger.getLogger(MigrationCoordinator::class.java.name)

    /**
     * Check if migration should be performed
     */
    suspend fun needsMigration(): Boolean {
        return configMigration.canMigrate()
    }

    /**
     * Execute the full migration flow:
     * 1. Migrate settings from V1 to V2
     * 2. Roll device API token
     * 3. Cleanup V1 storage
     */
    suspend fun executeMigration(): MigrationResult {
        logger.info("Starting migration from V1 to V2 configuration")

        return try {
            // Step 1: Migrate all settings
            configMigration.migrateSettings()
            logger.info("Settings migration completed successfully")

            // Step 2: Roll API token
            rollToken()

            // Step 3: Cleanup V1 storage
            performCleanup()

            logger.info("Migration completed successfully")
            MigrationResult.Success
        } catch (e: Exception) {
            logger.severe("Migration failed: ${e.message}")
            e.printStackTrace()
            MigrationResult.Failure(
                error = e.message ?: "Unknown error during migration",
                canRetry = true
            )
        }
    }

    /**
     * Roll device API token to invalidate old app access.
     */
    private suspend fun rollToken() {
        try {
            logger.info("Attempting to roll device API token")
            val result = tokenRoller.rollApiToken()

            result.fold(
                onSuccess = { newKey ->
                    tokenRoller.updateApiKey(newKey)
                    logger.info("Token rolled successfully - old app access revoked")
                },
                onFailure = { error ->
                    logger.warning("Token rolling failed (non-fatal): ${error.message}")
                    logger.warning("Old app may still have access - user can manually revoke device")
                }
            )
        } catch (e: Exception) {
            logger.warning("Token rolling failed (non-fatal): ${e.message}")
        }
    }

    /**
     * Perform silent cleanup of V1 storage directories.
     */
    private fun performCleanup() {
        configMigration.deleteOldConfig()

        if (!v1DirectoryLocator.hasV1Data() && !v1DirectoryLocator.hasV1Cache()) {
            logger.info("No V1 storage found, skipping cleanup")
            return
        }

        val dataPath = v1DirectoryLocator.getV1DataDir().absolutePath
        val cachePath = v1DirectoryLocator.getV1CacheDir().absolutePath

        logger.info("Starting automatic cleanup of V1 storage")

        when (val result = V1StorageCleaner.cleanup()) {
            is CleanupResult.Success -> {
                logger.info("V1 storage cleanup successful: from $dataPath and $cachePath")
            }

            is CleanupResult.Failure -> {
                logger.warning("V1 storage cleanup failed (non-fatal): ${result.error}")
            }
        }
    }
}
