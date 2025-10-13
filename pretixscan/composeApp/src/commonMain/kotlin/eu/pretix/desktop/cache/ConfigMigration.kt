package eu.pretix.desktop.cache

import java.util.logging.Logger

/**
 * Handles migration from legacy Java Preferences (AppConfig)
 * to KMP DataStore (DataStoreConfig).
 */
class ConfigMigration(
    private val oldConfig: AppConfig,
    private val newConfig: DataStoreConfig
) {
    private val logger = Logger.getLogger(ConfigMigration::class.java.name)

    /**
     * Deletes the old configuration preferences from disk
     */
    fun deleteOldConfig() {
        oldConfig.resetEventConfig()
    }

    /**
     * Check if old configuration exists and hasn't been migrated yet.
     */
    suspend fun canMigrate(): Boolean {
        return oldConfig.isConfigured && !newConfig.isMigrationComplete()
    }

    /**
     * Perform migration from AppConfig to DataStoreConfig.
     * @throws Exception if migration fails - user can retry
     */
    suspend fun migrateSettings() {
        if (!oldConfig.isConfigured) {
            logger.info("No old configuration to migrate")
            return
        }

        if (newConfig.isMigrationComplete()) {
            logger.info("Migration already completed, skipping")
            return
        }

        logger.info("Starting configuration migration from Preferences to DataStore")

        try {
            // Device & API Configuration (11 properties)
            newConfig.setApiUrl(oldConfig.apiUrl)
            newConfig.setApiKey(oldConfig.apiKey)
            newConfig.setApiVersion(oldConfig.apiVersion)
            newConfig.setApiDeviceId(oldConfig.getApiDeviceId())
            newConfig.setDeviceSerial(oldConfig.deviceSerial)
            newConfig.setOrganizerSlug(oldConfig.organizerSlug)

            // Device Metadata (6 properties)
            newConfig.setDeviceKnownVersion(oldConfig.deviceKnownVersion)
            newConfig.setDeviceKnownInfo(oldConfig.deviceKnownInfo)
            newConfig.setDeviceKnownName(oldConfig.deviceKnownName)
            newConfig.setDeviceKnownGateName(oldConfig.deviceKnownGateName)
            newConfig.setDeviceKnownGateID(oldConfig.deviceKnownGateID)
            newConfig.setKnownPretixVersion(oldConfig.knownPretixVersion)

            // Event Configuration
            newConfig.setEventSelections(oldConfig.eventSelections)
            newConfig.setActiveEventIndex(oldConfig.activeEventIndex)

            // Sync State
            newConfig.setLastSync(oldConfig.lastSync)
            newConfig.setLastDownload(oldConfig.lastDownload)
            newConfig.setLastCleanup(oldConfig.lastCleanup)
            newConfig.setLastFailedSync(oldConfig.lastFailedSync)
            newConfig.setLastFailedSyncMsg(oldConfig.lastFailedSyncMsg)
            newConfig.setKnownLiveEventSlugs(oldConfig.knownLiveEventSlugs)
            newConfig.setSyncOrders(oldConfig.syncOrders)
            newConfig.setSyncAuto(oldConfig.syncAuto)

            // Mode Flags
            newConfig.setOfflineMode(oldConfig.offlineMode)
            newConfig.setProxyMode(oldConfig.proxyMode)

            // UI Preferences
            newConfig.setPlaySound(oldConfig.playSound)
            newConfig.setUiHideNames(oldConfig.uiHideNames)
            newConfig.setUiReduceMotion(oldConfig.uiReduceMotion)
            newConfig.setAutoPrintBadges(oldConfig.autoPrintBadges)

            // Hardware Settings
            newConfig.setPreferredCameraName(oldConfig.preferredCameraName)
            newConfig.setScanType(oldConfig.scanType)
            newConfig.setBadgePrinterName(oldConfig.badgePrinterName)
            newConfig.setBadgePrinterOrientation(oldConfig.badgePrinterOrientation)

            // Mark migration complete
            newConfig.markMigrationComplete()

            logger.info("Configuration migration completed successfully")

        } catch (e: Exception) {
            logger.severe("Migration failed: ${e.message}")
            // Don't mark as complete - user can retry
            throw e
        }
    }
}
