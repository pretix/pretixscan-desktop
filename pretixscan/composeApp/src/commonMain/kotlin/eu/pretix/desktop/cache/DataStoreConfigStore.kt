package eu.pretix.desktop.cache

import eu.pretix.libpretixsync.config.ConfigStore
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

/**
 * Adapter that wraps DataStoreConfig to implement the synchronous ConfigStore interface
 * required by libpretixsync.
 *
 * Uses runBlocking to convert suspend functions to synchronous calls.
 * This is acceptable since libpretixsync operations are typically background tasks.
 */
class DataStoreConfigStore(
    private val dataStoreConfig: DataStoreConfig
) : ConfigStore {

    override fun isDebug(): Boolean = false

    override fun isConfigured(): Boolean = runBlocking {
        dataStoreConfig.isConfigured()
    }

    override fun getApiVersion(): Int = runBlocking {
        dataStoreConfig.getApiVersion()
    }

    override fun getApiUrl(): String = runBlocking {
        dataStoreConfig.getApiUrl()
    }

    override fun getApiKey(): String = runBlocking {
        dataStoreConfig.getApiKey()
    }

    override fun getOrganizerSlug(): String = runBlocking {
        dataStoreConfig.getOrganizerSlug()
    }

    override fun getDeviceKnownName(): String = runBlocking {
        dataStoreConfig.getDeviceKnownName()
    }

    override fun setDeviceKnownName(value: String?) {
        runBlocking {
            dataStoreConfig.setDeviceKnownName(value)
        }
    }

    override fun getDeviceKnownGateName(): String = runBlocking {
        dataStoreConfig.getDeviceKnownGateName()
    }

    override fun setDeviceKnownGateName(value: String?) {
        runBlocking {
            dataStoreConfig.setDeviceKnownGateName(value)
        }
    }

    override fun getDeviceKnownGateID(): Long = runBlocking {
        dataStoreConfig.getDeviceKnownGateID()
    }

    override fun setDeviceKnownGateID(value: Long?) {
        runBlocking {
            dataStoreConfig.setDeviceKnownGateID(value)
        }
    }

    override fun getDeviceKnownVersion(): Int = runBlocking {
        dataStoreConfig.getDeviceKnownVersion()
    }

    override fun setDeviceKnownVersion(value: Int) {
        runBlocking {
            dataStoreConfig.setDeviceKnownVersion(value)
        }
    }

    override fun getDeviceKnownInfo(): JSONObject = runBlocking {
        dataStoreConfig.getDeviceKnownInfo()
    }

    override fun setDeviceKnownInfo(value: JSONObject?) {
        runBlocking {
            dataStoreConfig.setDeviceKnownInfo(value)
        }
    }

    override fun getSyncCycleId(): String = "0"

    override fun getPosId(): Long = 0

    override fun getSynchronizedEvents(): List<String> = runBlocking {
        dataStoreConfig.getSynchronizedEvents()
    }

    override fun getSelectedSubeventForEvent(event: String): Long? = runBlocking {
        dataStoreConfig.getSelectedSubeventForEvent(event)
    }

    override fun getSelectedCheckinListForEvent(event: String?): Long = runBlocking {
        dataStoreConfig.getSelectedCheckinListForEvent(event)
    }

    override fun getLastDownload(): Long = runBlocking {
        dataStoreConfig.getLastDownload()
    }

    override fun setLastDownload(`val`: Long) {
        runBlocking {
            dataStoreConfig.setLastDownload(`val`)
        }
    }

    override fun getLastSync(): Long = runBlocking {
        dataStoreConfig.getLastSync()
    }

    override fun setLastSync(`val`: Long) {
        runBlocking {
            dataStoreConfig.setLastSync(`val`)
        }
    }

    override fun getLastCleanup(): Long = runBlocking {
        dataStoreConfig.getLastCleanup()
    }

    override fun setLastCleanup(`val`: Long) {
        runBlocking {
            dataStoreConfig.setLastCleanup(`val`)
        }
    }

    override fun getLastFailedSync(): Long = runBlocking {
        dataStoreConfig.getLastFailedSync()
    }

    override fun setLastFailedSync(`val`: Long) {
        runBlocking {
            dataStoreConfig.setLastFailedSync(`val`)
        }
    }

    override fun getLastFailedSyncMsg(): String = runBlocking {
        dataStoreConfig.getLastFailedSyncMsg()
    }

    override fun setLastFailedSyncMsg(`val`: String?) {
        runBlocking {
            dataStoreConfig.setLastFailedSyncMsg(`val`)
        }
    }

    override fun getKnownPretixVersion(): Long = runBlocking {
        dataStoreConfig.getKnownPretixVersion()
    }

    override fun setKnownPretixVersion(`val`: Long) {
        runBlocking {
            dataStoreConfig.setKnownPretixVersion(`val`)
        }
    }

    override fun getKnownLiveEventSlugs(): Set<String> = runBlocking {
        dataStoreConfig.getKnownLiveEventSlugs()
    }

    override fun setKnownLiveEventSlugs(slugs: Set<String>) {
        runBlocking {
            dataStoreConfig.setKnownLiveEventSlugs(slugs)
        }
    }

    // Deprecated properties - hardcoded defaults
    override fun getAutoSwitchRequested(): Boolean = false

    // ============================================================
    // Synchronous Facade Properties for Application Usage
    // ============================================================

    // Mode Flags
    var proxyMode: Boolean
        get() = runBlocking { dataStoreConfig.getProxyMode() }
        set(value) = runBlocking { dataStoreConfig.setProxyMode(value) }

    var offlineMode: Boolean
        get() = runBlocking { dataStoreConfig.getOfflineMode() }
        set(value) = runBlocking { dataStoreConfig.setOfflineMode(value) }

    // API Configuration properties are inherited from ConfigStore interface

    // Event Configuration
    var eventSelections: List<EventSelection>
        get() = runBlocking { dataStoreConfig.getEventSelections() }
        set(value) = runBlocking { dataStoreConfig.setEventSelections(value) }

    var activeEventIndex: Int
        get() = runBlocking { dataStoreConfig.getActiveEventIndex() }
        set(value) = runBlocking { dataStoreConfig.setActiveEventIndex(value) }

    val activeEvent: EventSelection?
        get() = runBlocking { dataStoreConfig.getActiveEvent() }

    // Scan Type
    var scanType: String
        get() = runBlocking { dataStoreConfig.getScanType() }
        set(value) = runBlocking { dataStoreConfig.setScanType(value) }

    // UI Preferences
    var playSound: Boolean
        get() = runBlocking { dataStoreConfig.getPlaySound() }
        set(value) = runBlocking { dataStoreConfig.setPlaySound(value) }

    var uiHideNames: Boolean
        get() = runBlocking { dataStoreConfig.getUiHideNames() }
        set(value) = runBlocking { dataStoreConfig.setUiHideNames(value) }

    var uiReduceMotion: Boolean
        get() = runBlocking { dataStoreConfig.getUiReduceMotion() }
        set(value) = runBlocking { dataStoreConfig.setUiReduceMotion(value) }

    var autoPrintBadges: Boolean
        get() = runBlocking { dataStoreConfig.getAutoPrintBadges() }
        set(value) = runBlocking { dataStoreConfig.setAutoPrintBadges(value) }

    // Feature Flags
    var printBadges: Boolean
        get() = runBlocking { dataStoreConfig.getPrintBadges() }
        set(value) = runBlocking { dataStoreConfig.setPrintBadges(value) }

    var syncOrders: Boolean
        get() = runBlocking { dataStoreConfig.getSyncOrders() }
        set(value) = runBlocking { dataStoreConfig.setSyncOrders(value) }

    var syncAuto: Boolean
        get() = runBlocking { dataStoreConfig.getSyncAuto() }
        set(value) = runBlocking { dataStoreConfig.setSyncAuto(value) }

    // Hardware Settings
    var preferredCameraName: String
        get() = runBlocking { dataStoreConfig.getPreferredCameraName() }
        set(value) = runBlocking { dataStoreConfig.setPreferredCameraName(value) }

    var badgePrinterName: String?
        get() = runBlocking { dataStoreConfig.getBadgePrinterName() }
        set(value) = runBlocking { dataStoreConfig.setBadgePrinterName(value) }

    var badgePrinterOrientation: String
        get() = runBlocking { dataStoreConfig.getBadgePrinterOrientation() }
        set(value) = runBlocking { dataStoreConfig.setBadgePrinterOrientation(value) }

    // Bulk Operations
    fun setDeviceConfig(url: String, key: String, orgaSlug: String, deviceId: Long, serial: String, sentVersion: Int) {
        runBlocking {
            dataStoreConfig.setDeviceConfig(url, key, orgaSlug, deviceId, serial, sentVersion)
        }
    }

    fun resetEventConfig() {
        runBlocking {
            dataStoreConfig.resetEventConfig()
        }
    }

    // ============================================================
    // Single-Event Storage
    // ============================================================
    // These properties have their own storage (not derived from eventSelections)
    // Both storage systems coexist

    var eventSlug: String?
        get() = runBlocking { dataStoreConfig.getEventSlug() }
        set(value) = runBlocking { dataStoreConfig.setEventSlug(value) }

    var subEventId: Long?
        get() = runBlocking { dataStoreConfig.getSubEventId() }
        set(value) = runBlocking { dataStoreConfig.setSubEventId(value) }

    var eventName: String?
        get() = runBlocking { dataStoreConfig.getEventName() }
        set(value) = runBlocking { dataStoreConfig.setEventName(value) }

    var checkInListId: Long
        get() = runBlocking { dataStoreConfig.getCheckInListId() }
        set(value) = runBlocking { dataStoreConfig.setCheckInListId(value) }

    var checkInListName: String
        get() = runBlocking { dataStoreConfig.getCheckInListName() }
        set(value) = runBlocking { dataStoreConfig.setCheckInListName(value) }

    // Convenience method for backward compatibility
    fun eventSelectionToMap(): Map<String, Long> {
        return eventSelections.associate { it.eventSlug to it.checkInListId }
    }
}
